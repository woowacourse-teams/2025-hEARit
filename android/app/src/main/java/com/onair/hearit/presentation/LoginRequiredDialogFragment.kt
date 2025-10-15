package com.onair.hearit.presentation

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.onair.hearit.databinding.DialogLoginRequiredBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginRequiredDialogFragment(
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
