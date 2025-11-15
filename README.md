📊 Budget App
A cross‑platform budgeting app built with Kotlin Multiplatform + Jetpack Compose. Features include transaction input, monthly summaries, category management, and historical tracking.

✨ Features
Add, edit, and delete transactions

Categorize income and expenses

Month/year selector with pull‑to‑refresh

Dashboard with totals (Income, Expense, Net)

Backend API integration for persistence

🖼️ Mockups (Textual Wireframes)
Dashboard Screen
Code
-------------------------------------------------
| November 2025                                 |
| [Refresh] [Pick Month]                        |
-------------------------------------------------
| Income:   $3,200                              |
| Expense:  $2,100                              |
| Net:      $1,100                              |
-------------------------------------------------
| Transactions                                  |
|-----------------------------------------------|
| Groceries      -$120   [Delete]               |
| Rent           -$950   [Delete]               |
| Salary        +$3,200  [Delete]               |
-------------------------------------------------
[ + Add Transaction ]
Add Transaction Screen
Code
-------------------------------------------------
| Add Transaction   [Back] [Save]               |
-------------------------------------------------
| Amount: [___________]                         |
| Category: [Dropdown ▾]                        |
|   - Groceries                                 |
|   - Rent                                      |
|   - Utilities                                 |
|   - ➕ Create new category                     |
-------------------------------------------------
Month/Year Picker
Code
-------------------------------------------------
| Select Month & Year                           |
-------------------------------------------------
| Month: [November ▾]                           |
| Year:  [2025]                                 |
-------------------------------------------------
[ OK ]   [ Cancel ]
🔌 API Endpoints
Transactions
GET /transactions?month=YYYY-MM → List transactions for a month

POST /transactions → Add a new transaction

json
{
  "id": "uuid",
  "amount": 120.0,
  "categoryId": "groceries",
  "categoryType": "EXPENSE",
  "date": "2025-11"
}
DELETE /transactions/{id} → Delete a transaction

Categories
GET /categories → List categories

POST /categories → Create a new category

json
{
  "name": "Groceries",
  "type": "EXPENSE"
}
🚀 Tech Stack
Frontend: Kotlin Multiplatform, Jetpack Compose

Backend: Ktor (REST API)

State Management: StateFlow in ViewModel

UI: Material3 with pullToRefresh

📱 Roadmap
Authentication (optional)

Export summaries (CSV/PDF)

Premium analytics (spending trends, forecasts)


---------------------------------------------------------------------------------------------------------------
This is a Kotlin Multiplatform project targeting Android, iOS, Server.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
      folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/server](./server/src/main/kotlin) is for the Ktor server application.

* [/shared](./shared/src) is for the code that will be shared between all targets in the project.
  The most important subfolder is [commonMain](./shared/src/commonMain/kotlin). If preferred, you
  can add code to the platform-specific folders here too.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Server

To build and run the development version of the server, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :server:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :server:run
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
