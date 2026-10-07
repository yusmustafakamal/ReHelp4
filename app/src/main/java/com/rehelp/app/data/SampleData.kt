package com.rehelp.app.data

// Pre-loads realistic sample data on the first launch (for demo and testing)
object SampleData {

    fun seed(repo: DataRepository) {
        if (repo.isSeeded()) return

        repo.saveUsers(listOf(
            User(1, "Aina Bakery", "aina@rehelp.my", "Pass1234", "Donor", "012-3456789", "Klang"),
            User(2, "Kafe Senja", "senja@rehelp.my", "Pass1234", "Donor", "013-2223344", "Shah Alam"),
            User(3, "Mak Cik Kiah", "kiah@rehelp.my", "Pass1234", "Donor", "011-5556677", "Subang Jaya"),
            User(4, "Hasan", "hasan@rehelp.my", "Pass1234", "Recipient", "019-1112233", "Klang"),
            User(5, "Siti", "siti@rehelp.my", "Pass1234", "Recipient", "017-4445566", "Shah Alam"),
            User(6, "Farid", "farid@rehelp.my", "Pass1234", "Recipient", "016-7778899", "Petaling Jaya"),
            User(7, "Daniel", "daniel@rehelp.my", "Pass1234", "Volunteer", "010-1234567", "Klang"),
            User(8, "Mei Ling", "meiling@rehelp.my", "Pass1234", "Volunteer", "012-7654321", "Shah Alam"),
            User(9, "Arif", "arif@rehelp.my", "Pass1234", "Volunteer", "014-9988776", "Subang Jaya")
        ))

        repo.saveFoods(listOf(
            Food(1, 1, "Fresh buns", "Bakery", 20, "2026-10-04 20:00", "Klang", "img_buns", "Available"),
            Food(2, 1, "Chocolate croissants", "Bakery", 12, "2026-10-04 20:00", "Klang", "img_croissant", "Available"),
            Food(3, 2, "Nasi lemak packs", "Meals", 10, "2026-10-04 21:00", "Shah Alam", "img_nasi_lemak", "Available"),
            Food(4, 2, "Chicken rice packs", "Meals", 8, "2026-10-04 21:00", "Shah Alam", "img_chicken_rice", "Available"),
            Food(5, 3, "Mixed vegetables", "Fruit & vegetables", 5, "2026-10-05 12:00", "Subang Jaya", "img_vegetables", "Available"),
            Food(6, 3, "Banana bunches", "Fruit & vegetables", 6, "2026-10-05 12:00", "Subang Jaya", "img_banana", "Available"),
            Food(7, 1, "Sandwiches", "Bakery", 15, "2026-10-04 19:00", "Klang", "img_sandwich", "Available"),
            Food(8, 2, "Bottled juice", "Drinks", 24, "2026-10-08 18:00", "Shah Alam", "img_juice", "Available"),
            Food(9, 3, "Canned sardines", "Groceries", 30, "2026-12-01 18:00", "Subang Jaya", "img_canned", "Available"),
            Food(10, 1, "Curry puffs", "Bakery", 25, "2026-10-01 20:00", "Klang", "img_curry_puff", "Completed")
        ))

        // One completed example so History is not empty during the demo
        repo.saveReservations(listOf(Reservation(1, 10, 4, "2026-10-01 18:20")))
        repo.savePickups(listOf(Pickup(1, 1, 7, "2026-10-01", "19:30", "Completed", true)))
        repo.saveLogs(listOf(
            StatusLog(1, 1, "Reserved", "2026-10-01 18:25", 4),
            StatusLog(2, 1, "Picked Up", "2026-10-01 19:35", 7),
            StatusLog(3, 1, "Completed", "2026-10-01 20:10", 7)
        ))
    }
}
