package com.onair.hearit.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.onair.hearit.databinding.BottomSheetBookmarkOptionBinding

class BookmarkOptionBottomSheet : BottomSheetDialogFragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: BottomSheetBookmarkOptionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = BottomSheetBookmarkOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvBookmarkOptionDeleteBookmark.setOnClickListener {
            // 북마크 삭제 기능이 들어가야 함
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
