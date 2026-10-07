package com.rehelp.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.rehelp.app.R
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.ActivityHomeBinding

// Hosts the bottom navigation. Each tab is a Fragment.
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)
        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top padding only. The bottom navigation handles its own bottom inset.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

        val role = session.getRole()
        val tab2 = when (role) {
            "Donor" -> "My listings"
            "Recipient" -> "Browse"
            else -> "Tasks"
        }
        binding.bottomNav.menu.findItem(R.id.nav_main).title = tab2

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_main -> when (role) {
                    "Donor" -> MyListingsFragment()
                    "Recipient" -> BrowseFragment()
                    else -> TasksFragment()
                }
                R.id.nav_history -> HistoryFragment()
                else -> ProfileFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
            true
        }

        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_home
        }
    }
}