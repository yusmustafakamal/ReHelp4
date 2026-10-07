package com.rehelp.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.Options
import com.rehelp.app.databinding.FragmentBrowseBinding

// Recipient's list of available food with search and filters (feature 2)
class BrowseFragment : Fragment() {

    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!

    private val allCategories = "All categories"
    private val allAreas = "All areas"

    private lateinit var listAdapter: FoodAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listAdapter = FoodAdapter(emptyList()) { food ->
            startActivity(
                Intent(requireContext(), FoodDetailActivity::class.java).putExtra("foodId", food.foodId)
            )
        }
        binding.rvFoods.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFoods.adapter = listAdapter

        val categories = listOf(allCategories) + Options.categories
        val areas = listOf(allAreas) + Options.areas
        binding.actCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        )
        binding.actArea.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, areas)
        )
        binding.actCategory.setText(allCategories, false)
        binding.actArea.setText(allAreas, false)

        binding.etSearch.doAfterTextChanged { refresh() }
        binding.actCategory.setOnItemClickListener { _, _, _, _ -> refresh() }
        binding.actArea.setOnItemClickListener { _, _, _, _ -> refresh() }
    }

    // Reload when coming back (a reserved item should disappear from the list)
    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        if (_binding == null) return
        val keyword = binding.etSearch.text?.toString().orEmpty()
        val category = binding.actCategory.text?.toString().orEmpty().takeIf { it in Options.categories }
        val area = binding.actArea.text?.toString().orEmpty().takeIf { it in Options.areas }

        // Soonest expiry first (the date text sorts correctly as a string)
        val list = DataRepository(requireContext())
            .searchFoods(keyword, category, area)
            .sortedBy { it.expiryTime }

        listAdapter.update(list)
        binding.tvCount.text = if (list.size == 1) "1 item available" else "${list.size} items available"
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
