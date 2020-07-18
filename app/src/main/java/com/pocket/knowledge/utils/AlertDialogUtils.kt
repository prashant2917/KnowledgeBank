package com.pocket.knowledge.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

class AlertDialogUtils{
    companion object{
        fun geTwoButtonDialog(context: Context,title:String,message:String,positiveBtn:String,negativeBtn:String,alertDialogCallback: AlertDialogCallback):AlertDialog.Builder {
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.setPositiveButton(positiveBtn) { dialogInterface, i -> dialogInterface.dismiss()
            alertDialogCallback.onPositiveButtonClick()
            }
            dialog.setNegativeButton(negativeBtn) { dialogInterface, i ->
                dialogInterface.dismiss()
                alertDialogCallback.onNegativeButtonClick()

            }
            return dialog;

        }
    }
}