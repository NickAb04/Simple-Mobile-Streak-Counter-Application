# Privacy Policy for RiyStreak

**Effective Date:** September 13, 2026

## 1. Overview
**RiyStreak** ("we", "our", or "the app") is a native Android habit tracker application designed to help users maintain daily habits using a streak counter with auto-protecting streak freezes. 

Your privacy is paramount. **RiyStreak is designed from the ground up as a 100% offline, local-only application.**

---

## 2. Information Collection and Storage

### 2.1 Personal Data
RiyStreak **does not collect, store, transmit, or process any personal data** on external servers or third-party databases.

### 2.2 Local Device Storage
All data entered into the application — including:
- Habit titles and descriptions
- Creation dates, current streak counts, and longest streak records
- Banked streak freezes and daily activity history (`DayLog`)
- Global app preferences (reminder time, notification toggles)

is stored **exclusively on your local device** using Android's encrypted Room Database and Jetpack DataStore preferences sandbox.

### 2.3 Accounts and Authentication
The app does not require, request, or create any user accounts, logins, email addresses, or authentication credentials.

---

## 3. Network and Third-Party Services

- **Zero Network Transmission:** RiyStreak does not execute network calls, API requests, or server communications.
- **Zero Third-Party SDKs:** The app contains no advertising SDKs, tracking frameworks, social logins, or third-party analytics libraries (such as Google Analytics or Firebase).
- **No Data Sharing:** Because no data is collected, no information is shared with third parties, advertisers, or analytics providers.

---

## 4. Android System Permissions

RiyStreak requests only minimal, necessary system permissions to deliver core device functionality:
- `android.permission.POST_NOTIFICATIONS`: Requested on Android 13+ (API 33+) to deliver local evening digested habit reminders. If denied, the app remains fully functional in silent mode.
- `android.permission.RECEIVE_BOOT_COMPLETED`: Used by Android WorkManager to reschedule local daily reminder alarms after device restart.

---

## 5. Data Retention and Security

- **User Control:** You retain full ownership and control over all data. Deleting a streak in the app permanently deletes its record from your local database.
- **App Uninstallation:** Uninstalling the app from your Android device will permanently remove all stored habit data and logs from your device.
- **Encrypted Backups:** If Android System Encrypted Backup (`android:allowBackup="true"`) is enabled on your device, your local database may be backed up to your personal private Google Drive device backup.

---

## 6. Children's Privacy
RiyStreak does not collect data from anyone, including children under the age of 13.

---

## 7. Support & External Links
Any support links (such as GitHub repository or voluntary developer tip jar links) are hosted externally on GitHub web pages. Following external links to GitHub is subject to GitHub's Privacy Policy.

---

## 8. Changes to This Privacy Policy
We may update our Privacy Policy from time to time. Any changes will be posted directly to this document on the project's public GitHub repository.

---

## 9. Contact Us
If you have any questions or feedback regarding this Privacy Policy, please open an issue on the official GitHub repository:
[https://github.com/NickAb04/Simple-Mobile-Streak-Counter-Application](https://github.com/NickAb04/Simple-Mobile-Streak-Counter-Application)
