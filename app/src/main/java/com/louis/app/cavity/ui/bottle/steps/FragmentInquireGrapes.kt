package com.louis.app.cavity.ui.bottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireGrapesBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.ui.bottle.AddBottleViewModel
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.showSnackbar

class FragmentInquireGrapes : Fragment(R.layout.fragment_inquire_grapes) {
    private lateinit var binding: FragmentInquireGrapesBinding
    private lateinit var grapeAdapter: GrapeRecyclerAdapter
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireGrapesBinding.bind(view)

        initRecyclerView()
        observe()
        setListener()
    }

    private fun initRecyclerView() {
        grapeAdapter = GrapeRecyclerAdapter(
            { addBottleViewModel.removeGrape(it) },
            { addBottleViewModel.updateGrape(it) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = grapeAdapter
        }

        addBottleViewModel.getAllGrapes().observe(viewLifecycleOwner) {
            grapeAdapter.submitList(it)
        }
    }

    private fun observe() {
        addBottleViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }
    }

    private fun setListener() {
        binding.buttonAddGrape.setOnClickListener {
            val grapeName = binding.grapeName.text.toString()

            if (grapeName.isEmpty()) {
                binding.coordinator.showSnackbar(R.string.empty_grape_name)
                return@setOnClickListener
            }

            val defaultPercentage = if (grapeAdapter.currentList.size >= 1) 0 else 25
            addBottleViewModel.addGrape(Grape(0, grapeName, defaultPercentage, 0))
        }
    }
}
