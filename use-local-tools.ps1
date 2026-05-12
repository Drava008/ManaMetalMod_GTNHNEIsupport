$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$jdkHome = Join-Path $repoRoot ".tools\jdk8u492-b09"
$gradleHome = Join-Path $repoRoot ".tools\gradle-2.14.1"

$env:JAVA_HOME = $jdkHome
$env:GRADLE_HOME = $gradleHome
$env:Path = (Join-Path $jdkHome "bin") + ";" + (Join-Path $gradleHome "bin") + ";" + $env:Path

Write-Host "JAVA_HOME=$env:JAVA_HOME"
java -version
javac -version
gradle -v
