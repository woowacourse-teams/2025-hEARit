package com.onair.hearit.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.onair.hearit.databinding.BottomSheetBookmarkOptionBinding

class BookmarkOptionBottomSheet : BottomSheetDialogFragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: BottomSheetBookmarkOptionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by viewModels({ requireParentFragment() })
    private val bookmarkId: Long by lazy {
        requireArguments().getLong(BOOKMARK_KEY)
    }

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

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarkList ->
            val targetBookmark = bookmarkList.find { it.bookmarkId == bookmarkId }
            if (targetBookmark != null) {
                binding.bookmark = targetBookmark
            }
        }
        binding.tvBookmarkOptionDeleteBookmark.setOnClickListener {
            viewModel.deleteBookmark(bookmarkId)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val BOOKMARK_KEY = "bookmark_key"

        fun newInstance(bookmarkId: Long): BookmarkOptionBottomSheet =
            BookmarkOptionBottomSheet().apply {
                arguments =
                    Bundle().apply {
                        putLong(BOOKMARK_KEY, bookmarkId)
                    }
            }
    }
}
