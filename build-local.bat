@echo off
set "REPO_ROOT=%~dp0"
set "JAVA_HOME=%REPO_ROOT%.tools\jdk8u492-b09"
set "GRADLE_HOME=%REPO_ROOT%.tools\gradle-2.14.1"
set "PATH=%JAVA_HOME%\bin;%GRADLE_HOME%\bin;%PATH%"

gradle -x getVersionJson -x downloadClient -x downloadServer %*
