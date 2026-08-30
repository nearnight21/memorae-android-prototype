param(
    [string]$ScreenshotPath = "captures/diagnostic-map-render.png",
    [double]$MinimumStdDev = 4.5
)

$ErrorActionPreference = "Stop"
. "D:\DevTools\Use-DevEnvironment.ps1"

$workspaceRoot = Split-Path -Parent $PSScriptRoot
$absoluteScreenshot = Join-Path $workspaceRoot $ScreenshotPath
$screenshotDirectory = Split-Path -Parent $absoluteScreenshot
New-Item -ItemType Directory -Path $screenshotDirectory -Force | Out-Null

adb shell screencap -p /sdcard/memorae-map-diagnostic.png | Out-Null
adb pull /sdcard/memorae-map-diagnostic.png $absoluteScreenshot | Out-Null

Add-Type -AssemblyName System.Drawing
$bitmap = [System.Drawing.Bitmap]::FromFile($absoluteScreenshot)
try {
    # Central map-only area: excludes status/header, timeline and navigation bar.
    $left = [int]($bitmap.Width * 0.10)
    $right = [int]($bitmap.Width * 0.90)
    $top = [int]($bitmap.Height * 0.18)
    $bottom = [int]($bitmap.Height * 0.76)
    $step = 12

    [double]$sum = 0
    [double]$sumSquared = 0
    [int]$count = 0

    for ($y = $top; $y -lt $bottom; $y += $step) {
        for ($x = $left; $x -lt $right; $x += $step) {
            $pixel = $bitmap.GetPixel($x, $y)
            $luminance = 0.2126 * $pixel.R + 0.7152 * $pixel.G + 0.0722 * $pixel.B
            $sum += $luminance
            $sumSquared += $luminance * $luminance
            $count++
        }
    }

    $mean = $sum / $count
    $variance = [Math]::Max(0, ($sumSquared / $count) - ($mean * $mean))
    $stdDev = [Math]::Sqrt($variance)
    $verdict = if ($stdDev -ge $MinimumStdDev) { "PASS" } else { "FAIL" }

    "MAP_RENDER=$verdict stddev=$([Math]::Round($stdDev, 2)) threshold=$MinimumStdDev samples=$count"
    if ($verdict -eq "FAIL") { exit 1 }
}
finally {
    $bitmap.Dispose()
}
