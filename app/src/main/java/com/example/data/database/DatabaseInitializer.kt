package com.example.data.database

import android.util.Log

object DatabaseInitializer {

    suspend fun prepopulateIfNeeded(db: AppDatabase) {
        val teamDao = db.teamDao()
        val playerDao = db.playerDao()
        val matchFixtureDao = db.matchFixtureDao()
        val transferDao = db.transferDao()
        val notificationDao = db.notificationDao()
        val userProfileDao = db.userProfileDao()

        // Check if database is already populated
        val existingUserProfile = userProfileDao.getUserProfileOnce()
        if (existingUserProfile != null) {
            Log.d("DatabaseInitializer", "Database already initialized.")
            return
        }

        Log.d("DatabaseInitializer", "Prepopulating Premier League database...")

        // 1. Insert Teams
        val teams = listOf(
            TeamEntity(1, "Arsenal", "Arsenal", "ARS", "", "#EF0107", "#063672", "Emirates Stadium", "Mikel Arteta", "High-intensity possession, fluid positional rotation, and strong defensive counter-pressing."),
            TeamEntity(2, "Aston Villa", "Aston Villa", "AVL", "", "#95BFE5", "#670E36", "Villa Park", "Unai Emery", "Compact mid-block, high-line defense, and explosive vertical counter-attacks."),
            TeamEntity(3, "Bournemouth", "Bournemouth", "BOU", "", "#B50E12", "#000000", "Vitality Stadium", "Andoni Iraola", "Direct vertical transition, relentless high pressing, and physical duels."),
            TeamEntity(4, "Brentford", "Brentford", "BRE", "", "#E30613", "#FBD01B", "Gtech Community Stadium", "Thomas Frank", "Direct direct-play, set-piece mastery, and structured low-block transitions."),
            TeamEntity(5, "Brighton & Hove Albion", "Brighton", "BHA", "", "#0057B8", "#FFFFFF", "Amex Stadium", "Fabian Hürzeler", "Inverted fullbacks, baiting the press, and overload-to-isolate on wings."),
            TeamEntity(6, "Chelsea", "Chelsea", "CHE", "", "#034694", "#EE242C", "Stamford Bridge", "Enzo Maresca", "Positional play, inverted fullbacks, deep build-up with double pivot."),
            TeamEntity(7, "Crystal Palace", "Crystal Palace", "CRY", "", "#1B458F", "#C4122E", "Selhurst Park", "Oliver Glasner", "Intense 3-4-2-1 counter-pressing, dynamic inside forwards, and direct wingback play."),
            TeamEntity(8, "Everton", "Everton", "EVE", "", "#003399", "#FFFFFF", "Goodison Park", "Sean Dyche", "Low-block rigidity, physical aerial duels, and set-piece target play."),
            TeamEntity(9, "Fulham", "Fulham", "FUL", "", "#FFFFFF", "#000000", "Craven Cottage", "Marco Silva", "Balanced 4-2-3-1, overlapping wingplay, and target striker focal point."),
            TeamEntity(10, "Ipswich Town", "Ipswich", "IPS", "", "#0000FF", "#FFFFFF", "Portman Road", "Kieran McKenna", "Highly fluid attacking rotations, intense central combinations, and high-energy press."),
            TeamEntity(11, "Leicester City", "Leicester", "LEI", "", "#003090", "#FDBE11", "King Power Stadium", "Steve Cooper", "Compact defense, transitions via agile wingers, and counter-attacks."),
            TeamEntity(12, "Liverpool", "Liverpool", "LIV", "", "#C8102E", "#00B2A9", "Anfield", "Arne Slot", "Controlled possession, high pressing, and dynamic wing overloads."),
            TeamEntity(13, "Manchester City", "Man City", "MCI", "", "#6CABDD", "#1C2C5B", "Etihad Stadium", "Pep Guardiola", "Stretching the pitch, dynamic half-space runs, and inverted center-backs in midfield."),
            TeamEntity(14, "Manchester United", "Man United", "MUN", "", "#DA291C", "#FBE122", "Old Trafford", "Rúben Amorim", "High-intensity 3-4-2-1, vertical ball progression, and fluid fluid front three."),
            TeamEntity(15, "Newcastle United", "Newcastle", "NEW", "", "#000000", "#41B6E6", "St. James' Park", "Eddie Howe", "Aggressive high-intensity press, physical athletic duels, and lightning counter-attacks."),
            TeamEntity(16, "Nottingham Forest", "Nottingham Forest", "NFO", "", "#DD0000", "#FFFFFF", "The City Ground", "Nuno Espírito Santo", "Structured low-block, explosive pace on counters, and wing transitions."),
            TeamEntity(17, "Southampton", "Southampton", "SOU", "", "#D71920", "#FFFFFF", "St. Mary's Stadium", "Russell Martin", "Extreme short-passing build-up, possession dominance, and patient positional carving."),
            TeamEntity(18, "Tottenham Hotspur", "Tottenham", "TOT", "", "#FFFFFF", "#132257", "Tottenham Hotspur Stadium", "Ange Postecoglou", "Ultra-high defensive line, inverted fullbacks in half-spaces, relentless attacking flow."),
            TeamEntity(19, "West Ham United", "West Ham", "WHU", "", "#7A263A", "#1BB1E7", "London Stadium", "Julen Lopetegui", "Possession build-up, solid defensive stability, and dynamic inverted wingers."),
            TeamEntity(20, "Wolverhampton Wanderers", "Wolves", "WOL", "", "#FDB913", "#231F20", "Molineux Stadium", "Gary O'Neil", "Adaptive 3-man or 4-man structures, rapid transitional ball-carrying, and dynamic wide overloads.")
        )
        teamDao.insertTeams(teams)

        // 2. Insert Star Players for Teams (focused on key squads for richness)
        val players = listOf(
            // Arsenal
            PlayerEntity(0, 1, "Bukayo Saka", "MID", 16, 12, 2900, 14.5, 11.2, 82.5, 2, 0, "Fit", "", false, false, 5),
            PlayerEntity(0, 1, "Martin Ødegaard", "MID", 11, 14, 3100, 9.8, 13.5, 88.0, 3, 0, "Fit", "", false, true, 4),
            PlayerEntity(0, 1, "Kai Havertz", "FWD", 14, 7, 2600, 13.8, 5.5, 79.1, 4, 0, "Fit", "", false, false, 4),
            PlayerEntity(0, 1, "Declan Rice", "MID", 7, 8, 3300, 5.2, 7.8, 90.2, 5, 0, "Fit", "", false, false, 3),
            PlayerEntity(0, 1, "William Saliba", "DEF", 2, 1, 3420, 1.5, 0.8, 93.4, 1, 0, "Fit", "", true, false, 2),
            
            // Liverpool
            PlayerEntity(0, 12, "Mohamed Salah", "FWD", 22, 13, 2800, 20.8, 12.1, 80.1, 1, 0, "Fit", "", false, false, 5),
            PlayerEntity(0, 12, "Luis Díaz", "FWD", 12, 8, 2500, 11.2, 7.1, 83.3, 2, 0, "Fit", "", false, false, 3),
            PlayerEntity(0, 12, "Alexis Mac Allister", "MID", 6, 7, 2850, 5.9, 6.8, 87.5, 6, 0, "Fit", "", true, false, 3),
            PlayerEntity(0, 12, "Trent Alexander-Arnold", "DEF", 3, 9, 2700, 4.1, 9.5, 79.8, 3, 0, "Doubtful", "Hamstring strain - 75% chance", false, true, 4),
            PlayerEntity(0, 12, "Virgil van Dijk", "DEF", 4, 2, 3200, 3.8, 1.5, 91.5, 2, 0, "Fit", "", false, false, 3),

            // Man City
            PlayerEntity(0, 13, "Erling Haaland", "FWD", 35, 5, 2850, 32.4, 4.2, 72.4, 1, 0, "Fit", "", false, false, 5),
            PlayerEntity(0, 13, "Kevin De Bruyne", "MID", 6, 15, 1900, 7.5, 16.2, 84.1, 2, 0, "Fit", "", false, true, 5),
            PlayerEntity(0, 13, "Phil Foden", "MID", 19, 11, 2800, 15.8, 10.5, 86.9, 2, 0, "Fit", "", false, false, 4),
            PlayerEntity(0, 13, "Rodri", "MID", 8, 9, 3200, 6.2, 8.5, 92.5, 7, 0, "Injured", "ACL knee injury - Out for season", false, false, 5),
            PlayerEntity(0, 13, "Josko Gvardiol", "DEF", 5, 3, 2900, 4.8, 2.9, 88.3, 3, 0, "Fit", "", true, false, 3),

            // Chelsea
            PlayerEntity(0, 6, "Cole Palmer", "MID", 25, 15, 3050, 21.5, 13.8, 83.2, 4, 0, "Fit", "", false, false, 5),
            PlayerEntity(0, 6, "Nicolas Jackson", "FWD", 16, 6, 2750, 17.2, 5.1, 75.6, 8, 0, "Fit", "", true, false, 4),
            PlayerEntity(0, 6, "Enzo Fernández", "MID", 5, 5, 2400, 4.2, 6.1, 86.8, 5, 0, "Fit", "", false, false, 3),
            PlayerEntity(0, 6, "Levi Colwill", "DEF", 1, 1, 2600, 1.1, 0.9, 89.5, 3, 0, "Fit", "", false, true, 3),

            // Man United
            PlayerEntity(0, 14, "Bruno Fernandes", "MID", 12, 11, 3200, 11.2, 12.5, 78.9, 6, 1, "Fit", "", false, false, 4),
            PlayerEntity(0, 14, "Marcus Rashford", "FWD", 10, 5, 2450, 9.5, 4.8, 77.2, 2, 0, "Fit", "", false, false, 3),
            PlayerEntity(0, 14, "Alejandro Garnacho", "FWD", 9, 6, 2300, 10.2, 5.2, 79.5, 3, 0, "Fit", "", false, true, 3),
            PlayerEntity(0, 14, "Kobbie Mainoo", "MID", 4, 3, 2100, 3.1, 2.8, 89.2, 4, 0, "Fit", "", true, false, 3),

            // Tottenham
            PlayerEntity(0, 18, "Heung-min Son", "FWD", 17, 10, 2750, 14.2, 9.1, 81.4, 1, 0, "Fit", "", false, false, 4),
            PlayerEntity(0, 18, "James Maddison", "MID", 8, 12, 2400, 7.8, 11.2, 82.9, 4, 0, "Fit", "", false, false, 3),
            PlayerEntity(0, 18, "Dominic Solanke", "FWD", 19, 4, 2950, 18.5, 3.8, 73.1, 2, 0, "Fit", "", true, false, 4),
            PlayerEntity(0, 18, "Cristian Romero", "DEF", 5, 0, 2800, 4.2, 0.5, 89.9, 9, 1, "Fit", "", false, true, 3),

            // Aston Villa
            PlayerEntity(0, 2, "Ollie Watkins", "FWD", 19, 13, 2980, 18.2, 11.5, 72.8, 2, 0, "Fit", "", false, false, 4),
            PlayerEntity(0, 2, "Leon Bailey", "MID", 10, 9, 2150, 8.5, 7.9, 78.4, 3, 0, "Fit", "", false, true, 3),
            PlayerEntity(0, 2, "John McGinn", "MID", 6, 5, 2800, 5.1, 5.4, 84.2, 8, 0, "Fit", "", true, false, 3),

            // Newcastle
            PlayerEntity(0, 15, "Alexander Isak", "FWD", 21, 4, 2400, 19.8, 3.5, 76.5, 1, 0, "Fit", "", false, false, 4),
            PlayerEntity(0, 15, "Anthony Gordon", "MID", 11, 10, 2850, 10.5, 9.2, 80.2, 6, 0, "Fit", "", false, false, 3),
            PlayerEntity(0, 15, "Bruno Guimarães", "MID", 7, 8, 3150, 6.1, 8.1, 88.6, 9, 0, "Fit", "", true, false, 4)
        )
        playerDao.insertPlayers(players)

        // 3. Insert Fixtures for Gameweek 1 (completed), Gameweek 2 (LIVE + scheduled), Gameweek 3 (scheduled)
        val currentTime = System.currentTimeMillis()
        val oneHour = 3600 * 1000L
        val oneDay = 24 * 3600 * 1000L

        val fixtures = listOf(
            // Gameweek 1 (All completed - FT)
            MatchFixtureEntity(101, 1, 13, 6, 2, 1, "FT", currentTime - 7 * oneDay, "Etihad Stadium", 4, 5, "Sky Sports", 58, 42, 16, 11, 7, 4, 540, 410, 89, 84, 11, 13, 1.85, 1.12),
            MatchFixtureEntity(102, 1, 1, 20, 3, 0, "FT", currentTime - 7 * oneDay, "Emirates Stadium", 1, 4, "TNT Sports", 64, 36, 19, 6, 9, 2, 620, 310, 91, 78, 8, 14, 2.45, 0.42),
            MatchFixtureEntity(103, 1, 12, 10, 2, 0, "FT", currentTime - 6 * oneDay, "Anfield", 1, 3, "Sky Sports", 61, 39, 14, 8, 6, 3, 580, 350, 88, 81, 9, 11, 1.95, 0.75),
            MatchFixtureEntity(104, 1, 14, 9, 1, 0, "FT", currentTime - 6 * oneDay, "Old Trafford", 2, 3, "Sky Sports", 51, 49, 12, 10, 5, 3, 490, 460, 84, 82, 12, 10, 1.35, 0.95),
            MatchFixtureEntity(105, 1, 18, 11, 2, 2, "FT", currentTime - 5 * oneDay, "Tottenham Stadium", 2, 3, "Sky Sports", 67, 33, 21, 9, 8, 4, 690, 310, 92, 75, 10, 15, 2.65, 1.25),
            MatchFixtureEntity(106, 1, 2, 16, 1, 1, "FT", currentTime - 5 * oneDay, "Villa Park", 2, 3, "Amazon Prime", 55, 45, 13, 11, 4, 5, 510, 420, 85, 80, 11, 12, 1.22, 1.15),
            MatchFixtureEntity(107, 1, 15, 17, 2, 1, "FT", currentTime - 5 * oneDay, "St. James' Park", 2, 3, "Sky Sports", 53, 47, 15, 12, 6, 4, 480, 440, 83, 81, 14, 9, 1.78, 1.32),
            MatchFixtureEntity(108, 1, 19, 2, 1, 2, "FT", currentTime - 4 * oneDay, "London Stadium", 3, 3, "TNT Sports", 48, 52, 11, 14, 4, 6, 420, 490, 81, 84, 13, 11, 1.05, 1.62),
            MatchFixtureEntity(109, 1, 8, 5, 0, 1, "FT", currentTime - 4 * oneDay, "Goodison Park", 3, 3, "Sky Sports", 42, 58, 8, 13, 2, 5, 380, 550, 76, 85, 15, 10, 0.65, 1.45),
            MatchFixtureEntity(110, 1, 7, 3, 2, 1, "FT", currentTime - 4 * oneDay, "Selhurst Park", 2, 3, "Amazon Prime", 50, 50, 14, 12, 5, 4, 450, 450, 82, 82, 12, 13, 1.55, 1.15),

            // Gameweek 2 (LIVE & scheduled)
            // Let's set some as FT, some as LIVE, some as SCHEDULED
            MatchFixtureEntity(201, 2, 6, 1, 1, 1, "LIVE", currentTime - 30 * 60 * 1000L, "Stamford Bridge", 4, 4, "Sky Sports", 52, 48, 8, 7, 4, 3, 280, 260, 85, 83, 6, 7, 0.85, 0.78), // active live match
            MatchFixtureEntity(202, 2, 12, 14, 2, 1, "LIVE", currentTime - 10 * 60 * 1000L, "Anfield", 4, 4, "Sky Sports", 56, 44, 9, 5, 5, 2, 310, 240, 87, 81, 4, 8, 1.12, 0.65), // active live match
            MatchFixtureEntity(203, 2, 13, 10, 3, 0, "FT", currentTime - oneDay, "Etihad Stadium", 1, 5, "Sky Sports", 72, 28, 24, 4, 11, 1, 740, 280, 93, 72, 6, 12, 3.12, 0.22),
            MatchFixtureEntity(204, 2, 18, 15, 2, 2, "FT", currentTime - oneDay, "Tottenham Stadium", 3, 3, "TNT Sports", 58, 42, 17, 14, 6, 5, 560, 410, 88, 81, 11, 14, 1.95, 1.68),
            MatchFixtureEntity(205, 2, 2, 19, 0, 0, "SCHEDULED", currentTime + 2 * oneHour, "Villa Park", 2, 3, "Sky Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0),
            MatchFixtureEntity(206, 2, 5, 14, 0, 0, "SCHEDULED", currentTime + 4 * oneHour, "Amex Stadium", 3, 2, "TNT Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0),
            MatchFixtureEntity(207, 2, 20, 7, 0, 0, "SCHEDULED", currentTime + 1 * oneDay, "Molineux Stadium", 3, 3, "Sky Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0),
            MatchFixtureEntity(208, 2, 3, 8, 0, 0, "SCHEDULED", currentTime + 1 * oneDay, "Vitality Stadium", 3, 3, "Sky Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0),
            MatchFixtureEntity(209, 2, 11, 16, 0, 0, "SCHEDULED", currentTime + 2 * oneDay, "King Power Stadium", 3, 3, "Sky Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0),
            MatchFixtureEntity(210, 2, 17, 4, 0, 0, "SCHEDULED", currentTime + 2 * oneDay, "St. Mary's Stadium", 3, 3, "Sky Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0),

            // Gameweek 3 (All Scheduled - Upcoming Big Matches)
            MatchFixtureEntity(301, 3, 1, 13, 0, 0, "SCHEDULED", currentTime + 7 * oneDay, "Emirates Stadium", 5, 5, "Sky Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0),
            MatchFixtureEntity(302, 3, 14, 12, 0, 0, "SCHEDULED", currentTime + 7 * oneDay + 2 * oneHour, "Old Trafford", 4, 4, "Sky Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0),
            MatchFixtureEntity(303, 3, 6, 18, 0, 0, "SCHEDULED", currentTime + 8 * oneDay, "Stamford Bridge", 3, 4, "TNT Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0),
            MatchFixtureEntity(304, 3, 15, 2, 0, 0, "SCHEDULED", currentTime + 8 * oneDay + 4 * oneHour, "St. James' Park", 3, 3, "Sky Sports", 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0)
        )
        matchFixtureDao.insertFixtures(fixtures)

        // 4. Match events for GW1 and active GW2 live matches
        val events = listOf(
            // Chelsea vs Arsenal (GW2 Live Match 201)
            MatchEventEntity(0, 201, 14, "GOAL", 6, "Cole Palmer", "", "Clinical penalty slotted into the bottom right corner. Gtech penalty shout upheld by VAR."),
            MatchEventEntity(0, 201, 28, "YELLOW_CARD", 1, "William Saliba", "", "Tactical foul pulling down Nicolas Jackson on counter attack."),
            MatchEventEntity(0, 201, 41, "GOAL", 1, "Bukayo Saka", "", "Stunning curler from the edge of the box. Assist: Martin Ødegaard."),

            // Liverpool vs Man United (GW2 Live Match 202)
            MatchEventEntity(0, 202, 8, "GOAL", 12, "Mohamed Salah", "", "Pushed in from close range after a rebound. Assist: Trent Alexander-Arnold."),
            MatchEventEntity(0, 202, 35, "GOAL", 12, "Luis Díaz", "", "Superb header from a whipped corner. Assist: Alexis Mac Allister."),
            MatchEventEntity(0, 202, 45, "GOAL", 14, "Bruno Fernandes", "", "Incredible long-range freekick over the wall into the top corner.")
        )
        for (event in events) {
            db.matchEventDao().insertEvent(event)
        }

        // 5. Summer 2026 Transfers (window active)
        val transfers = listOf(
            TransferEntity(0, "Viktor Gyökeres", "Sporting CP", "Arsenal", "£68M", "CONFIRMED", "Summer 2026", currentTime - 10 * oneDay),
            TransferEntity(0, "Nico Williams", "Athletic Club", "Chelsea", "£52M", "CONFIRMED", "Summer 2026", currentTime - 5 * oneDay),
            TransferEntity(0, "Joshua Kimmich", "Bayern Munich", "Manchester United", "£35M", "CONFIRMED", "Summer 2026", currentTime - 3 * oneDay),
            TransferEntity(0, "Kieran Trippier", "Newcastle", "Bayern Munich", "£12M", "CONFIRMED", "Summer 2026", currentTime - 15 * oneDay),
            TransferEntity(0, "Jamal Musiala", "Bayern Munich", "Manchester City", "£110M", "RUMOUR", "Summer 2026", currentTime - 2 * oneHour),
            TransferEntity(0, "Anthony Gordon", "Newcastle", "Liverpool", "£75M", "RUMOUR", "Summer 2026", currentTime - 4 * oneHour),
            TransferEntity(0, "Jarrad Branthwaite", "Everton", "Manchester United", "£70M", "RUMOUR", "Summer 2026", currentTime - 8 * oneHour),
            TransferEntity(0, "Marc Guéhi", "Crystal Palace", "Newcastle", "£65M", "RUMOUR", "Summer 2026", currentTime - 1 * oneDay)
        )
        transferDao.insertTransfers(transfers)

        // 6. Notifications Seed
        val notifications = listOf(
            NotificationEntity(0, "Welcome to PL 26/27!", "Stay tuned for real-time live score updates, detailed team sheets, transfers, and AI-powered tactics advice.", currentTime - 1 * oneDay, "INFO"),
            NotificationEntity(0, "GOAL! Chelsea 1-0 Arsenal", "Cole Palmer (14' Penalty) opens the scoring at Stamford Bridge!", currentTime - 30 * 60 * 1000L, "GOAL"),
            NotificationEntity(0, "GOAL! Liverpool 2-0 Man United", "Luis Díaz (35') doubles Liverpool's lead with a beautiful header!", currentTime - 15 * 60 * 1000L, "GOAL"),
            NotificationEntity(0, "GOAL! Liverpool 2-1 Man United", "Bruno Fernandes (45') pulls one back for United with a spectacular freekick!", currentTime - 1 * 60 * 1000L, "GOAL")
        )
        for (notif in notifications) {
            notificationDao.insertNotification(notif)
        }

        // 7. Default User Profile (Follows Arsenal - Team id 1)
        userProfileDao.saveUserProfile(UserProfileEntity(1, 1, true, true, true, 1))

        Log.d("DatabaseInitializer", "Premier League database successfully prepopulated.")
    }
}
