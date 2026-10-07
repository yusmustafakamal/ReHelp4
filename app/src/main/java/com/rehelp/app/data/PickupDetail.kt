package com.rehelp.app.data

// A pickup together with the food and people involved (for list and detail screens)
data class PickupDetail(
    val pickup: Pickup,
    val food: Food,
    val donor: User?,
    val recipient: User?,
    val volunteer: User?
)
