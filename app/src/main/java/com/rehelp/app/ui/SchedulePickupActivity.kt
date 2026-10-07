package com.rehelp.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.Food
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.ActivitySchedulePickupBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Recipient picks a pickup date and time (feature 4)
class SchedulePickupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySchedulePickupBinding
    private lateinit var repo: DataRepository
    private lateinit var session: SessionManager
    private lateinit var food: Food
    private var reservationId = -1
    private var expiryMillis = Long.MAX_VALUE

    private val pickup: Calendar = Calendar.getInstance().apply {
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    private var dateSet = false
    private var timeSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchedulePickupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        repo = DataRepository(this)
        session = SessionManager(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

        reservationId = intent.getIntExtra("reservationId", -1)
        val reservation = repo.getReservationById(reservationId)
        val foodItem = reservation?.let { repo.getFoodById(it.foodId) }
        if (foodItem == null) {
            Toast.makeText(this, "Reservation not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        food = foodItem

        expiryMillis = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(food.expiryTime)?.time ?: Long.MAX_VALUE
        if (expiryMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "This food has already expired.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.tvFoodName.text = food.name
        binding.tvFoodMeta.text = "${food.area} \u00B7 Expires ${food.expiryTime}"

        binding.etDate.setOnClickListener { showDatePicker() }
        binding.etTime.setOnClickListener { showTimePicker() }
        binding.btnConfirm.setOnClickListener { attemptConfirm() }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                pickup.set(year, month, day)
                dateSet = true
                binding.etDate.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day))
                binding.tilDate.error = null
            },
            pickup.get(Calendar.YEAR), pickup.get(Calendar.MONTH), pickup.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Only dates between now and the expiry are allowed
            datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.maxDate = expiryMillis
        }.show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hour, minute ->
                pickup.set(Calendar.HOUR_OF_DAY, hour)
                pickup.set(Calendar.MINUTE, minute)
                timeSet = true
                binding.etTime.setText(String.format(Locale.US, "%02d:%02d", hour, minute))
                binding.tilTime.error = null
            },
            if (timeSet) pickup.get(Calendar.HOUR_OF_DAY) else 19,
            if (timeSet) pickup.get(Calendar.MINUTE) else 0,
            true
        ).show()
    }

    private fun attemptConfirm() {
        binding.tilDate.error = null
        binding.tilTime.error = null

        var valid = true
        if (!dateSet) {
            binding.tilDate.error = "Choose a pickup date"
            valid = false
        }
        if (!timeSet) {
            binding.tilTime.error = "Choose a pickup time"
            valid = false
        } else if (dateSet) {
            if (pickup.timeInMillis <= System.currentTimeMillis()) {
                binding.tilTime.error = "Pickup time must be in the future"
                valid = false
            } else if (pickup.timeInMillis > expiryMillis) {
                binding.tilTime.error = "Pickup must be before the food expires (${food.expiryTime})"
                valid = false
            }
        }
        if (!valid) return

        val dateText = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(pickup.time)
        val timeText = SimpleDateFormat("HH:mm", Locale.US).format(pickup.time)
        repo.schedulePickup(
            reservationId, dateText, timeText,
            binding.swVolunteer.isChecked, session.getUserId()
        )
        Toast.makeText(this, "Pickup scheduled for $dateText at $timeText", Toast.LENGTH_LONG).show()
        finish()
    }
}
