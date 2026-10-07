package com.rehelp.app.data

// Roles: "Donor", "Recipient", "Volunteer"
data class User(
    val userId: Int,
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val phone: String,
    val area: String
)

// Status: "Available", "Reserved", "Completed"
data class Food(
    val foodId: Int,
    val donorId: Int,
    val name: String,
    val category: String,
    val quantity: Int,
    val expiryTime: String,   // yyyy-MM-dd HH:mm
    val area: String,
    val imageName: String,    // drawable resource name, e.g. "img_buns"
    val status: String
)

data class Reservation(
    val reservationId: Int,
    val foodId: Int,
    val recipientId: Int,
    val reservedAt: String
)

// Status: "Reserved", "Picked Up", "Completed", "Cancelled"
data class Pickup(
    val pickupId: Int,
    val reservationId: Int,
    val volunteerId: Int?,     // null if recipient collects personally or no volunteer yet
    val pickupDate: String,    // yyyy-MM-dd
    val pickupTime: String,    // HH:mm
    val status: String,
    val needsVolunteer: Boolean
)

data class StatusLog(
    val logId: Int,
    val pickupId: Int,
    val status: String,
    val changedAt: String,
    val changedBy: Int
)
