package ru.practicum.android.diploma.feature.search.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.feature.search.presentation.viewmodels.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.core.util.DataTransmitter
import ru.practicum.android.diploma.feature.search.domain.VacanciesResponse
import ru.practicum.android.diploma.feature.search.domain.models.VacancyShort
import ru.practicum.android.diploma.feature.search.presentation.VacanciesSearchState
import ru.practicum.android.diploma.feature.search.searchadapter.VacanciesAdapter

class SearchFragment : Fragment(), VacanciesAdapter.ClickListener {

    private val viewModel: SearchViewModel by viewModel()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var vacanciesAdapter: VacanciesAdapter? = null

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
            clearButtonVisibility(text)
            text?.let { viewModel.searchDebounce(it.toString()) }
        }

        binding.clearSearchImageView.setOnClickListener {
            clearSearch()
        }
    }

    private fun render(state: VacanciesSearchState) {
        when (state) {
            is VacanciesSearchState.Loading -> showLoading()
            is VacanciesSearchState.Content -> showContent(state.response)
            is VacanciesSearchState.Error -> showError()
            is VacanciesSearchState.Empty -> showEmpty()
            is VacanciesSearchState.ClearScreen -> showClearScreen()
        }
    }

    private fun showLoading() {
        clearContent()
        binding.progressBar.visibility = View.VISIBLE
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
        vacanciesAdapter?.vacancies = response.items
        binding.searchRecycler.visibility = View.VISIBLE
        binding.amountTextView.visibility = View.VISIBLE
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
        if (s.isNullOrEmpty()){
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

    private fun showVacanciesNumber(vacanciesSearchState: VacanciesSearchState) {
        if (vacanciesSearchState is VacanciesSearchState.Content) {
            binding.amountTextView.text = requireContext().resources.getQuantityString(
                R.plurals.plural_vacancies, vacanciesSearchState.response.found, vacanciesSearchState.response.found
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        vacanciesAdapter = null
    }

    override fun onClick(vacancy: VacancyShort) {
        DataTransmitter.postId(vacancy.id)
        findNavController().navigate(R.id.action_searchFragment_to_vacancyFragment)
    }
}