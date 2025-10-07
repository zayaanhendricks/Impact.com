# Impact.com
# Number Range Summarizer
## By Zayaan Hendricks

Java CLI that summarizes a sequence of integers by grouping **strictly monotonic runs** where each step is **+1 or −1**, preserving the original input order.

Example:
Input: 1,2,3,4,6,5,4,3,2
Output: 1-4, 6-2

## Requirements
- Java 11+
- Maven 3.8+

## Quick start
### Using the helper scripts:

#### macOS/Linux:
```
./setup.sh test
./setup.sh package
./setup.sh run "1,2,3,4,6,5,4,3,2"
./setup.sh interactive
``` 
#### Windows:

```
setup.bat test
setup.bat package
setup.bat run "1,2,3,4,6,5,4,3,2"
setup.bat interactive
```
## Build & Test using Maven (local)
```bash
mvn clean test           # run unit + data-driven tests
mvn clean package        # build JAR
```

## Run the CLI
Two ways:

### 1. Using the runnable JAR
```
java -jar target/number-range-summarizer-1.0.0.jar
# Interactive mode:
#   Enter comma-delimited numbers (or 'quit' to exit)
#   Example: 1,3,6,7,8,12,13,14,15

# One-shot input:
java -jar target/number-range-summarizer-1.0.0.jar "1,2,3,4,6,5,4,3,2"
# Result: 1-4, 6-2
```

### 2. Using classpath + main class
```
java -cp target/number-range-summarizer-1.0.0.jar numberrangesummarizer.NumberRangeSummarizerCLI "2,3,5,4"
# Result: 2-3, 5-4
```

## Docker 
Build and run with Docker Compose:

```
docker compose up --build
```

Run the CLI inside a container (interactive):

```
docker compose run number-range-summarizer
# then type inputs as prompted
```

Run tests in Docker:

```
docker compose run test-runner
```

Project structure
```Impact.com/
├── src/main/java/numberrangesummarizer/
│   ├── NumberRangeSummarizer.java
│   ├── NumberRangeSummarizerImpl.java
│   └── NumberRangeSummarizerCLI.java
├── src/test/java/numberrangesummarizer/
│   ├── NumberRangeSummarizerTest.java
│   └── DataDrivenTests.java
├── src/test/resources/
│   ├── test-cases.txt
│   ├── test-cases.json
│   └── test-cases.yaml
├── .github/workflows/
│   └── ci.yml
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── Makefile
├── setup.sh
├── setup.bat
├── .gitignore
└── README.md
```

## Notes
- Input order is preserved; the algorithm compresses only +1/−1 steps.
- Non-integer tokens produce a clear NumberFormatException.
- null input to summarizeCollection throws IllegalArgumentException.
- Blank input yields an empty collection/empty summary string.
