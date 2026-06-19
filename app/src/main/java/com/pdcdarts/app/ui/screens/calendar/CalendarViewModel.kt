package com.pdcdarts.app.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdcdarts.app.data.model.Tournament
import com.pdcdarts.app.data.repository.DartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CalendarUiState(
    val upcoming: List<Tournament> = emptyList(),
    val completed: List<Tournament> = emptyList(),
    val allCategories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val lastUpdatedMs: Long = 0L,
    val fromCache: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class CalendarViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DartsRepository(app)
    private var allTournaments: List<Tournament> = emptyList()

    private val _state = MutableStateFlow(CalendarUiState(isLoading = true))
    val state: StateFlow<CalendarUiState> = _state

    init { load() }

    fun refresh() = load(forceRefresh = true)

    fun selectCategory(category: String) {
        _state.value = _state.value.copy(selectedCategory = category)
        applyFilter()
    }

    private fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = repo.getCalendar(forceRefresh)
                allTournaments = result.data
                val categories = listOf("All") + result.data.map { it.category }.distinct().sorted()
                _state.value = _state.value.copy(
                    allCategories = categories,
                    lastUpdatedMs = result.lastUpdatedMs,
                    fromCache = result.fromCache,
                    isLoading = false,
                )
                applyFilter()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load calendar",
                )
            }
        }
    }

    private fun applyFilter() {
        val cat = _state.value.selectedCategory
        val filtered = if (cat == "All") allTournaments
        else allTournaments.filter { it.category.equals(cat, ignoreCase = true) }

        val upcoming = filtered.filter { it.status.lowercase() != "completed" }
        val completed = filtered.filter { it.status.lowercase() == "completed" }
        _state.value = _state.value.copy(upcoming = upcoming, completed = completed)
    }
}
