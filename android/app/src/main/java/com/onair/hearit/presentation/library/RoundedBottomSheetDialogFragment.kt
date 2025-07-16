package com.onair.hearit.presentation.library

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.onair.hearit.R

open class RoundedBottomSheetDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.background =
                MaterialShapeDrawable(
                    ShapeAppearanceModel
                        .builder(
                            requireContext(),
                            0,
                            com.google.android.material.R.style.ShapeAppearance_Material3_Corner_Large,
                        ).build(),
                ).apply {
                    fillColor = requireContext().getColorStateList(R.color.hearit_gray1)
                    elevation = 8f
                }

            dialog.window?.setDimAmount(0.3f)
        }

        return dialog
    }
}
