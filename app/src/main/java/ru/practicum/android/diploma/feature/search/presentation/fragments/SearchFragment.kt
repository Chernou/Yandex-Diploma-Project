package ru.practicum.android.diploma.feature.search.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.feature.search.presentation.viewmodels.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.core.util.DataTransmitter
import ru.practicum.android.diploma.feature.search.domain.VacanciesResponse
import ru.practicum.android.diploma.feature.search.domain.models.VacancyShort
import ru.practicum.android.diploma.feature.search.presentation.SearchState
import ru.practicum.android.diploma.feature.search.searchadapter.SlideInBottomAnimator
import ru.practicum.android.diploma.feature.search.searchadapter.VacanciesAdapter

class SearchFragment : Fragment(), VacanciesAdapter.ClickListener {

    private val viewModel: SearchViewModel by viewModel()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var vacanciesAdapter: VacanciesAdapter? = null

    private var lastVisibleItemPosition: Int = 0

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

            if (!viewModel.isLoading) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && !viewModel.isLastPage()
                ) {
                    viewModel.loadNextPage()

                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterButtonImageView.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_settingsFiltersFragment)
        }

        vacanciesAdapter = VacanciesAdapter(this)
        binding.searchRecycler.adapter = vacanciesAdapter

        viewModel.stateLiveData.observe(viewLifecycleOwner) {
            render(it)
            showVacanciesNumber(it)
        }

        binding.searchInputEditText.doOnTextChanged { text, _, _, _ ->
            vacanciesAdapter?.clear()
            clearButtonVisibility(text)
            text?.let {
                viewModel.searchDebounce(it.toString())
                binding.amountTextView.visibility = View.GONE
                binding.searchPlaceholderImageView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                if (text.isBlank()) {
                    binding.progressBar.visibility = View.GONE
                    binding.searchPlaceholderImageView.visibility = View.VISIBLE
                    binding.amountTextView.visibility = View.GONE
                }
            }
        }

        binding.clearSearchImageView.setOnClickListener {
            clearSearch()
        }

        binding.searchRecycler.addOnScrollListener(onScrollListener)
    }

    private fun render(state: SearchState) {
        when (state) {
            is SearchState.Loading -> showLoading()
            is SearchState.Content -> showContent(state.response)
            is SearchState.Error -> showError()
            is SearchState.Empty -> showEmpty()
            is SearchState.ClearScreen -> showClearScreen()
        }
    }

    private fun showLoading() {
        if (viewModel.isFirstLoad) {
            clearContent()
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showError() {
        clearContent()
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showEmpty() {
        clearContent()
        binding.amountTextView.visibility = View.VISIBLE
        binding.amountTextView.text = "Таких вакансий нет"
    }

    private fun showContent(response: VacanciesResponse) {
        clearContent()
        binding.amountTextView.visibility = View.VISIBLE
        binding.amountTextView.text = response.found.toString()
        vacanciesAdapter?.setVacancyList(response.items)
        binding.searchRecycler.visibility = View.VISIBLE
        val itemAnimator = SlideInBottomAnimator()
        binding.searchRecycler.itemAnimator = itemAnimator
    }

    private fun showClearScreen() {
        clearContent()
        binding.amountTextView.visibility = View.GONE
        binding.searchPlaceholderImageView.visibility = View.VISIBLE
    }

    private fun clearContent() {
        binding.searchPlaceholderImageView.visibility = View.GONE
        binding.searchRecycler.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.amountTextView.visibility = View.GONE
    }

    private fun clearButtonVisibility(s: CharSequence?) {
        if (s.isNullOrEmpty()) {
            binding.searchImageView.visibility = View.VISIBLE
            binding.clearSearchImageView.visibility = View.GONE
        } else {
            binding.searchImageView.visibility = View.GONE
            binding.clearSearchImageView.visibility = View.VISIBLE
        }
    }

    private fun clearSearch() {
        binding.searchInputEditText.setText("")
        binding.searchInputEditText.clearFocus()
        clearContent()
        binding.searchPlaceholderImageView.visibility = View.VISIBLE
    }

    private fun showVacanciesNumber(searchState: SearchState) {
        if (searchState is SearchState.Content) {
            binding.amountTextView.text = requireContext().resources.getQuantityString(
                R.plurals.plural_vacancies, searchState.response.found, searchState.response.found
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        vacanciesAdapter = null
    }

    override fun onResume() {
        super.onResume()
        showClearScreen()
    }

    override fun onClick(vacancy: VacancyShort) {
        DataTransmitter.postId(vacancy.id)
        findNavController().navigate(R.id.action_searchFragment_to_vacancyFragment)
    }
}