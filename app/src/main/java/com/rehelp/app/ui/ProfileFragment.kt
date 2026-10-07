package com.rehelp.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = SessionManager(requireContext())
        val user = DataRepository(requireContext()).getUsers().find { it.userId == session.getUserId() }

        binding.tvName.text = user?.name ?: session.getName()
        binding.tvRole.text = session.getRole()
        binding.tvEmail.text = "Email: ${user?.email ?: "-"}"
        binding.tvPhone.text = "Phone: ${user?.phone ?: "-"}"
        binding.tvArea.text = "Area: ${user?.area ?: "-"}"

        binding.btnLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
