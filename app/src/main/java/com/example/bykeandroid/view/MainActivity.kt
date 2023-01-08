package com.example.bykeandroid.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bykeandroid.R
import com.example.bykeandroid.data.Commands
import com.example.bykeandroid.data.Coordinates
import com.example.bykeandroid.data.parseCommand
import com.example.bykeandroid.utils.MyDialog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val RUNTIME_PERMISSION_REQUEST_CODE = 2

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    var macAdress : String? = null
    lateinit var bluetoothGatt : BluetoothGatt
    lateinit var comCharacteristic: BluetoothGattCharacteristic
    var dataToSend: Array<ByteArray> = arrayOf()
    var readData: ByteArray = byteArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
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
            if (macAdress == result.device.address) {
                Log.i("ScanCallback", "Found your device!");
                macAdress = result.device.address
                bleScanner.stopScan(this)
                result.device.connectGatt(this@MainActivity, false, gattCallback)
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


    override fun onResume() {
        super.onResume()
        if (bluetoothAdapter.isEnabled == false) {
            promptEnableBluetooth()
        }
    }

    private fun promptEnableBluetooth() {
        if (bluetoothAdapter.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }
    fun Context.hasRequiredRuntimePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun startBleScan() {
        if (hasRequiredRuntimePermissions() == false) {
            requestRelevantRuntimePermissions()
        } else {
            bleScanner.startScan(null, scanSettings, scanCallback)
        }
    }
    private fun Activity.requestRelevantRuntimePermissions() {
        if (hasRequiredRuntimePermissions()) { return }
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                requestLocationPermission()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestBluetoothPermissions()
            }
        }
    }

    private fun requestLocationPermission() {
        runOnUiThread {
            MyDialog("Starting from Android M (6.0), the system requires apps to be granted " +
                    "location access in order to scan for BLE devices.", {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
            }, {}).show(supportFragmentManager, "MyDialog")
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestBluetoothPermissions() {
        runOnUiThread {
            MyDialog("Starting from Android 12, the system requires apps to be granted " +
                    "Bluetooth access in order to scan for and connect to BLE devices.", {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    RUNTIME_PERMISSION_REQUEST_CODE
                )
            }, {}).show(supportFragmentManager, "MyDialog")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RUNTIME_PERMISSION_REQUEST_CODE -> {
                val containsPermanentDenial = permissions.zip(grantResults.toTypedArray()).any {
                    it.second == PackageManager.PERMISSION_DENIED &&
                            ActivityCompat.shouldShowRequestPermissionRationale(this, it.first) == false
                }
                val containsDenial = grantResults.any { it == PackageManager.PERMISSION_DENIED }
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                when {
                    containsPermanentDenial -> {
                        // TODO: Handle permanent denial (e.g., show AlertDialog with justification)
                        // Note: The user will need to navigate to App Settings and manually grant
                        // permissions that were permanently denied
                    }
                    containsDenial -> {
                        requestRelevantRuntimePermissions()
                    }
                    allGranted && hasRequiredRuntimePermissions() -> {
                        startBleScan()
                    }
                    else -> {
                        // Unexpected scenario encountered when handling permissions
                        recreate()
                    }
                }
            }
        }
    }
}
