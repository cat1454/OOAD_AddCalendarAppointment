$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$classes = Join-Path $root "target/classes"
$h2 = Join-Path $root "lib/h2-2.2.224.jar"

if (!(Test-Path $h2)) {
    Write-Error "Missing H2 jar at $h2. Run Maven, or download h2-2.2.224.jar into lib/."
}

New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = Get-ChildItem -Path (Join-Path $root "src/main/java") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d $classes $sources
java -cp "$classes;$h2" com.ooad.calendar.CalendarApplication
