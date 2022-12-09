package com.example.bykeandroid.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.bykeandroid.R

class MyDialog(private val message : String, private val onPositive : (()->Unit)?, private val onNegative : (()->Unit)?) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(message)

            if (onPositive != null) {
                builder.setPositiveButton(
                    R.string.ok
                ) { _, _ ->
                    onPositive.invoke()
                }
            }

            if (onNegative != null) {
                builder.setNegativeButton(
                    R.string.cancel
                ) { _, _ ->
                    onNegative.invoke()
                }
            }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}