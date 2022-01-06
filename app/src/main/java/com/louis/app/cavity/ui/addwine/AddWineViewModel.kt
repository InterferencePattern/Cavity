package com.louis.app.cavity.ui.addwine

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.WineColor
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddWineViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _wineUpdatedEvent = MutableLiveData<Event<Int>>()
    val wineUpdatedEvent: LiveData<Event<Int>>
        get() = _wineUpdatedEvent

    private val _updatedWine = MutableLiveData<Wine>()
    val updatedWine: LiveData<Wine>
        get() = _updatedWine

    private val _image = MutableLiveData<String>()
    val image: LiveData<String>
        get() = _image

    private val _countyId = MutableLiveData<Long>()

    private val isEditMode: Boolean
        get() = wineId != 0L

    val namings = _countyId.switchMap { repository.getNamingsForCounty(it) }

    private var wineId = 0L

    fun start(wineId: Long) {
        this.wineId = wineId

        if (wineId != 0L) {
            viewModelScope.launch(IO) {
                val wine = repository.getWineByIdNotLive(wineId)

                _countyId.postValue(wine.countyId)
                _updatedWine.postValue(wine)
                _image.postValue(wine.imgPath)
            }
        }
    }

    fun getAllCounties() = repository.getAllCounties()

    fun saveWine(
        name: String,
        naming: String,
        cuvee: String,
        isOrganic: Int,
        colorChipId: Int,
        county: County?
    ) {
        if (county == null) {
            _userFeedback.postOnce(R.string.no_county)
            return
        }

        val color = when (colorChipId) {
            R.id.colorRed -> WineColor.RED
            R.id.colorWhite -> WineColor.WHITE
            R.id.colorSweet -> WineColor.SWEET
            else /* R.id.colorRose */ -> WineColor.ROSE
        }

        val wine = Wine(
            wineId,
            name,
            naming,
            color,
            cuvee,
            isOrganic,
            _image.value ?: "",
            hidden = false.toInt(),
            county.id
        )

        viewModelScope.launch(IO) {
            if (isEditMode) {
                repository.updateWine(wine)
                _wineUpdatedEvent.postOnce(R.string.wine_updated)
            } else {
                repository.insertWine(wine)
                _wineUpdatedEvent.postOnce(R.string.wine_added)
            }

            reset()
        }

    }

    fun setImage(imagePath: String) {
        _image.postValue(imagePath)
    }

    fun setCountyId(countyId: Long?) {
        countyId?.let {
            _countyId.postValue(it)
        }
    }

    private fun reset() {
        wineId = 0
    }
}
