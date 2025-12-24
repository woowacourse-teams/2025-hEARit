package com.onair.hearit.presentation.search.category

import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryComposeFragment : Fragment() {
//    private val category by lazy {
//        SearchInput.Category(
//            arguments?.getLong(CATEGORY_ID_KEY) ?: -1L,
//            arguments?.getString(CATEGORY_NAME_KEY) ?: "카테고리",
//            arguments?.getString(CATEGORY_COLOR_KEY) ?: "#000000",
//        )
//    }
//
//    private val mainViewModel: MainViewModel by activityViewModels()
//
//    private val viewModel: SearchViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        viewModel.setSearchInput(category)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//    ): View =
//        ComposeView(requireContext()).apply {
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//            setContent {
//                CategorySearchScreen(
//                    viewModel = viewModel,
//                    mainViewModel = mainViewModel,
//                    onBack = { parentFragmentManager.popBackStack() },
//                    onHearitClick = { heartId -> onHearitClick(heartId) },
//                    modifier = Modifier.fillMaxSize(),
//                )
//            }
//        }
//
//    private fun onHearitClick(hearitId: Long) {
//        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
//        (activity as? MainActivity)?.launchDetailActivity(intent)
//    }
}
