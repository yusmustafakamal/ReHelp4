package com.rehelp.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.FragmentTasksBinding

// Volunteer's task list: "Open" tasks to accept, and "Mine" (accepted, still active)
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PickupAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = PickupAdapter(emptyList()) { d ->
            startActivity(
                Intent(requireContext(), PickupDetailActivity::class.java)
                    .putExtra("pickupId", d.pickup.pickupId)
            )
        }
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) = refresh()
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // Reload when coming back from the detail screen
    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        if (_binding == null) return
        val repo = DataRepository(requireContext())
        val userId = SessionManager(requireContext()).getUserId()
        val openTab = binding.tabs.selectedTabPosition == 0

        val pickups = if (openTab) repo.getOpenTasks() else repo.getMyActiveTasks(userId)
        val details = repo.toDetails(pickups).sortedBy { it.pickup.pickupDate + it.pickup.pickupTime }

        adapter.update(details)
        binding.tvEmpty.text = if (openTab) {
            "No open tasks right now. Check back soon."
        } else {
            "You have no active tasks. Accept one from the Open tab."
        }
        binding.tvEmpty.visibility = if (details.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
