package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentWinesBinding
import com.louis.app.cavity.util.toBoolean

class FragmentWines : Fragment(R.layout.fragment_wines) {
    private var _binding: FragmentWinesBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWinesBinding.bind(view)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val wineAdapter = WineRecyclerAdapter(
            requireContext(),
            onClickListener = { wineId: Long, bottleId: Long ->
                val action = FragmentHomeDirections.homeToBottleDetails(wineId, bottleId)
                //findNavController().navigate(action)
            },
            onShowOptionsListener = { wine ->
                val action = FragmentHomeDirections.homeToWineOptions(
                    wine.id,
                    wine.countyId,
                    wine.name,
                    wine.naming,
                    wine.isOrganic.toBoolean(),
                    wine.color
                )
                //findNavController().navigate(action)
            }
        )

        binding.recyclerView.apply {
            layoutManager = HoneycombLayoutManager(
                colCount = 2,
                HoneycombLayoutManager.Orientation.VERTICAL
            )

            // Appear to be causing memory leaks.
            setRecycledViewPool((parentFragment as FragmentHome).getRecycledViewPool())
            setHasFixedSize(true)
            adapter = wineAdapter
        }

        val countyId = arguments?.getLong(COUNTY_ID)

        homeViewModel.getWinesWithBottlesByCounty(countyId ?: 0).observe(viewLifecycleOwner) {
            wineAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val COUNTY_ID = "com.louis.app.cavity.ui.home.FragmentWines.COUNTY_ID"

        // Used by WinesPagerAdapter
        fun newInstance(countyId: Long): FragmentWines {
            return FragmentWines().apply {
                arguments = bundleOf(COUNTY_ID to countyId)
            }
        }
    }
}
