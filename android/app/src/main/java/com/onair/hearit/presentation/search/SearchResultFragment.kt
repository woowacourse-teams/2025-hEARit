package com.onair.hearit.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.onair.hearit.data.dummy.SearchedHearitDummyData
import com.onair.hearit.databinding.FragmentSearchResultBinding

class SearchResultFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy { SearchedHearitAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        binding.rvSearchedHearit.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // 테스트용으로 더미 데이터 넣어 놓음
        adapter.submitList(SearchedHearitDummyData.searchedItems)
    }
}
