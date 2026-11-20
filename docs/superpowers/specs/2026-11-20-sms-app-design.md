# Personal SMS App — Design

**Date:** 2026-11-20
**Status:** Awaiting approval

## Intent & why
A private, single-user replacement for the stock Android messaging app. Installed only on the owner's phone, never published, never networked. It becomes Android's **default SMS app** so it can send/receive real SMS over the SIM and read the system SMS database. Success = it looks like the six provided mockups, reliably sends/receives texts, and feels like a clean modern Material You app rather than enterprise software.

## Goals
- Full default-SMS-app contract (so receive + DB read actually work).
- Send & receive real SMS over the SIM, including multipart (long) messages.
- Match the six mockups closely in light and dark, Material You dynamic color.
- Complete v1 feature list (10 items) + the 3 polish items.
- Compiles cleanly to a debug APK on this machine (verified via Gradle wrapper).

## Non-goals (v1)
- MMS / picture messaging — contract receivers stubbed; code commented where MMS would slot in.
- Custom sender name / alphanumeric sender ID — impossible over a SIM; not attempted.
- Any cloud, account, backend, or third-party networking library.

## Anti-goals (would make it a failure even if it "works")
- Requiring a backend or internet to function.
- Silently losing/failing sends without surfacing status.
- A UI that visibly diverges from the mockups (wrong palette, fake status bar, tiny touch targets).

## Constraints
- Kotlin, 100% Jetpack Compose, Material 3 / Material You. Gradle Kotlin DSL.
- minSdk 24, target/compile SDK 34.
- MVVM + coroutines/Flow; Room for app-owned state only.
- No internet permission at all.
- Build pinned to **JDK 21** (installed JDK 25 is too new for current AGP). Gradle wrapper supplies Gradle.

## Approaches considered
1. **Single `:app` module, internally layered `data/domain/ui`** — *chosen.* Matches "clean result over ceremony"; enough separation to keep provider/Room/telephony code out of the UI without multi-module build overhead.
2. Multi-module (`:core`, `:data`, `:ui`, …) — rejected: real separation benefits only appear with a team / large surface; adds Gradle ceremony the owner explicitly doesn't want.
3. Store a full local mirror of all SMS in Room — rejected: the system provider *is* the source of truth for a default SMS app; mirroring invites divergence. Room holds only what the provider can't (schedules, drafts, archive flags).

## Architecture
Single `:app` module, packages:
- `data/` — system SMS provider access (`Telephony.Sms`, `Telephony.Threads`), contacts resolver, Room DB (scheduled sends, drafts, archive flags), SmsManager send wrapper, repositories.
- `domain/` — models (Conversation, Message, Contact, ScheduledMessage), use-case-ish logic (OTP detection, thread/message mapping, search), kept free of Android framework types where practical.
- `ui/` — Compose screens, ViewModels, theme (Material You + fallback palette), navigation.
- Android components: `SmsDeliverReceiver` (SMS_DELIVER), `MmsWapPushReceiver` (WAP_PUSH_DELIVER, stub), `ComposeSmsActivity` (sendto/ intent), `HeadlessSmsSendService`, `SmsSentReceiver`/`SmsDeliveredReceiver` (status PendingIntents), `ScheduledSendReceiver` (AlarmManager), notification + direct-reply receiver.

**Threads/messages** read live from the system provider via `ContentResolver` + `ContentObserver` → Flow, so incoming texts refresh the UI automatically. **Writing** received/sent messages into the provider (required of the default app) handled in the deliver/sent paths.

## Key decisions (my calls)
- **Inter bundled** as a font resource (mockups clearly use it); M3 type scale maps to it.
- **Scheduled send: AlarmManager `setExactAndAllowWhileIdle`**, requesting `SCHEDULE_EXACT_ALARM` on Android 12+. Pending schedules survive reboot via a `BOOT_COMPLETED` receiver that re-arms alarms from Room.
- **Swipe on a conversation row → delete + archive**; archived threads reached from the list overflow menu. Delete/message-delete confirmed via dialog.
- **OTP detection**: regex over recent inbound bodies; a "Copy" chip renders inline in the list row (as mockup) and in-thread.
- **Permissions**: runtime requests with rationale screens for SEND/RECEIVE/READ_SMS, READ_CONTACTS, POST_NOTIFICATIONS; each feature degrades gracefully when denied (e.g. no contacts → show numbers; no notif permission → no shade alert but app still works).
- **Default-SMS onboarding**: dedicated screen (mockup 6) using RoleManager (`ROLE_SMS`) on API 29+ with the legacy `ACTION_CHANGE_DEFAULT` fallback below.

## Screens (to mockups)
1. Conversations list — big "Messages" title, avatar/initials chips, search field, unread badge, OTP Copy chip, "Scheduled · time" row, FAB "Compose", swipe actions.
2. Conversation thread — header with avatar/name/number + call + overflow, date divider, asymmetric chat bubbles (sent indigo/white, received surface), delivery ticks + "Delivered · time", composer with +, clock (schedule), send FAB.
3. Same thread in dark (background `#121018`, surface `#1C1A24`, received `#2A2735`).
4. New message — "To:" field, contact picker icon, SUGGESTED + alphabetical sections, type-a-number.
5. Search — rounded search bar, CONTACTS + MESSAGES sections, highlighted query matches.
6. Default-SMS onboarding — icon, headline, three feature rows, "Set as default" / "Not now".

Palette tokens exactly as spec: light bg `#FAF8FE`, received `#EDEBF4`, sent `#5A4BE0`/white; dark bg `#121018`, surface `#1C1A24`, received `#2A2735`, primary `#6457E6`, accent `#B4A8FF`. Bubble radius ~20dp w/ 6dp asymmetric corner; rows/cards 18dp; FAB/send soft shadow. Material You dynamic color on 12+ overrides tokens; tokens are the <12 fallback.

## Left to the builder
- Exact Compose component decomposition and nav graph.
- Dependency versions (pinned to a known-good AGP/Kotlin/Compose BOM set that works with JDK 21).
- OTP regex specifics and copy-chip timing.
- Room schema details for schedules/drafts/archive.
- README contents (open in Studio, build debug APK, adb sideload, set-as-default steps, real-device-vs-emulator notes).

## Verification
- `./gradlew assembleDebug` must succeed on this machine (compile-verify, per owner's choice).
- SMS send/receive and real notifications require the owner's physical phone — called out in README; not testable here.
