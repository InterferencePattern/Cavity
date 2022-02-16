package com.louis.app.cavity.ui.bottle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class BottleDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val bottleId = MutableLiveData<Long>()

    private val _pdfEvent = MutableLiveData<Event<Uri>>()
    val pdfEvent: LiveData<Event<Uri>>
        get() = _pdfEvent

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    val bottle = bottleId.switchMap { repository.getBottleById(it) }

    val grapes = bottleId.switchMap { repository.getQGrapesAndGrapeForBottle(it) }

    val reviews = bottleId.switchMap { repository.getFReviewAndReviewForBottle(it) }

    fun getWineById(wineId: Long) = repository.getWineById(wineId)

    fun getBottlesForWine(wineId: Long) = repository.getBottlesForWine(wineId)

    fun setBottleId(bottleId: Long) {
        this.bottleId.value = bottleId
    }

    fun getBottleId() = this.bottleId.value

    fun deleteBottle() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            repository.deleteBottleById(bottleId)
        }
    }

    fun toggleFavorite() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            repository.run {
                val bottle = getBottleByIdNotLive(bottleId)
                if (bottle.isFavorite.toBoolean()) unfav(bottleId) else fav(bottleId)
            }
        }
    }

    fun preparePdf() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            val bottle = repository.getBottleByIdNotLive(bottleId)
            val path = bottle.pdfPath

            if (path.isNotBlank()) {
                _pdfEvent.postOnce(Uri.parse(path))
            } else {
                _userFeedback.postOnce(R.string.no_pdf)
            }
        }
    }

    fun revertBottleConsumption() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            repository.revertBottleConsumption(bottleId)
        }
    }

    fun removeBottleFromTasting() {
        val bottleId = bottleId.value ?: return

        viewModelScope.launch(IO) {
            val bottle = repository.getBottleByIdNotLive(bottleId)
            bottle.tastingId = null
            repository.updateBottle(bottle)
        }
    }
}
