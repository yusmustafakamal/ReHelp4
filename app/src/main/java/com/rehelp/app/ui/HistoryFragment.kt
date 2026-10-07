package com.rehelp.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.FragmentHistoryBinding

// Donation / collection history for every role (feature 6)
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PickupAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val role = SessionManager(requireContext()).getRole()
        binding.tvTitle.text = if (role == "Donor") "Donation history" else "Collection history"

        adapter = PickupAdapter(emptyList()) { d ->
            startActivity(
                Intent(requireContext(), PickupDetailActivity::class.java)
                    .putExtra("pickupId", d.pickup.pickupId)
            )
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) = refresh()
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        if (_binding == null) return
        val repo = DataRepository(requireContext())
        val session = SessionManager(requireContext())
        val uid = session.getUserId()

        val all = repo.toDetails(repo.getHistory(uid, session.getRole()))
        val tab = binding.tabs.selectedTabPosition
        val shown = when (tab) {
            1 -> all.filter { it.pickup.status == "Reserved" || it.pickup.status == "Picked Up" }
            2 -> all.filter { it.pickup.status == "Completed" }
            3 -> all.filter { it.pickup.status == "Cancelled" }
            else -> all
        }
        adapter.update(shown)

        binding.tvCount.text = if (shown.size == 1) "1 record" else "${shown.size} records"
        binding.tvEmpty.text = when (tab) {
            1 -> "Nothing in progress right now."
            2 -> "No completed records yet."
            3 -> "No cancelled records."
            else -> "No history yet. Your donations or collections will appear here."
        }
        binding.tvEmpty.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE

        updatePendingBanner(repo, session)
    }

    // Recipients: reservations that still need a pickup time
    private fun updatePendingBanner(repo: DataRepository, session: SessionManager) {
        val pending = if (session.getRole() == "Recipient") {
            repo.getUnscheduledReservations(session.getUserId())
        } else emptyList()

        if (pending.isEmpty()) {
            binding.cardPending.visibility = View.GONE
            return
        }
        val first = pending.first()
        val name = repo.getFoodById(first.foodId)?.name ?: "A reservation"
        binding.tvPending.text = if (pending.size == 1) {
            "$name is reserved but has no pickup time yet."
        } else {
            "$name and ${pending.size - 1} more reservations have no pickup time yet."
        }
        binding.cardPending.visibility = View.VISIBLE

        binding.btnSchedule.setOnClickListener {
            startActivity(
                Intent(requireContext(), SchedulePickupActivity::class.java)
                    .putExtra("reservationId", first.reservationId)
            )
        }
        binding.btnCancelRes.setOnClickListener { confirmCancel(repo, first.reservationId, name) }
    }

    private fun confirmCancel(repo: DataRepository, reservationId: Int, name: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel reservation?")
            .setMessage("$name will be listed as available again.")
            .setPositiveButton("Cancel reservation") { _, _ ->
                repo.cancelReservation(reservationId)
                    .onSuccess {
                        Toast.makeText(requireContext(), "Reservation cancelled", Toast.LENGTH_SHORT).show()
                        refresh()
                    }
                    .onFailure {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Keep it", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
