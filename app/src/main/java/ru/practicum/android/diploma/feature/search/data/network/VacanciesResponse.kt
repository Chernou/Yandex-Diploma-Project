package ru.practicum.android.diploma.feature.search.data.network

import com.google.gson.annotations.SerializedName
import com.usunin1994.headhunterapi.data.dtomodels.VacancyDtoShort

data class VacanciesResponse (val items: List<VacancyDtoShort>,
                            val found: Int,
                            val pages: Int,
                            @SerializedName("per_page")
                            val perPage: Int,
                            val page: Int)
