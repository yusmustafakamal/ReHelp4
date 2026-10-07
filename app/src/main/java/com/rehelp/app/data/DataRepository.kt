package com.rehelp.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Stores all data as JSON files in internal storage (filesDir)
class DataRepository(context: Context) {

    private val gson = Gson()
    private val dir: File = context.filesDir

    companion object {
        const val USERS_FILE = "users.json"
        const val FOOD_FILE = "food.json"
        const val RESERVATIONS_FILE = "reservations.json"
        const val PICKUPS_FILE = "pickups.json"
        const val LOG_FILE = "status_log.json"

        // Allowed status changes for a pickup
        private val ALLOWED = mapOf(
            "Reserved" to listOf("Picked Up", "Cancelled"),
            "Picked Up" to listOf("Completed")
        )
    }

    // ---------- Low-level read/write ----------
    private inline fun <reified T> load(name: String): MutableList<T> {
        val f = File(dir, name)
        if (!f.exists()) return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<T>>() {}.type
            gson.fromJson<MutableList<T>>(f.readText(), type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun save(name: String, data: Any) {
        File(dir, name).writeText(gson.toJson(data))
    }

    private fun now(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

    fun isSeeded(): Boolean = File(dir, FOOD_FILE).exists()

    // ---------- Getters and savers ----------
    fun getUsers(): MutableList<User> = load<User>(USERS_FILE)
    fun getFoods(): MutableList<Food> = load<Food>(FOOD_FILE)
    fun getReservations(): MutableList<Reservation> = load<Reservation>(RESERVATIONS_FILE)
    fun getPickups(): MutableList<Pickup> = load<Pickup>(PICKUPS_FILE)
    fun getLogs(): MutableList<StatusLog> = load<StatusLog>(LOG_FILE)

    fun saveUsers(l: List<User>) = save(USERS_FILE, l)
    fun saveFoods(l: List<Food>) = save(FOOD_FILE, l)
    fun saveReservations(l: List<Reservation>) = save(RESERVATIONS_FILE, l)
    fun savePickups(l: List<Pickup>) = save(PICKUPS_FILE, l)
    fun saveLogs(l: List<StatusLog>) = save(LOG_FILE, l)

    // ---------- Login / register ----------
    fun login(email: String, password: String): User? =
        getUsers().find { it.email.equals(email.trim(), true) && it.password == password }

    fun register(user: User): Result<User> {
        val users = getUsers()
        if (users.any { it.email.equals(user.email.trim(), true) }) {
            return Result.failure(Exception("That email is already registered. Try logging in."))
        }
        val newUser = user.copy(userId = (users.maxOfOrNull { it.userId } ?: 0) + 1)
        users.add(newUser)
        saveUsers(users)
        return Result.success(newUser)
    }

    // ---------- Feature 1: surplus-food listing ----------
    fun addFood(food: Food): Food {
        val list = getFoods()
        val newFood = food.copy(foodId = (list.maxOfOrNull { it.foodId } ?: 0) + 1, status = "Available")
        list.add(newFood)
        saveFoods(list)
        return newFood
    }

    // ---------- Feature 2: search / filter ----------
    fun searchFoods(keyword: String, category: String?, area: String?): List<Food> =
        getFoods().filter {
            it.status == "Available" &&
                (keyword.isBlank() || it.name.contains(keyword.trim(), ignoreCase = true)) &&
                (category.isNullOrBlank() || it.category == category) &&
                (area.isNullOrBlank() || it.area.equals(area, ignoreCase = true))
        }

    // ---------- Feature 3: reservation ----------
    fun reserve(foodId: Int, recipientId: Int): Result<Reservation> {
        val foods = getFoods()
        val i = foods.indexOfFirst { it.foodId == foodId }
        if (i < 0) return Result.failure(Exception("This food listing no longer exists."))
        if (foods[i].status != "Available") {
            return Result.failure(Exception("Sorry, this food has just been reserved by someone else."))
        }
        foods[i] = foods[i].copy(status = "Reserved")
        saveFoods(foods)

        val reservations = getReservations()
        val r = Reservation((reservations.maxOfOrNull { it.reservationId } ?: 0) + 1, foodId, recipientId, now())
        reservations.add(r)
        saveReservations(reservations)
        return Result.success(r)
    }

    // ---------- Feature 4: pickup scheduling ----------
    fun schedulePickup(reservationId: Int, date: String, time: String, needsVolunteer: Boolean, changedBy: Int): Pickup {
        val pickups = getPickups()
        val p = Pickup(
            pickupId = (pickups.maxOfOrNull { it.pickupId } ?: 0) + 1,
            reservationId = reservationId,
            volunteerId = null,
            pickupDate = date,
            pickupTime = time,
            status = "Reserved",
            needsVolunteer = needsVolunteer
        )
        pickups.add(p)
        savePickups(pickups)
        addLog(p.pickupId, "Reserved", changedBy)
        return p
    }

    // Volunteer sees tasks that still need a volunteer
    fun getOpenTasks(): List<Pickup> =
        getPickups().filter { it.needsVolunteer && it.volunteerId == null && it.status == "Reserved" }

    fun acceptTask(pickupId: Int, volunteerId: Int): Result<Pickup> {
        val pickups = getPickups()
        val i = pickups.indexOfFirst { it.pickupId == pickupId }
        if (i < 0) return Result.failure(Exception("Task not found."))
        if (pickups[i].volunteerId != null) {
            return Result.failure(Exception("Another volunteer has already accepted this task."))
        }
        pickups[i] = pickups[i].copy(volunteerId = volunteerId)
        savePickups(pickups)
        return Result.success(pickups[i])
    }

    // ---------- Feature 5: collection-status update ----------
    fun updateStatus(pickupId: Int, newStatus: String, changedBy: Int): Result<Pickup> {
        val pickups = getPickups()
        val i = pickups.indexOfFirst { it.pickupId == pickupId }
        if (i < 0) return Result.failure(Exception("Pickup not found."))
        val current = pickups[i]
        val allowed = ALLOWED[current.status] ?: emptyList()
        if (newStatus !in allowed) {
            return Result.failure(Exception("Can't change status from ${current.status} to $newStatus."))
        }
        val updated = current.copy(status = newStatus)
        pickups[i] = updated
        savePickups(pickups)

        // Keep the food status in sync
        val res = getReservations().find { it.reservationId == current.reservationId }
        if (res != null) {
            val foods = getFoods()
            val fi = foods.indexOfFirst { it.foodId == res.foodId }
            if (fi >= 0) {
                val foodStatus = when (newStatus) {
                    "Cancelled" -> "Available"
                    "Completed" -> "Completed"
                    else -> foods[fi].status
                }
                foods[fi] = foods[fi].copy(status = foodStatus)
                saveFoods(foods)
            }
        }
        addLog(pickupId, newStatus, changedBy)
        return Result.success(updated)
    }

    private fun addLog(pickupId: Int, status: String, changedBy: Int) {
        val logs = getLogs()
        logs.add(StatusLog((logs.maxOfOrNull { it.logId } ?: 0) + 1, pickupId, status, now(), changedBy))
        saveLogs(logs)
    }

    // ---------- Feature 6: donation / collection history ----------
    fun getHistory(userId: Int, role: String): List<Pickup> {
        val reservations = getReservations()
        val foods = getFoods()
        return getPickups().filter { p ->
            val r = reservations.find { it.reservationId == p.reservationId }
            val f = r?.let { x -> foods.find { it.foodId == x.foodId } }
            when (role) {
                "Donor" -> f?.donorId == userId
                "Recipient" -> r?.recipientId == userId
                else -> p.volunteerId == userId
            }
        }.sortedByDescending { it.pickupId }
    }

    // ---------- Summary numbers for the Home screen ----------
    fun getHomeStats(userId: Int, role: String): List<Pair<String, Int>> {
        val foods = getFoods()
        val history = getHistory(userId, role)
        val active = history.count { it.status == "Reserved" || it.status == "Picked Up" }
        val done = history.count { it.status == "Completed" }
        return when (role) {
            "Donor" -> {
                val mine = foods.filter { it.donorId == userId }
                listOf(
                    "Available" to mine.count { it.status == "Available" },
                    "Reserved" to mine.count { it.status == "Reserved" },
                    "Completed" to done
                )
            }
            "Recipient" -> listOf(
                "Food available" to foods.count { it.status == "Available" },
                "Active pickups" to active,
                "Collected" to done
            )
            else -> listOf(
                "Open tasks" to getOpenTasks().size,
                "My active tasks" to active,
                "Completed" to done
            )
        }
    }

    // Donor's own listings, newest first
    fun getFoodsByDonor(donorId: Int): List<Food> =
        getFoods().filter { it.donorId == donorId }.sortedByDescending { it.foodId }

    // Lookups by ID
    fun getFoodById(id: Int): Food? = getFoods().find { it.foodId == id }
    fun getUserById(id: Int): User? = getUsers().find { it.userId == id }
    fun getReservationById(id: Int): Reservation? = getReservations().find { it.reservationId == id }

    // ---------- Pickup details and tasks ----------
    fun getPickupDetail(pickupId: Int): PickupDetail? {
        val p = getPickups().find { it.pickupId == pickupId } ?: return null
        val r = getReservationById(p.reservationId) ?: return null
        val f = getFoodById(r.foodId) ?: return null
        return PickupDetail(
            p, f,
            getUserById(f.donorId),
            getUserById(r.recipientId),
            p.volunteerId?.let { getUserById(it) }
        )
    }

    fun toDetails(list: List<Pickup>): List<PickupDetail> =
        list.mapNotNull { getPickupDetail(it.pickupId) }

    // The current (not yet finished) pickup of a food item, if any
    fun getActivePickupForFood(foodId: Int): Pickup? {
        val reservationIds = getReservations().filter { it.foodId == foodId }.map { it.reservationId }
        return getPickups()
            .filter { it.reservationId in reservationIds && (it.status == "Reserved" || it.status == "Picked Up") }
            .maxByOrNull { it.pickupId }
    }

    // Volunteer's accepted tasks that are still in progress
    fun getMyActiveTasks(volunteerId: Int): List<Pickup> =
        getPickups().filter {
            it.volunteerId == volunteerId && (it.status == "Reserved" || it.status == "Picked Up")
        }

    // Reservations by this recipient that still have no pickup time
    fun getUnscheduledReservations(recipientId: Int): List<Reservation> {
        val scheduledIds = getPickups().map { it.reservationId }.toSet()
        val foods = getFoods()
        return getReservations().filter { r ->
            r.recipientId == recipientId &&
                    r.reservationId !in scheduledIds &&
                    foods.find { it.foodId == r.foodId }?.status == "Reserved"
        }
    }

    // Cancel a reservation that has no pickup yet; the food becomes Available again
    fun cancelReservation(reservationId: Int): Result<Unit> {
        val reservations = getReservations()
        val r = reservations.find { it.reservationId == reservationId }
            ?: return Result.failure(Exception("Reservation not found."))
        if (getPickups().any { it.reservationId == reservationId }) {
            return Result.failure(Exception("A pickup is already scheduled. Cancel it from the pickup details instead."))
        }
        reservations.remove(r)
        saveReservations(reservations)

        val foods = getFoods()
        val i = foods.indexOfFirst { it.foodId == r.foodId }
        if (i >= 0) {
            foods[i] = foods[i].copy(status = "Available")
            saveFoods(foods)
        }
        return Result.success(Unit)
    }
}
