package ru.practicum.android.diploma.feature.filter.presentation.states

import ru.practicum.android.diploma.feature.filter.domain.model.Industry

sealed interface IndustriesState {
    class Error(val errorText: String): IndustriesState
    class DisplayIndustries(val industries: ArrayList<Industry>): IndustriesState
}