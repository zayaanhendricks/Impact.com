#!/usr/bin/env bash
set -euo pipefail

APP_JAR="target/number-range-summarizer-1.0.0.jar"
MAIN_CLASS="numberrangesummarizer.NumberRangeSummarizerCLI"

usage() {
  cat <<EOF
Usage: $(basename "$0") [command] [args...]

Commands:
  test              Run unit + data-driven tests (mvn clean test)
  package           Build the JAR (mvn clean package)
  run [csv]         Run CLI once with a CSV input
  interactive       Start interactive CLI (type lines; 'quit' to exit)
  all [csv]         Clean, test, package, then run with optional CSV

Examples:
  $(basename "$0") test
  $(basename "$0") package
  $(basename "$0") run "1,2,3,4,6,5,4,3,2"
  $(basename "$0") interactive
  $(basename "$0") all "2,3,5,4"
EOF
}

cmd="${1:-}"
case "$cmd" in
  test)
    mvn -q clean test
    ;;
  package)
    mvn -q clean package
    ;;
  run)
    shift || true
    if [[ ! -f "$APP_JAR" ]]; then mvn -q clean package; fi
    if [[ -n "${1:-}" ]]; then
      java -jar "$APP_JAR" "$1"
    else
      echo "Please provide a CSV input string (e.g. \"1,2,3,4\")."
      exit 1
    fi
    ;;
  interactive)
    if [[ ! -f "$APP_JAR" ]]; then mvn -q clean package; fi
    java -jar "$APP_JAR"
    ;;
  all)
    shift || true
    mvn -q clean test
    mvn -q package
    if [[ -n "${1:-}" ]]; then
      java -jar "$APP_JAR" "$1"
    else
      java -jar "$APP_JAR"
    fi
    ;;
  ""|-h|--help|help)
    usage
    ;;
  *)
    echo "Unknown command: $cmd"
    echo
    usage
    exit 1
    ;;
esac
