package com.riystreak.app.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.riystreak.app.data.model.Streak
import com.riystreak.app.data.repository.StreakRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditUiState(
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val reminderTime: LocalTime? = null,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class AddEditStreakViewModel(
    private val streakId: Long?,
    private val repository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private var existingStreak: Streak? = null

    init {
        if (streakId != null && streakId > 0) {
            viewModelScope.launch {
                val streak = repository.getStreakById(streakId)
                if (streak != null) {
                    existingStreak = streak
                    _uiState.value = AddEditUiState(
                        title = streak.title,
                        description = streak.description ?: "",
                        startDate = streak.startDate,
                        reminderTime = streak.reminderTimeOverride,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title, error = null)
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onReminderTimeChanged(time: LocalTime?) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
    }

    fun saveStreak() {
        val title = _uiState.value.title.trim()
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Title cannot be empty")
            return
        }

        viewModelScope.launch {
            if (existingStreak != null) {
                val updated = existingStreak!!.copy(
                    title = title,
                    description = _uiState.value.description.ifBlank { null },
                    reminderTimeOverride = _uiState.value.reminderTime
                )
                repository.updateStreak(updated)
            } else {
                val newStreak = Streak(
                    title = title,
                    description = _uiState.value.description.ifBlank { null },
                    startDate = _uiState.value.startDate,
                    reminderTimeOverride = _uiState.value.reminderTime
                )
                repository.insertStreak(newStreak)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    class Factory(
        private val streakId: Long?,
        private val repository: StreakRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddEditStreakViewModel(streakId, repository) as T
        }
    }
}
