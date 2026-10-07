package com.rehelp.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.Food
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.ActivityFoodDetailBinding

// Shows one listing and lets a recipient reserve it (feature 3)
class FoodDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodDetailBinding
    private lateinit var repo: DataRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        repo = DataRepository(this)
        session = SessionManager(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val food = repo.getFoodById(intent.getIntExtra("foodId", -1))
        if (food == null) {
            Toast.makeText(this, "This listing is no longer available.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        showFood(food)

        if (session.getRole() != "Recipient") {
            binding.btnReserve.visibility = View.GONE
            binding.tvNote.visibility = View.VISIBLE
        }
        binding.btnReserve.setOnClickListener { confirmReserve(food) }
    }

    private fun showFood(food: Food) {
        val donor = repo.getUserById(food.donorId)
        FoodImages.load(binding.ivFood, food.imageName)
        binding.tvName.text = food.name
        binding.tvStatus.text = food.status
        binding.tvCategory.text = "Category: ${food.category}"
        binding.tvQty.text = "Quantity: ${food.quantity}"
        binding.tvExpiry.text = "Expires: ${food.expiryTime}"
        binding.tvArea.text = "Collection area: ${food.area}"
        binding.tvDonor.text = "Donor: ${donor?.name ?: "Unknown"}"
    }

    private fun confirmReserve(food: Food) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reserve this food?")
            .setMessage("${food.name} will be held for you. You will choose a pickup time next.")
            .setPositiveButton("Reserve") { _, _ -> reserve(food) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reserve(food: Food) {
        repo.reserve(food.foodId, session.getUserId())
            .onSuccess { reservation ->
                Toast.makeText(this, "Food reserved", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(this, SchedulePickupActivity::class.java)
                        .putExtra("reservationId", reservation.reservationId)
                )
                finish()
            }
            .onFailure { error ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Couldn't reserve")
                    .setMessage(error.message)
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .show()
            }
    }
}
