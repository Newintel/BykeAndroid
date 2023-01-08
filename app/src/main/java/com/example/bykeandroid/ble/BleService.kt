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

@SuppressLint("MissingPermission")
class BleService(private val activity: MainActivity) {
    private var deviceMac : String? = null
    lateinit var bluetoothGatt : BluetoothGatt
    lateinit var comCharacteristic: BluetoothGattCharacteristic
    var dataToSend: Array<ByteArray> = arrayOf()
    var readData: ByteArray = byteArrayOf()

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.i("ScanCallback", "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
            }
            if (deviceMac == result.device.address) {
                Log.i("ScanCallback", "Found your device!");
                deviceMac = result.device.address
                bleScanner.stopScan(this)
                result.device.connectGatt(activity, false, gattCallback)
            }
        }
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // TODO: Store a reference to BluetoothGatt
                    bluetoothGatt = gatt
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
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
            read()
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
            if (command == Commands.NONE && length != 0){
                Log.i("BLE read", "Incomplete data")
                read()
                return
            }

            readData = byteArrayOf()
            when (command) {
                else -> {
                    Log.i("BLE read", "$command")
                }
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
                ble_write(dataToSend[0])
                dataToSend = dataToSend.drop(1).toTypedArray()
            }
        }
    }

    fun read() {
        Log.i("read", "Reading")
        bluetoothGatt.readCharacteristic(comCharacteristic)
    }

    fun write(command : Commands, info : Coordinates? = null) {
        if (command.has_info() && info == null) {
            throw Exception("Command has info but no info was provided")
        }
        val infoString = info?.let { Json.encodeToString(it) }.orEmpty()
        val infoData = infoString.toByteArray()
        val data = byteArrayOf(command.getCode(), infoData.size.toByte(), *infoData)


        if (data.size > 20) {
            for (i in data.indices step 20) {
                dataToSend += data.sliceArray(i until minOf(i + 20, data.size))
            }
            ble_write("".toByteArray())
        } else {
            ble_write(data)
        }
    }

    private fun ble_write(data : ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt.writeCharacteristic(comCharacteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
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


    // -------------------------------- Utils --------------------------------
    fun startBleScan() {
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    fun isBluetoothEnabled() : Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun setDeviceMac(mac : String?) {
        deviceMac = mac
    }
}