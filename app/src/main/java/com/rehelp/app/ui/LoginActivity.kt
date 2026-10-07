package com.rehelp.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.SampleData
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repo: DataRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = DataRepository(this)
        session = SessionManager(this)
        SampleData.seed(repo)   // loads demo data on first launch only

        // Already logged in: skip this screen
        if (session.isLoggedIn()) {
            goHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        var valid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Enter your email"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email, like name@example.com"
            valid = false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Enter your password"
            valid = false
        }
        if (!valid) return

        val user = repo.login(email, password)
        if (user == null) {
            binding.tilPassword.error = "Email or password is incorrect"
            Snackbar.make(binding.root, "Login failed. Check your email and password.", Snackbar.LENGTH_LONG).show()
            return
        }

        session.saveLogin(user)
        goHome()
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
