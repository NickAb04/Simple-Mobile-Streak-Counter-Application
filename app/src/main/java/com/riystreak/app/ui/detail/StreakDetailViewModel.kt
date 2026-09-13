package com.riystreak.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.riystreak.app.data.model.DayLog
import com.riystreak.app.data.model.Streak
import com.riystreak.app.data.repository.StreakRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StreakDetailViewModel(
    private val streakId: Long,
    private val repository: StreakRepository
) : ViewModel() {

    val streak: StateFlow<Streak?> = repository.getStreakByIdFlow(streakId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val logs: StateFlow<List<DayLog>> = repository.getLogsForStreakFlow(streakId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markCompleted() {
        viewModelScope.launch {
            repository.markCompleted(streakId, LocalDate.now())
        }
    }

    fun archiveStreak() {
        viewModelScope.launch {
            repository.archiveStreak(streakId, true)
        }
    }

    fun deleteStreak(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteStreak(streakId)
            onDeleted()
        }
    }

    class Factory(
        private val streakId: Long,
        private val repository: StreakRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StreakDetailViewModel(streakId, repository) as T
        }
    }
}
