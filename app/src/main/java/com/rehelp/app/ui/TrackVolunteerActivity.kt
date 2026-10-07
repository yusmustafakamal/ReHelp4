package com.rehelp.app.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rehelp.app.data.DataRepository
import com.rehelp.app.data.SessionManager
import com.rehelp.app.databinding.ActivityTrackVolunteerBinding
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

// Dummy tracking screen: shows where the volunteer is. Positions are simulated and static.
class TrackVolunteerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackVolunteerBinding

    // Fixed map position for each area (fractions of the map size)
    private fun areaPoint(area: String, dx: Float, dy: Float): PointF {
        val base = when (area) {
            "Klang" -> PointF(0.15f, 0.70f)
            "Shah Alam" -> PointF(0.40f, 0.50f)
            "Subang Jaya" -> PointF(0.58f, 0.66f)
            "Petaling Jaya" -> PointF(0.80f, 0.36f)
            else -> PointF(0.50f, 0.50f)
        }
        return PointF((base.x + dx).coerceIn(0.12f, 0.88f), (base.y + dy).coerceIn(0.12f, 0.82f))
    }

    // Rough distance for a map about 25 km wide and 18 km high
    private fun km(a: PointF, b: PointF): Double {
        val dx = (a.x - b.x) * 25.0
        val dy = (a.y - b.y) * 18.0
        return sqrt(dx * dx + dy * dy)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackVolunteerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        binding.toolbar.setNavigationOnClickListener { finish() }

        val repo = DataRepository(this)
        val me = SessionManager(this).getUserId()
        val d = repo.getPickupDetail(intent.getIntExtra("pickupId", -1))
        val volunteer = d?.volunteer

        // Only people involved in this pickup can see the volunteer
        val isDonor = d?.food?.donorId == me
        val isRecipient = d?.recipient?.userId == me
        val isVolunteer = volunteer?.userId == me
        if (d == null || volunteer == null || !(isDonor || isRecipient || isVolunteer)) {
            Toast.makeText(this, "No volunteer to track for this pickup.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val onTheWay = d.pickup.status == "Picked Up"
        val donorPoint = areaPoint(d.food.area, -0.05f, -0.05f)
        val recipientPoint = areaPoint(d.recipient?.area ?: d.food.area, 0.05f, 0.05f)
        val volunteerPoint = if (onTheWay) {
            PointF((donorPoint.x + recipientPoint.x) / 2, (donorPoint.y + recipientPoint.y) / 2)
        } else {
            areaPoint(volunteer.area, 0f, 0.08f)
        }

        val route = if (onTheWay) listOf(volunteerPoint, recipientPoint)
        else listOf(volunteerPoint, donorPoint, recipientPoint)

        binding.mapView.setData(
            listOf(
                DummyMapView.Pin(donorPoint.x, donorPoint.y, "Pickup: ${d.donor?.name ?: "Donor"}", Color.parseColor("#E65100"), "D"),
                DummyMapView.Pin(recipientPoint.x, recipientPoint.y, "Drop-off: ${d.recipient?.name ?: "Recipient"}", Color.parseColor("#2E7D32"), "R"),
                DummyMapView.Pin(volunteerPoint.x, volunteerPoint.y, volunteer.name, Color.parseColor("#1565C0"), "V")
            ),
            route
        )

        // Estimated time at about 30 km/h
        val distance = max(0.8, if (onTheWay) km(volunteerPoint, recipientPoint) else km(volunteerPoint, donorPoint))
        val minutes = max(3, (distance / 30.0 * 60).roundToInt())

        binding.tvStage.text = if (onTheWay) "On the way to the recipient" else "Heading to the pickup point"
        binding.tvVolunteer.text = "Volunteer: ${volunteer.name}"
        binding.tvEta.text = "Estimated arrival: about $minutes min (${String.format(Locale.US, "%.1f", distance)} km)"
        binding.tvSteps.text = "Pickup: ${d.donor?.name ?: "-"}, ${d.food.area}\n" +
            "Drop-off: ${d.recipient?.name ?: "-"}, ${d.recipient?.area ?: "-"}"
        binding.mapView.contentDescription =
            "Map showing the donor, recipient and volunteer ${volunteer.name}. ${binding.tvStage.text}."

        // Donor and recipient can call the volunteer (opens the phone dialer)
        if (isDonor || isRecipient) {
            binding.btnCall.visibility = View.VISIBLE
            binding.btnCall.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${volunteer.phone}")))
            }
        }
    }
}
