package com.rehelp.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.Food
import com.rehelp.app.data.Options
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.ActivityAddFoodBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddFoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFoodBinding
    private lateinit var repo: DataRepository
    private lateinit var session: SessionManager

    private val expiry: Calendar = Calendar.getInstance().apply {
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    private var dateSet = false
    private var timeSet = false
    private var photoPath: String? = null

    // Opens the gallery. No storage permission is needed for GetContent.
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) savePhoto(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        repo = DataRepository(this)
        session = SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.actCategory.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Options.categories)
        )
        binding.actArea.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Options.areas)
        )

        // Pre-fill the area with the donor's own area
        val me = repo.getUsers().find { it.userId == session.getUserId() }
        if (me != null && me.area in Options.areas) {
            binding.actArea.setText(me.area, false)
        }

        binding.btnPhoto.setOnClickListener { pickImage.launch("image/*") }
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.etTime.setOnClickListener { showTimePicker() }
        binding.btnPost.setOnClickListener { attemptPost() }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                expiry.set(year, month, day)
                dateSet = true
                binding.etDate.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day))
                binding.tilDate.error = null
            },
            expiry.get(Calendar.YEAR), expiry.get(Calendar.MONTH), expiry.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hour, minute ->
                expiry.set(Calendar.HOUR_OF_DAY, hour)
                expiry.set(Calendar.MINUTE, minute)
                timeSet = true
                binding.etTime.setText(String.format(Locale.US, "%02d:%02d", hour, minute))
                binding.tilTime.error = null
            },
            if (timeSet) expiry.get(Calendar.HOUR_OF_DAY) else 18,
            if (timeSet) expiry.get(Calendar.MINUTE) else 0,
            true
        ).show()
    }

    // Copies the chosen photo into the app's internal storage
    private fun savePhoto(uri: Uri) {
        try {
            val file = File(filesDir, "food_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            photoPath = file.absolutePath
            FoodImages.load(binding.ivPhoto, "file:${file.absolutePath}")
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't load that photo. Try another one.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptPost() {
        listOf(binding.tilName, binding.tilCategory, binding.tilQuantity, binding.tilDate, binding.tilTime, binding.tilArea)
            .forEach { it.error = null }

        val name = binding.etName.text?.toString().orEmpty().trim()
        val category = binding.actCategory.text?.toString().orEmpty()
        val quantity = binding.etQuantity.text?.toString().orEmpty().trim().toIntOrNull()
        val area = binding.actArea.text?.toString().orEmpty()

        var valid = true
        if (name.length < 2 || name.length > 40) {
            binding.tilName.error = "Enter a food name (2 to 40 characters)"
            valid = false
        }
        if (category !in Options.categories) {
            binding.tilCategory.error = "Choose a category"
            valid = false
        }
        if (quantity == null || quantity < 1 || quantity > 999) {
            binding.tilQuantity.error = "Enter a quantity between 1 and 999"
            valid = false
        }
        if (!dateSet) {
            binding.tilDate.error = "Choose a date"
            valid = false
        }
        if (!timeSet) {
            binding.tilTime.error = "Choose a time"
            valid = false
        } else if (dateSet && expiry.timeInMillis <= System.currentTimeMillis()) {
            binding.tilTime.error = "Expiry time must be in the future"
            valid = false
        }
        if (area !in Options.areas) {
            binding.tilArea.error = "Choose a collection area"
            valid = false
        }
        if (!valid || quantity == null) return

        val expiryText = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(expiry.time)
        val imageName = photoPath?.let { "file:$it" } ?: ""

        repo.addFood(
            Food(0, session.getUserId(), name, category, quantity, expiryText, area, imageName, "Available")
        )
        Toast.makeText(this, "Listing posted", Toast.LENGTH_SHORT).show()
        finish()
    }
}
