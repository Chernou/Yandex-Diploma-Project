package ru.practicum.android.diploma.feature.search.domain.models

data class VacancyShort(
    val id: String,
    val area: Area,
    val employer: Employer?,
    val name: String,
    val salary: Salary?,
)
