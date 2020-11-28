package com.louis.app.cavity.ui.addbottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddGrapeBinding
import com.louis.app.cavity.databinding.FragmentInquireGrapesBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentInquireGrapes : Fragment(R.layout.fragment_inquire_grapes){
    private lateinit var quantifiedGrapeAdapter: QuantifiedGrapeRecyclerAdapter
    private var _binding: FragmentInquireGrapesBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()
    private val grapeViewModel: GrapeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireGrapesBinding.bind(view)

        initRecyclerView()
        setListeners()
    }

    private fun initRecyclerView() {
        lifecycleScope.launch(IO) {
            quantifiedGrapeAdapter = QuantifiedGrapeRecyclerAdapter(
                onDeleteListener = { grapeViewModel.removeQuantifiedGrape(it) },
                onValueChangeListener = { qGrape, newValue ->
                    grapeViewModel.updateQuantifiedGrape(qGrape, newValue)
                },
            )

            withContext(Main) {
                binding.recyclerView.apply {
                    layoutManager = LinearLayoutManager(activity)
                    setHasFixedSize(true)
                    adapter = quantifiedGrapeAdapter
                }
            }
        }


        grapeViewModel.getQGrapesForBottle(0).observe(viewLifecycleOwner) {
            toggleRvPlaceholder(it.isEmpty())
            quantifiedGrapeAdapter.submitList(it)
        }

        grapeViewModel.getAllGrapes().observe(viewLifecycleOwner) {
            quantifiedGrapeAdapter.grapes = it
        }
    }

    private fun setListeners() {
        binding.buttonAddGrape.setOnClickListener { showDialog() }

        binding.buttonCreateGrape.setOnClickListener {
            grapeViewModel.addGrape(Grape(grapeId = 0, name = binding.grapeName.text.toString()))
        }
    }

    private fun addGrape(grapeName: String) {
        if (grapeName == resources.getString(R.string.grape_other)) {
            binding.coordinator.showSnackbar(R.string.reserved_name)
            return
        }

        grapeViewModel.addGrape(Grape(0, grapeName))
    }

    private fun toggleRvPlaceholder(toggle: Boolean) {
        with(binding) {
            grapeIconEmpty.setVisible(toggle)
            explanation.setVisible(toggle)
            buttonAddGrapeSecondary.setVisible(toggle)
        }
    }

    private fun showDialog() {
        val dialogBinding = DialogAddGrapeBinding.inflate(layoutInflater)

        lifecycleScope.launch(IO) {
            val grapes = grapeViewModel.getAllGrapesNotLive()
            val grapesName = grapes.map { it.name }.toTypedArray()
            val checked = grapes.map { false }.toBooleanArray()

            withContext(Main) {
                MaterialAlertDialogBuilder(requireContext())
                    .setCancelable(false)
                    .setTitle(R.string.add_grapes)
                    .setMultiChoiceItems(grapesName, checked) { a, pos, checked ->
                        if (checked) {
                            grapeViewModel.addQuantifiedGrape(1, grapes[pos].grapeId)
                        } else {
                            //grapeViewModel.removeQuantifiedGrape()
                        }
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                    }
                    .setPositiveButton(R.string.submit) { _, _ ->
                        addGrape(dialogBinding.grapeName.text.toString())
                    }
                    .show()
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
