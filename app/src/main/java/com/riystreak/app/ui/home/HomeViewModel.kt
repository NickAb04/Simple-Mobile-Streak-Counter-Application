package com.riystreak.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.riystreak.app.data.model.Streak
import com.riystreak.app.data.repository.StreakRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: StreakRepository) : ViewModel() {

    val activeStreaks: StateFlow<List<Streak>> = repository.activeStreaksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markCompleted(streakId: Long) {
        viewModelScope.launch {
            repository.markCompleted(streakId, LocalDate.now())
        }
    }

    fun archiveStreak(streakId: Long) {
        viewModelScope.launch {
            repository.archiveStreak(streakId, true)
        }
    }

    fun deleteStreak(streakId: Long) {
        viewModelScope.launch {
            repository.deleteStreak(streakId)
        }
    }

    class Factory(private val repository: StreakRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
