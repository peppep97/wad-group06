package com.group06.lab.utils


import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class RateDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val alertBuilder = AlertDialog.Builder(it)

            alertBuilder.setTitle("Are you sure ?")
            alertBuilder.setMessage("If you want to delete the file, please click on OK.")

            alertBuilder.setPositiveButton("OK", DialogInterface.OnClickListener{dialog, id ->
                Log.d("mydialoglog", "OK Pressed")
            }).setNegativeButton("Cancel", DialogInterface.OnClickListener{dialog, id ->
                Log.d("mydialoglog", "Cancel Pressed")
            }).setNeutralButton("Dismiss", DialogInterface.OnClickListener{dialog, id ->
                Log.d("mydialoglog", "Dismiss Pressed")
            })
            alertBuilder.create()
        } ?: throw IllegalStateException("Exception !! Activity is null !!")
    }
}