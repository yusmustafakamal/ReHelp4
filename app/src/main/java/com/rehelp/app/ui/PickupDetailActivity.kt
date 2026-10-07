package com.rehelp.app.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.PickupDetail
import com.rehelp.app.data.SessionManager
import com.rehelp.app.data.User
import com.rehelp.app.databinding.ActivityPickupDetailBinding
import android.content.Intent

// Shows one pickup and the actions the current user is allowed to take (feature 5)
class PickupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickupDetailBinding
    private lateinit var repo: DataRepository
    private lateinit var session: SessionManager
    private var pickupId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickupDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        repo = DataRepository(this)
        session = SessionManager(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

        pickupId = intent.getIntExtra("pickupId", -1)
        if (!load()) {
            Toast.makeText(this, "This pickup could not be found.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun load(): Boolean {
        val detail = repo.getPickupDetail(pickupId) ?: return false
        show(detail)
        return true
    }

    private fun show(d: PickupDetail) {
        val uid = session.getUserId()
        val role = session.getRole()
        val isDonor = d.food.donorId == uid
        val isRecipient = d.recipient?.userId == uid
        val isMyTask = d.pickup.volunteerId == uid
        val isParty = isDonor || isRecipient || isMyTask   // only people in this pickup see phone numbers
        val status = d.pickup.status

        FoodImages.load(binding.ivFood, d.food.imageName)
        binding.tvName.text = d.food.name
        binding.tvStatus.text = status
        binding.tvStatus.setTextColor(StatusColors.of(status))
        binding.tvWhen.text = "Pickup: ${d.pickup.pickupDate} at ${d.pickup.pickupTime}"
        binding.tvArea.text = "Collection area: ${d.food.area}"

        fun person(u: User?) = when {
            u == null -> "-"
            isParty -> "${u.name} (${u.phone})"
            else -> u.name
        }
        binding.tvDonor.text = "Donor: ${person(d.donor)}"
        binding.tvRecipient.text = "Recipient: ${person(d.recipient)}"
        binding.tvVolunteer.text = when {
            d.volunteer != null -> "Volunteer: ${person(d.volunteer)}"
            d.pickup.needsVolunteer -> "Volunteer: Not assigned yet"
            else -> "Volunteer: Not needed (recipient collects)"
        }

        // Reset, then show only the actions this user may take
        binding.btnPrimary.visibility = View.GONE
        binding.btnSecondary.visibility = View.GONE
        binding.btnTrack.visibility = View.GONE

        val canHandle = isDonor || isMyTask
        when {
            role == "Volunteer" && status == "Reserved" && d.pickup.needsVolunteer && d.pickup.volunteerId == null ->
                setPrimary("Accept this task") { accept() }

            canHandle && status == "Reserved" -> {
                setPrimary("Mark as picked up") { changeStatus("Picked Up") }
                if (isDonor) setSecondary("Cancel pickup") { confirmCancel() }
            }

            canHandle && status == "Picked Up" ->
                setPrimary("Mark as completed") { changeStatus("Completed") }

            isRecipient && status == "Reserved" ->
                setSecondary("Cancel my pickup") { confirmCancel() }
        }

        val inProgress = status == "Reserved" || status == "Picked Up"
        if (isParty && d.volunteer != null && inProgress) {
            binding.btnTrack.visibility = View.VISIBLE
            binding.btnTrack.setOnClickListener {
                startActivity(Intent(this, TrackVolunteerActivity::class.java).putExtra("pickupId", pickupId))
            }
        }
    }

    private fun setPrimary(text: String, action: () -> Unit) {
        binding.btnPrimary.text = text
        binding.btnPrimary.visibility = View.VISIBLE
        binding.btnPrimary.setOnClickListener { action() }
    }

    private fun setSecondary(text: String, action: () -> Unit) {
        binding.btnSecondary.text = text
        binding.btnSecondary.visibility = View.VISIBLE
        binding.btnSecondary.setOnClickListener { action() }
    }

    private fun accept() {
        repo.acceptTask(pickupId, session.getUserId())
            .onSuccess {
                Toast.makeText(this, "Task accepted. It is now in your Mine tab.", Toast.LENGTH_SHORT).show()
                load()
            }
            .onFailure { showError(it.message) }
    }

    private fun changeStatus(newStatus: String) {
        repo.updateStatus(pickupId, newStatus, session.getUserId())
            .onSuccess {
                Toast.makeText(this, "Status updated: $newStatus", Toast.LENGTH_SHORT).show()
                load()
            }
            .onFailure { showError(it.message) }
    }

    private fun confirmCancel() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cancel this pickup?")
            .setMessage("The food will be listed as available again.")
            .setPositiveButton("Cancel pickup") { _, _ -> changeStatus("Cancelled") }
            .setNegativeButton("Keep it", null)
            .show()
    }

    private fun showError(message: String?) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Couldn't update")
            .setMessage(message ?: "Something went wrong. Please try again.")
            .setPositiveButton("OK") { _, _ -> load() }
            .show()
    }
}
