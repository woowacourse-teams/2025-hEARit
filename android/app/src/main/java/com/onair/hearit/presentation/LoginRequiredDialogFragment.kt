package com.onair.hearit.presentation

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.onair.hearit.databinding.DialogLoginRequiredBinding

class LoginRequiredDialogFragment(
    @StringRes private val messageRes: Int,
    private val onPositive: () -> Unit,
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogLoginRequiredBinding.inflate(layoutInflater)

        val dialog =
            AlertDialog
                .Builder(requireContext())
                .setView(binding.root)
                .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        binding.tvDialogMessage.setText(messageRes)

        binding.tvDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.tvDialogLoginPositive.setOnClickListener {
            dialog.dismiss()
            onPositive()
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
    }
}
