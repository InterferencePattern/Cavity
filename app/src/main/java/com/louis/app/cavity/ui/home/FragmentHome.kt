package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.model.Wine

class FragmentHome : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()
        setupScrollableTab()
        setHasOptionsMenu(true)
    }

    private fun setupRecyclerView() {
        val wineAdapter = WineRecyclerViewAdapter(object : OnVintageClickListener {
            override fun onVintageClick(wine: Wine) {
                TODO()
            }
        })
        wineAdapter.setHasStableIds(true)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            setItemViewCacheSize(10)
            adapter = wineAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    // Show components no matter what if RV can't be scrolled
                    if (
                        !recyclerView.canScrollVertically(1) &&
                        !recyclerView.canScrollVertically(-1)
                    ) binding.fab.show()
                    else if (dy > 0 && binding.fab.isShown) binding.fab.hide()
                    else if (dy < 0 && !binding.fab.isShown) binding.fab.show()
                }
            })
        }

        homeViewModel.getAllWines().observe(viewLifecycleOwner) {
            wineAdapter.submitList(it)
        }
    }

    private fun setupScrollableTab() {
        binding.tab.addTabs(
            listOf(
                "Alsace", "Beaujolais", "Bourgogne", "Bordeaux", "Italie", "Suisse",
                "Langudoc-Roussillon", "Jura"
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.switchView -> TODO("Change item view type in RecyclerView")
        }

        return super.onOptionsItemSelected(item)
    }
}
