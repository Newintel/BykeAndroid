package com.example.bykeandroid.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.SparseArray
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.util.valueIterator
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.bykeandroid.R
import com.example.bykeandroid.databinding.FragmentConnectionBinding
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class ConnectionFragment : Fragment() {
    private lateinit var detector: BarcodeDetector
    private lateinit var binding: FragmentConnectionBinding
    private lateinit var activity : MainActivity

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildQrDetector()
    }

    private fun buildQrDetector() {
        detector = BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()
    }

    fun startCamera(v: View) {
        dispatchTakePictureIntent()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            detectQrCode(imageBitmap)
        }
    }

    private fun detectQrCode(image: Bitmap) {
        if(detector.isOperational == false) {
            binding.qrTv.text = "Could not set up the detector!"
            return
        }
        val frame: Frame = Frame.Builder().setBitmap(image).build()
        val barcodes: SparseArray<Barcode> = detector.detect(frame)
        var macAdress : String? = null
        barcodes.valueIterator().forEach { barcode ->
            val rawValue = barcode.rawValue
            if (rawValue.length == 17 && rawValue.count() {
                    it == ':'
                } == 5) {
                macAdress = rawValue
            }
        }

        if (macAdress != null) {
            binding.qrTv.text = macAdress
            activity.macAdress = macAdress
            activity.startBleScan()
        } else {
            binding.qrTv.text = getString(R.string.no_qr_code_found)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connection, container, false)

        binding.lifecycleOwner = this

        activity = requireActivity() as MainActivity

        binding.btnQr.setOnClickListener { startCamera(it) }

        // Inflate the layout for this fragment
        return binding.root
    }
}