# Personal SMS

A private, single-user Android SMS app — a clean Material You replacement for the
stock messaging app. It becomes Android's **default SMS app** and sends/receives
real SMS over your phone's SIM. Everything stays on the device: **no cloud, no
backend, no internet permission at all.**

Built with Kotlin + Jetpack Compose (Material 3 / Material You), MVVM, Room, and
the Android telephony APIs. SMS only — MMS/picture messaging is intentionally out
of scope for v1 (see [Scope](#scope)).

---

## Requirements

- **Android Studio** (Ladybug / 2024.2 or newer recommended).
- **JDK 21** for the build. Android Gradle Plugin 8.5 supports JDK 17–21; this
  machine's default `JAVA_HOME` points at JDK 25 (too new), so the build is pinned
  to JDK 21 via `org.gradle.java.home` in [`gradle.properties`](gradle.properties).
  Adjust that path if your JDK 21 lives elsewhere.
- A **physical Android phone with a SIM** to actually test sending/receiving
  (see [Device vs emulator](#what-needs-a-real-device)).

## Open in Android Studio

1. **File → Open** and select this project folder (`android-sms`).
2. Set the Gradle JDK to 21: **Settings → Build, Execution, Deployment → Build
   Tools → Gradle → Gradle JDK → 21**. (This overrides `org.gradle.java.home` in
   the IDE.)
3. Let Gradle sync. First sync downloads Gradle 8.9 and the dependencies.

## Build a debug APK

From the project root (the Gradle wrapper downloads Gradle 8.9 on first run):

```bash
# macOS/Linux
./gradlew assembleDebug

# Windows (PowerShell / cmd)
gradlew.bat assembleDebug
```

If your shell's `JAVA_HOME` points at a JDK newer than 21, the pin in
`gradle.properties` still forces JDK 21 for the build. The APK lands at:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Install it on your phone (adb sideload)

1. Enable **Developer options → USB debugging** on the phone, connect via USB.
2. Confirm the device is visible:
   ```bash
   adb devices
   ```
3. Install (the `-r` reinstalls over an existing copy):
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

## Set it as your default SMS app

You must do this for it to **receive** SMS and read the message database.

- **In-app:** launch the app — the onboarding screen appears. Tap **Set as
  default** and confirm the system prompt.
- **Or manually:** **Settings → Apps → Default apps → SMS app → Messages.**

On first run it also requests runtime permissions (SMS, Contacts, Notifications)
with an explanation. If you deny any, the app still runs and degrades gracefully
(numbers instead of names, no notifications, etc.).

## What needs a real device

| Feature | Emulator | Real device (SIM) |
|---|---|---|
| Browse UI, theming, navigation | ✅ | ✅ |
| Set as default SMS app | ✅ | ✅ |
| **Send SMS over the SIM** | ❌ (no carrier) | ✅ |
| **Receive SMS** | ⚠️ only between two AVDs | ✅ |
| Delivery reports (✓✓ Delivered) | ❌ | ✅ (carrier-dependent) |
| Notifications + inline reply | ✅ (local) | ✅ |
| Scheduled send + reboot re-arm | ✅ | ✅ |

> You can send a fake incoming SMS to an emulator from the **Extended Controls →
> Phone** panel, or between two running AVDs using each other's port number. But
> real sending/receiving and delivery reports require a physical device with an
> active SIM.

## Permissions

Requested at runtime with rationale: `SEND_SMS`, `RECEIVE_SMS`, `READ_SMS`,
`READ_CONTACTS`, and `POST_NOTIFICATIONS` (Android 13+). `SCHEDULE_EXACT_ALARM`
(Android 12+) is requested for precise scheduled sends; without it, scheduling
falls back to an inexact alarm. No `INTERNET` permission is declared.

## Architecture

Single `:app` module, layered by responsibility:

- **`data/`** — system SMS provider access (`Telephony.Sms`/`Threads`) exposed as
  `ContentObserver`-backed Flows (incoming texts refresh the UI automatically),
  contacts resolver, SMS send/status, and a Room database that owns **only**
  app-private state: scheduled sends, drafts, archive flags. The system provider
  is the source of truth for conversations and messages.
- **`domain/`** — framework-free models and logic (OTP detection, search match
  highlighting, phone formatting, relative timestamps), covered by unit tests.
- **`ui/`** — Compose screens + ViewModels (MVVM), the Material You theme
  (dynamic color on Android 12+, falling back to the design palette), navigation.
- **Default-SMS contract** — `SmsDeliverReceiver` (SMS_DELIVER),
  `MmsWapPushReceiver` (WAP_PUSH stub), `ComposeSmsActivity` (sendto),
  `HeadlessSmsSendService` (respond-via-message), and the RoleManager flow.

Dependencies are wired manually (no DI framework) via `di/AppContainer`, held by
`SmsApp`.

### Running the tests

```bash
./gradlew test        # JVM unit tests for the domain layer
```

## Scope

**In v1:** conversation list, chat thread, real send/receive with multipart,
contacts, send/delivered status, notifications with inline reply, search,
Material You theming, new-message/contact picker, delete conversation/message,
swipe to archive/delete, OTP one-tap copy, scheduled send.

**Out of scope for v1:**
- **MMS / picture messaging** — the contract receiver `MmsWapPushReceiver` exists
  (required to be the default app) but is a no-op. MMS would slot in there: parse
  the WAP push PDU, download from the carrier MMSC, and persist to the
  `Telephony.Mms` tables.
- **Custom sender name / alphanumeric sender ID** — not possible over a SIM; not
  attempted.
- **Any cloud, account, or backend.**

## Project layout

```
app/src/main/java/com/personal/sms/
├── data/            system provider, contacts, Room, send/status, default-SMS manager
│   ├── local/       Room entities + DAOs + database
│   └── sms/         SmsProvider, SmsSender, cursor mappers
├── domain/          models + pure logic (OTP, search, phone, time)
├── notification/    MessageNotifier (MessagingStyle + direct reply)
├── receiver/        SMS_DELIVER, WAP_PUSH, sent/delivered, direct reply, scheduled, boot
├── scheduler/       ScheduledSender (exact alarm)
├── service/         HeadlessSmsSendService
├── ui/              Compose screens, ViewModels, theme, navigation
└── di/              AppContainer (manual DI)
```
