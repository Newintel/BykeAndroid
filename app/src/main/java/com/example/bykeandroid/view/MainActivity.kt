package com.example.bykeandroid.view

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.example.bykeandroid.R
import com.example.bykeandroid.api.ApiServices
import com.example.bykeandroid.ble.BlePoller
import com.example.bykeandroid.ble.BleService
import com.example.bykeandroid.data.Commands
import com.example.bykeandroid.data.Coordinates
import com.example.bykeandroid.data.Step
import com.example.bykeandroid.utils.MyDialog
import com.example.bykeandroid.viewmodel.AccountViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


private const val RUNTIME_PERMISSION_REQUEST_CODE = 2

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    val bleService = BleService(this)
    lateinit var bottomNavigationView: BottomNavigationView
    val accountViewModel = AccountViewModel()
    val blePoller = BlePoller(bleService)
    var pollingJob : Job? = null

    var homePageView: View? = null
    var accountPageView: View? = null
    var connectionPageView: View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottom_nav)

        val job = Job()
        val coroutineScope = CoroutineScope(job + Dispatchers.Main)

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.homePageFragment)
                    true
                }
                R.id.bottom_ble -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.connectionFragment)
                    true
                }
                R.id.bottom_account -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.accountFragment)
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.setOnItemReselectedListener {
            when (it.itemId) {
                R.id.bottom_home -> {
                    homePageView = null
                }
                R.id.bottom_ble -> {
                    connectionPageView = null
                }
                R.id.bottom_account -> {
                    accountPageView = null
                }
            }
        }

        accountViewModel.loadUser { user ->
            run {
                bleService.onCommand(Commands.NEW_STEP) { info ->
                    if (info == null) {
                        return@onCommand
                    }
                    val coords = Json.decodeFromString(Coordinates.serializer(), info)
                    coroutineScope.launch {
                        ApiServices.stepService.postStep(
                            Step(
                                creatorId = user.id,
                                latitude = coords.latitude,
                                longitude = coords.longitude,
                                location = getString(R.string.new_step)
                            )
                        )
                    }
                }
            }
        }

        pollingJob = coroutineScope.launch {
            blePoller.startRepeatingJob(1000L);
        }
    }

    //  ------------------------------- Permissions --------------------------------

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

    fun requestRelevantRuntimePermissions() {
        if (hasRequiredRuntimePermissions()) {
            return
        }
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                requestLocationPermission()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestBluetoothPermissions()
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
