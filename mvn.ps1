#!/usr/bin/env pwsh
# Convenience wrapper to call Maven with correct JAVA_HOME
$env:JAVA_HOME = "C:\Users\gaura\.jdks\ms-21.0.12.1"
$env:PATH = "C:\Users\gaura\tools\apache-maven-3.9.6\bin;" + $env:PATH
mvn @args
