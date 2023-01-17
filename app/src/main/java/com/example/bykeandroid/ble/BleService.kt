package com.example.bykeandroid.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.bykeandroid.data.Commands
import com.example.bykeandroid.data.Coordinates
import com.example.bykeandroid.data.parseCommand
import com.example.bykeandroid.view.MainActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@SuppressLint("MissingPermission")
class BleService(
    private val activity: MainActivity
) {
    private var deviceMac: String? = null
    private lateinit var bluetoothGatt: BluetoothGatt
    private lateinit var comCharacteristic: BluetoothGattCharacteristic
    private var dataToSend: Array<ByteArray> = arrayOf()
    private var readData: ByteArray = byteArrayOf()
    private var commandsCallback: EnumMap<Commands, (String?) -> Unit> =
        EnumMap(Commands::class.java)
    private var onDeviceFound: (() -> Unit)? = null
    private var onDeviceConnected: (() -> Unit)? = null
    var isConnected: Boolean = false
    var isScanning: Boolean = false

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (deviceMac == result.device.address) {
                Log.i("ScanCallback", "Found your device!")
                activity.runOnUiThread(onDeviceFound)
                deviceMac = result.device.address
                bleScanner.stopScan(this)
                result.device.connectGatt(activity, false, gattCallback)
            }
        }
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i(
                "printGattTable",
                "No service and characteristic available, call discoverServices() first?"
            )
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i(
                "printGattTable",
                "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            isScanning = false
            isConnected = false

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    isConnected = true
                    bluetoothGatt = gatt
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            gatt?.printGattTable()
            val comService = gatt?.services?.last()?.characteristics?.first()?.service
            val char = comService?.characteristics?.first()
            if (char != null) {
                comCharacteristic = char
            }
            activity.runOnUiThread(onDeviceConnected)
        }

        fun onRead(value: ByteArray) {
            Log.i("onCharacteristicRead", "Read: ${value.contentToString()}")
            if (readData.isNotEmpty()) {
                readData += value
            } else {
                readData = value
            }

            val triple = parseCommand(readData)
            if (triple == null) {
                Log.w("BLE read", "Not a command")
                return
            }

            val (command, length, info) = triple
            if (command == Commands.NONE && length != 0) {
                Log.i("BLE read", "Incomplete data")
                read()
                return
            }

            readData = byteArrayOf()
            activity.runOnUiThread {
                commandsCallback[command]?.invoke(info)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            onRead(value)
        }

        // If version is >= 33
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (characteristic != null) {
                onRead(characteristic.value)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Log.i("onCharacteristicWrite", "Value: ${characteristic?.value}")
            if (dataToSend.isNotEmpty()) {
                bleWrite(dataToSend[0])
                dataToSend = dataToSend.drop(1).toTypedArray()
            }
        }
    }

    private fun bleWrite(data: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt.writeCharacteristic(
                comCharacteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        } else {
            Log.i("write", "Must use deprecated write (API < 33)")
            comCharacteristic.value = data
            comCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            bluetoothGatt.writeCharacteristic(comCharacteristic)
        }
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()


    // -------------------------------- BLE Utils --------------------------------

    fun read() {
        Log.i("read", "Reading")
        if (isConnected) {
            bluetoothGatt.readCharacteristic(comCharacteristic)
        }
    }

    fun write(command: Commands, info: Coordinates? = null) {
        if (command.hasInfo() && info == null) {
            throw Exception("Command should have info but no info was provided")
        }
        val infoString = info?.let { Json.encodeToString(it) }.orEmpty()
        val infoData = infoString.toByteArray()
        val data = byteArrayOf(command.getCode(), infoData.size.toByte(), *infoData)


        if (data.size > 20) {
            for (i in data.indices step 20) {
                dataToSend += data.sliceArray(i until minOf(i + 20, data.size))
            }
            bleWrite("".toByteArray())
        } else {
            bleWrite(data)
        }
    }

    // -------------------------------- Utils --------------------------------
    fun startBleScan() {
        if (isScanning) {
            Log.i("startBleScan", "Already scanning")
            return
        }
        isScanning = true
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun setDeviceMac(mac: String?) {
        deviceMac = mac
    }

    fun onCommand(command: Commands, callback: (info: String?) -> Unit): BleService {
        commandsCallback[command] = callback
        return this
    }

    fun onDeviceFound(callback: () -> Unit): BleService {
        onDeviceFound = callback
        return this
    }

    fun onDeviceConnected(callback: () -> Unit): BleService {
        onDeviceConnected = callback
        return this
    }
}