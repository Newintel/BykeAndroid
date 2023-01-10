package com.example.bykeandroid.view

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.valueIterator
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.bykeandroid.R
import com.example.bykeandroid.ble.BleService
import com.example.bykeandroid.data.Commands
import com.example.bykeandroid.data.Coordinates
import com.example.bykeandroid.databinding.FragmentConnectionBinding
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.serialization.json.Json

class ConnectionFragment : Fragment() {
    private lateinit var detector: BarcodeDetector
    private lateinit var binding: FragmentConnectionBinding
    private lateinit var activity: MainActivity
    private lateinit var bleService: BleService

    // ------------------------------- Lifecycle --------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildQrDetector()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connection, container, false)

        binding.lifecycleOwner = this

        activity = requireActivity() as MainActivity
        activity.bottomNavigationView.isVisible = true
        
        bleService = activity.bleService
            .onDeviceFound {
                Log.i("BLE", "Device found")
                binding.qrTv.text = "Device found"
            }
            .onDeviceConnected {
                Log.i("BLE", "Device connected")
                binding.qrTv.text = "Device connected"
            }

        binding.btnQr.setOnClickListener { startCamera() }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (bleService.isBluetoothEnabled() == false) {
            promptEnableBluetooth()
        }
    }

    // ------------------------------- QR Code --------------------------------

    private fun buildQrDetector() {
        detector = BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()
    }

    private fun startCamera() {
        dispatchTakePictureIntent()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                resultLauncher.launch(takePictureIntent)
            }
        }
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // On laisse la version dépréciée en cas d'utilisation d'une version d'android inférieure à 33
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                detectQrCode(imageBitmap)
            }
        }

    private fun isMacAddressValid(macAddress: String): Boolean {
        val regex = Regex("([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})")
        return regex.matches(macAddress)
    }

    private fun detectQrCode(image: Bitmap) {
        if (detector.isOperational == false) {
            binding.qrTv.text = "Could not set up the detector!"
            return
        }
        val frame: Frame = Frame.Builder().setBitmap(image).build()
        val barcodes: SparseArray<Barcode> = detector.detect(frame)
        var macAdress: String? = null
        barcodes.valueIterator().forEach { barcode ->
            val rawValue = barcode.rawValue
            if (isMacAddressValid(rawValue)) {
                macAdress = rawValue
            }
        }

        if (macAdress != null) {
            binding.qrTv.text = getString(R.string.ble_scan_start)
            bleService.setDeviceMac(macAdress)
            startBleScan()
        } else {
            binding.qrTv.text = getString(R.string.no_qr_code_found)
        }
    }

    // ------------------------------- BLUETOOTH -------------------------------

    private fun promptEnableBluetooth() {
        if (bleService.isBluetoothEnabled() == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            btIntentEnable.launch(enableBtIntent)
        }
    }

    val btIntentEnable =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                promptEnableBluetooth()
            }
        }

    fun startBleScan() {
        if (activity.hasRequiredRuntimePermissions() == false) {
            activity.requestRelevantRuntimePermissions()
        } else {
            bleService.startBleScan()
        }
    }
}