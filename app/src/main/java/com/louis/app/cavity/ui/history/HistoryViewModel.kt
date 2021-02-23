package com.louis.app.cavity.ui.history

import android.app.Application
import androidx.annotation.IdRes
import androidx.lifecycle.*
import androidx.paging.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.HistoryEntryType
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    private val _scrollTo = MutableLiveData<Event<Int>>()
    val scrollTo: LiveData<Event<Int>>
        get() = _scrollTo

    // TODO: consider removing public part if not needed
    private val _filter = MutableLiveData<Int?>(null)
    val filter: LiveData<Int?>
        get() = _filter

    var bottleId = -1L

    val entries: LiveData<PagingData<HistoryUiModel>> = filter.switchMap {
        Pager(PagingConfig(pageSize = 100, prefetchDistance = 10, enablePlaceholders = true)) {
            when (it) {
                R.id.chipReplenishments -> repository.getEntriesByType(1, 3)
                R.id.chipComsumptions -> repository.getEntriesByType(0, 2)
                R.id.chipTastings -> repository.getEntriesByType(4, 4)
                R.id.chipGiftedTo -> repository.getEntriesByType(2, 2)
                R.id.chipGiftedBy -> repository.getEntriesByType(3, 3)
                R.id.chipFavorites -> repository.getFavoriteEntries()
                else -> repository.getAllEntries()
            }
        }.liveData.map { pagingData ->
            pagingData
                .filter { if (bottleId != -1L) it.bottleAndWine.bottle.id == bottleId else true }
                .map { HistoryUiModel.EntryModel(it) }
                .insertSeparators { before, after ->
                    if (shouldSeparate(before, after))
                        HistoryUiModel.HeaderModel(after?.model?.historyEntry?.date ?: 0L)
                    else null
                }
        }.cachedIn(viewModelScope)
    }

    fun requestScrollToDate(timestamp: Long) {
        viewModelScope.launch(IO) {
            val entries = repository.getAllEntriesNotPagedNotLive()
            val offset = 1

            withContext(Default) {
                var headerCount = 0
                var currentDay = -1L

                for ((position, entry) in entries.withIndex()) {
                    val day = DateFormatter.roundToDay(entry.date)

                    if (day != currentDay) {
                        currentDay = day
                        headerCount++
                    }

                    if (entry.date <= DateFormatter.roundToDay(timestamp)) {
                        _scrollTo.postOnce(position + headerCount - offset)
                        break
                    }

                    // No date found, scroll to bottom
                    _scrollTo.postOnce(position + headerCount)
                }
            }
        }
    }

    fun setFilter(@IdRes chipId: Int?) {
        _filter.postValue(chipId)
    }

    private fun shouldSeparate(
        before: HistoryUiModel.EntryModel?,
        after: HistoryUiModel?
    ): Boolean {
        if (before == null && after == null) {
            return false
        }

        return if (after is HistoryUiModel.EntryModel?) {
            val beforeTimestamp =
                DateFormatter.roundToDay(before?.model?.historyEntry?.date ?: return true)
            val afterTimestamp =
                DateFormatter.roundToDay(after?.model?.historyEntry?.date ?: return false)

            beforeTimestamp != afterTimestamp
        } else false
    }
}
