package com.rehelp.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.Food
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.FragmentMyListingsBinding

// Donor's own listings (all statuses). Tap a reserved one to manage its pickup.
class MyListingsFragment : Fragment() {

    private var _binding: FragmentMyListingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FoodAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyListingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FoodAdapter(emptyList()) { food -> openPickupFor(food) }
        binding.rvFoods.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFoods.adapter = adapter
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddFoodActivity::class.java))
        }
    }

    private fun openPickupFor(food: Food) {
        val pickup = DataRepository(requireContext()).getActivePickupForFood(food.foodId)
        when {
            pickup != null -> startActivity(
                Intent(requireContext(), PickupDetailActivity::class.java)
                    .putExtra("pickupId", pickup.pickupId)
            )
            food.status == "Reserved" ->
                Toast.makeText(requireContext(), "Waiting for the recipient to choose a pickup time.", Toast.LENGTH_SHORT).show()
            food.status == "Completed" ->
                Toast.makeText(requireContext(), "This donation has been completed.", Toast.LENGTH_SHORT).show()
            else ->
                Toast.makeText(requireContext(), "No one has reserved this food yet.", Toast.LENGTH_SHORT).show()
        }
    }

    // Reload when returning from Add food or a pickup
    override fun onResume() {
        super.onResume()
        val session = SessionManager(requireContext())
        val list = DataRepository(requireContext()).getFoodsByDonor(session.getUserId())
        adapter.update(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}