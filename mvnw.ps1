# Maven Wrapper for Windows (auto-downloads Maven if needed)
$ErrorActionPreference = "Stop"

$MVN_VERSION = "3.9.9"
$MVN_DIR = "$env:USERPROFILE\.m2\wrapper\apache-maven-$MVN_VERSION"
$MVN_CMD = "$MVN_DIR\bin\mvn.cmd"
$MVN_ZIP = "$env:TEMP\maven-$MVN_VERSION-bin.zip"

if (!(Test-Path $MVN_CMD)) {
    Write-Host "Downloading Maven $MVN_VERSION ..." -ForegroundColor Yellow
    $url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MVN_VERSION/apache-maven-$MVN_VERSION-bin.zip"
    New-Item -ItemType Directory -Force -Path $MVN_DIR | Out-Null
    Invoke-WebRequest -Uri $url -OutFile $MVN_ZIP
    Expand-Archive -Path $MVN_ZIP -DestinationPath (Split-Path $MVN_DIR) -Force
    Remove-Item $MVN_ZIP
    Write-Host "Maven ready!" -ForegroundColor Green
}

& $MVN_CMD @args
