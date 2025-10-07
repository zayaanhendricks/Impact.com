@echo off
setlocal enabledelayedexpansion

set APP_JAR=target\number-range-summarizer-1.0.0.jar
set MAIN_CLASS=numberrangesummarizer.NumberRangeSummarizerCLI

if "%1"=="" goto :help

if /I "%1"=="test" (
  mvn -q clean test
  goto :eof
)

if /I "%1"=="package" (
  mvn -q clean package
  goto :eof
)

if /I "%1"=="run" (
  if not exist "%APP_JAR%" mvn -q clean package
  shift
  if "%1"=="" (
    echo Please provide a CSV input string, e.g. "1,2,3,4".
    exit /b 1
  )
  java -jar "%APP_JAR%" "%1%"
  goto :eof
)

if /I "%1"=="interactive" (
  if not exist "%APP_JAR%" mvn -q clean package
  java -jar "%APP_JAR%"
  goto :eof
)

if /I "%1"=="all" (
  shift
  mvn -q clean test
  mvn -q package
  if "%1"=="" (
    java -jar "%APP_JAR%"
  ) else (
    java -jar "%APP_JAR%" "%1%"
  )
  goto :eof
)

:help
echo Usage: %~n0 ^<test^|package^|run CSV^|interactive^|all [CSV]^>
echo.
echo   test          Run tests
echo   package       Build the JAR
echo   run CSV       Run once with CSV input
echo   interactive   Start interactive CLI
echo   all [CSV]     Clean, test, package, then run (optionally with CSV)
exit /b 0
