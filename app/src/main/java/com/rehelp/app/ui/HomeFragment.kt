package com.rehelp.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Refresh the numbers every time the user comes back to this tab
    override fun onResume() {
        super.onResume()
        val session = SessionManager(requireContext())
        val repo = DataRepository(requireContext())
        val role = session.getRole()

        binding.tvGreeting.text = "Hi, ${session.getName()}"
        binding.tvRoleLine.text = "Logged in as $role"

        val stats = repo.getHomeStats(session.getUserId(), role)
        binding.tvStat1Value.text = stats[0].second.toString()
        binding.tvStat1Label.text = stats[0].first
        binding.tvStat2Value.text = stats[1].second.toString()
        binding.tvStat2Label.text = stats[1].first
        binding.tvStat3Value.text = stats[2].second.toString()
        binding.tvStat3Label.text = stats[2].first

        binding.tvTips.text = when (role) {
            "Donor" -> "List your surplus food, see who has reserved it, and track each pickup until it is completed."
            "Recipient" -> "Browse available food in your area, reserve what you need, and choose a pickup time."
            else -> "Accept open pickup tasks, collect the food, and update the status as you go."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
