package com.louis.app.cavity.ui.bottle.stepper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.util.L

class StepperViewModel(app: Application) : AndroidViewModel(app) {
    private val finalStep = 3

    private var _lastValidStep = 0
    val lastValidStep: Int
        get() = _lastValidStep

    private val _step = MutableLiveData(0 to false)
    val step: LiveData<Pair<Int, Boolean>>
        get() = _step

    fun goToNextStep(): Boolean {
        L.v("goToNextStep")
        val currentStep = _step.value?.first ?: 0
        return if (currentStep + 1 <= finalStep) {
            val wasLookingBehind = currentStep < _lastValidStep
            _step.postValue(currentStep + 1 to wasLookingBehind)

            if (!wasLookingBehind) _lastValidStep = currentStep + 1

            false
        } else {
            true
        }
    }

    fun goToPreviousStep() {
        val currentStep = _step.value?.first ?: 0
        if (currentStep > 0) _step.postValue(currentStep - 1 to true)
    }

    fun goToStep(step: Int) {
        val isLookingBehind = _lastValidStep > step

        if (step in 0.._lastValidStep) _step.postValue(step to isLookingBehind)
        else _step.postValue(step - 1 to isLookingBehind)
    }

    fun reset() {
        _lastValidStep = 0
        _step.postValue(0 to false)
    }
}