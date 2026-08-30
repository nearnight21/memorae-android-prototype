# Memorae Android Home Prototype

Throwaway visual prototype for one question: can a live AMap `TextureMapView`
support a convincing, restrained Smoke Crystal timeline on an Android device?

This phase contains no product data, persistence, navigation, login, sync, or
backend integration. All memory points are fake and live only in memory.

## Requirements

- Android 13 / API 33 or newer
- AMap Android key bound to package `com.memorae.prototype`
- Debug certificate SHA-1:
  `2A:F9:39:70:35:BF:37:94:84:1F:CC:94:52:B7:72:E1:B7:28:62:F1`

The key is read from `amap.key` in the ignored `local.properties` file, the
`AMAP_KEY` Gradle property, or the `AMAP_KEY` environment variable. It must not
be committed to the project.

## Build and install

```powershell
. 'D:\DevTools\Use-DevEnvironment.ps1'
Add-Content .\local.properties 'amap.key=<your key>'
.\gradlew.bat --offline assembleDebug
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

The app deliberately shows a one-time in-memory privacy gate before creating
the AMap SDK. Accepting it calls the required AMap privacy compliance APIs.

## Prototype structure

- `prototype/HomePrototypeActivity.kt`: single prototype activity and privacy gate
- `map/PrototypeMap.kt`: live Shanghai satellite map and native map gestures
- `memory/FakeMemoryMarker.kt`: programmatically drawn fake photo markers
- `timeline/SmokeCrystalTimeline.kt`: timeline geometry, labels, glass edges
- `timeline/SmokeCrystalShader.kt`: independent live `RuntimeShader` material layer
- `res/raw/smoke_crystal.agsl`: procedural smoke, absorption, edge light and drag response
- `timeline/SmokeCrystalSpec.kt`: centralized experimental tuning parameters

The shader is deliberately rendered as a transparent material layer rather than
as a `RenderEffect` on the map. Applying a render effect directly to AMap's
`TextureView` produced an opaque gray surface on the target Redmi device. This
keeps the map live and avoids screenshot-based blur.

AMap world-vector loading is enabled, but the bundled key must also have the
paid world-map entitlement before the camera can be moved back to Tokyo.
