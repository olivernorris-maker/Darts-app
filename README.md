# PDC Darts Companion App

A personal, read-only Android companion app for following PDC darts, the Modus Super Series, and the PDC Women's Series.

---

## Quick Start: Setting Up Your Google Sheet

### 1. Create the spreadsheet

Create one Google Sheets spreadsheet with **five tabs** named exactly as shown below. Each tab must have a header row matching the column names.

### 2. Publish each tab as CSV

For each tab:
1. **File → Share → Publish to web**
2. Change the first dropdown from "Entire Document" to the **tab name**
3. Change the format dropdown to **"Comma-separated values (.csv)"**
4. Click **Publish** and copy the URL

### 3. Paste the URLs

Open `app/src/main/java/com/pdcdarts/app/config/CsvUrls.kt` and replace each `REPLACE_ME` with the correct published CSV URL:

```kotlin
const val CALENDAR     = "https://docs.google.com/spreadsheets/d/YOUR_ID/export?format=csv&gid=0"
const val MATCHES      = "https://docs.google.com/spreadsheets/d/YOUR_ID/export?format=csv&gid=1"
const val DRAW         = "https://docs.google.com/spreadsheets/d/YOUR_ID/export?format=csv&gid=2"
const val RANKINGS     = "https://docs.google.com/spreadsheets/d/YOUR_ID/export?format=csv&gid=3"
const val COMPETITIONS = "https://docs.google.com/spreadsheets/d/YOUR_ID/export?format=csv&gid=4"
```

---

## Sheet Structure

### Tab 1 — Calendar

| Column | Description |
|--------|-------------|
| Tournament | Tournament name (must match exactly across all tabs) |
| Category | `Major` / `ProTour` / `EuroTour` / `PremierLeague` / `Modus` / `Womens` / `Other` |
| StartDate | e.g. `1 Feb 2025` or `2025-02-01` |
| EndDate | Same format as StartDate |
| Venue | Venue name |
| City | City |
| Country | Country |
| Status | `Upcoming` / `Live` / `Completed` |
| Notes | Any free-text notes (optional) |

### Tab 2 — Matches

| Column | Description |
|--------|-------------|
| Tournament | Must match Calendar tab |
| Round | e.g. `Round 1`, `Quarter-Final`, `Final` |
| Player1 | Player name |
| Player2 | Player name |
| Date | e.g. `1 Feb 2025` or `2025-02-01` |
| Time | UK time, e.g. `19:00` |
| Session | `Afternoon` or `Evening` |
| MatchID | Unique ID, e.g. `M001` (used to link Draw tab) |
| Status | `Scheduled` or `Completed` |
| Result | Score, e.g. `6–3` (leave blank if not played) |
| Winner | Winning player's name (leave blank if not played) |

### Tab 3 — Draw

| Column | Description |
|--------|-------------|
| Tournament | Must match Calendar tab |
| Round | Round name, e.g. `Round 1` |
| RoundOrder | `1` = first round, ascending to final |
| MatchID | Unique ID matching Matches tab |
| SlotTop | Player name, `TBD`, or `Winner of M001` |
| SlotBottom | Player name, `TBD`, or `Winner of M001` |
| ScheduledDate | Date of this match |
| ScheduledTime | UK time |
| Session | `Afternoon` or `Evening` |
| Winner | Winner's name once known (leave blank until played) |
| FeedsIntoMatchID | MatchID of the next match the winner advances to (blank for the final) |

**How the bracket works:** `RoundOrder` determines column order (left = earliest round). `FeedsIntoMatchID` builds the tree — the app follows these links to advance winners into future slots and to highlight a player's projected route.

### Tab 4 — Rankings

| Column | Description |
|--------|-------------|
| RankingType | `OrderOfMerit` / `ProTour` / `Womens` / `Development` |
| Position | Integer rank |
| Player | Player name |
| Country | Country/nationality |
| Points | Points total |
| Change | Position change, e.g. `+2`, `-1`, or blank |

### Tab 5 — Competitions

| Column | Description |
|--------|-------------|
| Tournament | Must match Calendar tab |
| Format | e.g. `Best of 11 legs` |
| Field | e.g. `128 players` |
| PrizeFund | e.g. `£500,000` |
| DefendingChampion | Player name |
| Dates | Human-readable date range |
| Venue | Venue name |
| Broadcast | e.g. `Sky Sports / DAZN` |
| Notes | Any additional info |

---

## Screens

| Screen | Description |
|--------|-------------|
| **Tonight** | Today's matches and the next few upcoming, grouped by tournament |
| **Calendar** | Full season filterable by category. Tap a tournament to open its detail. |
| **Tournament Detail** | Venue, dates, prize fund, format, defending champion. Tap "View Bracket" if a draw exists. |
| **Bracket** | Zoomable/pannable knockout bracket. Tap any player to highlight their entire route. |
| **Results** | Completed matches, most recent first |
| **Rankings** | Order of Merit, Pro Tour, Women's, Development — toggled by tab |

---

## Getting the APK (no Android Studio needed)

Every push triggers a GitHub Actions build. To download the APK:

1. Go to your repository on GitHub
2. Click **Actions** → select the latest **Build Debug APK** run
3. Scroll down to **Artifacts** and download `PDCDarts-debug-<run_number>`
4. Unzip and sideload `app-debug.apk` to your Samsung Galaxy S23

> Enable **Settings → Apps → Install unknown apps** for whichever app you use to open the APK (e.g. Files).

---

## Building Locally (optional)

```bash
# Requires Android Studio or SDK command line tools with JDK 17
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

---

## Data Behaviour

- **Refresh:** Pull down on any screen to force a fresh fetch
- **Cache:** Data is cached locally; the "last updated" timestamp is shown at the top of each screen
- **Offline:** If no network is available, the most recent cached data is shown with a wifi-off indicator
- **Missing data:** Blank cells display as "TBD" — the app never crashes on missing values
- **Times:** All times are displayed in UK timezone (Europe/London, BST/GMT automatically)
