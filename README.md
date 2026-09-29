# BusGo – Android UI (Kotlin + Jetpack Compose)

UI-only build of the Online Bus Tracking and Booking System. Every screen runs on
mock data (`data/mock/MockData.kt`), so you can click through the whole app
without a backend, map key or Stripe key.

## Run it
1. Unzip, then in Android Studio choose **File > Open** and pick this `BusGo` folder.
2. Let Gradle sync. The first sync downloads Gradle 9.5.0, so give it several minutes.
3. Run on an emulator or phone with Android 8.0 (API 26) or higher.

### Toolchain
| Thing | Version |
|---|---|
| Gradle | 9.5.0 |
| Android Gradle Plugin | 9.3.0 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.03.01 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 |
| Java | 17 |

AGP 9.3 requires Gradle 9.5.0 or newer, so those two move together.

`gradle.properties` sets `android.newDsl=false` and `android.builtInKotlin=false`.
AGP 9 otherwise compiles Kotlin itself and rejects the `org.jetbrains.kotlin.android`
plugin. Google removes this opt-out in AGP 10, so the eventual migration is:
delete those two lines, drop the `kotlin.android` plugin, and move `kotlinOptions`
into a top-level `kotlin { compilerOptions { } }` block.

The `gradlew` wrapper scripts are not included. To generate them, press Ctrl twice
in Android Studio and run `gradle wrapper`, then commit `gradlew`, `gradlew.bat`
and `gradle/wrapper/gradle-wrapper.jar`.

## Demo flow
Splash (animated bus + loading bar) > Welcome > **Get started** to register, or **Log in**.

Registering starts by choosing an account type:
- **Passenger:** account > emergency contact > travel preferences > Home.
  Then search town to town > results > Track live / Book seat > seat count dial >
  boarding & alighting stops (fare) > seat map > pay > QR e-ticket.
  Tabs: Home, Tickets, Alerts, Profile.
- **Bus:** owner/driver account > bus details (bus number, route number, from, to,
  bus type, seats, first trip, with a live preview card) > the bus's own screen
  (share GPS, scan tickets, report incident) showing the bus you just registered.

On the login screen, pick Passenger or Bus to jump straight to that side.

## Package structure
```
com.busgo.app
├── MainActivity.kt
├── navigation/        Routes.kt, BusGoNavGraph.kt
├── data/model/        Bus, Stop, Seat, Ticket, AlertItem, UserRole, BookingDraft ...
├── data/mock/         MockData.kt  (replace with API calls)
├── util/              Format.kt (LKR, dates, countdown)
└── ui/
    ├── theme/         Color, Type, Gradients, Theme
    ├── components/    PillButton, StepTopBar, DialPicker, GradientSlider, SelectChip,
    │                  SoftCard, SoftTextField, BusCard, QrCodeView, BottomNavBar, map/RouteMapCanvas ...
    └── screens/
        ├── splash/  welcome/  auth/  auth/register/  home/  search/  tracking/
        ├── booking/  payment/  ticket/  tickets/  alerts/  profile/
        └── driver/  emergency/  admin/
```

## Swapping mocks for real features later
| Placeholder | Replace with |
|---|---|
| `RouteMapCanvas` | osmdroid `MapView` inside `AndroidView`, positions from Socket.IO |
| Fake pay delay in `PaymentScreen` | Stripe Android SDK `PaymentSheet` |
| `QrCodeView` pattern | ZXing `QRCodeWriter` matrix (draw it the same way) |
| `ScanResultDialog` | CameraX + ML Kit barcode scanning |
| `MockData` | Retrofit calls to the Node.js REST API |
| Seat hold timer | Real Redis lock expiry returned by the booking API |

## App icon
The launcher icon is an adaptive vector icon (orange bus on navy):
- `res/drawable/ic_launcher_foreground.xml` – the bus
- `res/drawable/ic_launcher_background.xml` – navy background
- `res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`

To use your own image instead: right-click `res` → **New → Image Asset** →
Icon type *Launcher Icons (Adaptive and Legacy)* → choose your PNG/SVG → Next → Finish.
It overwrites these files. Uninstall the old app from the emulator if the icon doesn't update.
