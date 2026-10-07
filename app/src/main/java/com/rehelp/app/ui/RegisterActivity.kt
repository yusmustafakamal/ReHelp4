package com.rehelp.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rehelp.app.R
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.SessionManager
import com.rehelp.app.data.User
import com.rehelp.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repo: DataRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        repo = DataRepository(this)
        session = SessionManager(this)

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.tvLogin.setOnClickListener { finish() }
        binding.rgRole.setOnCheckedChangeListener { _, _ ->
            binding.tvRoleError.visibility = View.GONE
        }
    }

    private fun attemptRegister() {
        listOf(binding.tilName, binding.tilEmail, binding.tilPassword, binding.tilPhone, binding.tilArea)
            .forEach { it.error = null }

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val phone = binding.etPhone.text.toString().trim()
        val area = binding.etArea.text.toString().trim()
        val role = when (binding.rgRole.checkedRadioButtonId) {
            R.id.rbDonor -> "Donor"
            R.id.rbRecipient -> "Recipient"
            R.id.rbVolunteer -> "Volunteer"
            else -> ""
        }

        var valid = true
        if (name.length < 2) {
            binding.tilName.error = "Enter your name (at least 2 letters)"
            valid = false
        }
        if (email.isEmpty()) {
            binding.tilEmail.error = "Enter your email"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email, like name@example.com"
            valid = false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            valid = false
        }
        if (!Regex("^[0-9+\\- ]{9,15}$").matches(phone)) {
            binding.tilPhone.error = "Enter a valid phone number, e.g. 012-3456789"
            valid = false
        }
        if (area.isEmpty()) {
            binding.tilArea.error = "Enter your area"
            valid = false
        }
        if (role.isEmpty()) {
            binding.tvRoleError.visibility = View.VISIBLE
            valid = false
        }
        if (!valid) return

        repo.register(User(0, name, email, password, role, phone, area))
            .onSuccess { user ->
                session.saveLogin(user)
                Toast.makeText(this, "Welcome to ReHelp, ${user.name}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .onFailure { binding.tilEmail.error = it.message }
    }
}
