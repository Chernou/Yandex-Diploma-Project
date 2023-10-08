package ru.practicum.android.diploma.feature.filter.presentation.states

import ru.practicum.android.diploma.feature.filter.domain.model.Country

sealed interface CountriesState {
    class Error(val errorText: String): CountriesState
    class DisplayCountries(val countries: ArrayList<Country>): CountriesState
}