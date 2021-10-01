package com.louis.app.cavity.ui.tasting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentTastingOverviewBinding
import com.louis.app.cavity.databinding.FragmentTastingsBinding
import com.louis.app.cavity.util.setupNavigation

class FragmentTastingOverview: Fragment(R.layout.fragment_tasting_overview) {
    private var _binding: FragmentTastingOverviewBinding? = null
    private val binding get() = _binding!!
    private val tastingOverviewViewModel: TastingOverviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTastingOverviewBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)
    }
}
