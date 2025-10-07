.PHONY: help build test clean run run-interactive package docker-build docker-run coverage quality install

# Variables
APP_NAME = number-range-summarizer
VERSION = 1.0.0
JAR_FILE = target/$(APP_NAME)-$(VERSION)-jar-with-dependencies.jar
DOCKER_IMAGE = $(APP_NAME):$(VERSION)

# Colors for output
GREEN = \033[0;32m
YELLOW = \033[1;33m
NC = \033[0m # No Color

help: ## Display this help message
	@echo "$(GREEN)Number Range Summarizer - Build Commands$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}'

install: ## Install Maven dependencies
	@echo "$(GREEN)Installing dependencies...$(NC)"
	mvn clean install -DskipTests

build: ## Compile the project
	@echo "$(GREEN)Building project...$(NC)"
	mvn clean compile

test: ## Run all tests
	@echo "$(GREEN)Running tests...$(NC)"
	mvn clean test

test-verbose: ## Run tests with verbose output
	@echo "$(GREEN)Running tests with verbose output...$(NC)"
	mvn clean test -X

test-data: ## Run only data-driven tests
	@echo "$(GREEN)Running data-driven tests...$(NC)"
	mvn test -Dtest=DataDrivenTests

package: ## Package the application
	@echo "$(GREEN)Packaging application...$(NC)"
	mvn clean package

run: package ## Run with sample input
	@echo "$(GREEN)Running application with sample input...$(NC)"
	java -jar $(JAR_FILE) "1,3,6,7,8,12,13,14,15,21,22,23,24,31"

run-interactive: package ## Run in interactive mode
	@echo "$(GREEN)Starting interactive mode...$(NC)"
	java -jar $(JAR_FILE)

run-custom: package ## Run with custom input (use INPUT variable)
	@echo "$(GREEN)Running with custom input: $(INPUT)$(NC)"
	java -jar $(JAR_FILE) "$(INPUT)"

coverage: ## Generate code coverage report
	@echo "$(GREEN)Generating coverage report...$(NC)"
	mvn clean test jacoco:report
	@echo "$(GREEN)Coverage report generated at: target/site/jacoco/index.html$(NC)"

coverage-open: coverage ## Generate and open coverage report
	@open target/site/jacoco/index.html || xdg-open target/site/jacoco/index.html

quality: ## Run code quality checks
	@echo "$(GREEN)Running code quality checks...$(NC)"
	mvn spotbugs:check checkstyle:check

verify: ## Run full verification (tests + quality checks)
	@echo "$(GREEN)Running full verification...$(NC)"
	mvn clean verify

clean: ## Clean build artifacts
	@echo "$(GREEN)Cleaning build artifacts...$(NC)"
	mvn clean
	rm -rf target/

docker-build: ## Build Docker image
	@echo "$(GREEN)Building Docker image...$(NC)"
	docker build -t $(DOCKER_IMAGE) .

docker-run: docker-build ## Run Docker container with sample input
	@echo "$(GREEN)Running Docker container...$(NC)"
	docker run $(DOCKER_IMAGE) "1,3,6,7,8,12,13,14,15"

docker-interactive: docker-build ## Run Docker container in interactive mode
	@echo "$(GREEN)Starting Docker container in interactive mode...$(NC)"
	docker run -it $(DOCKER_IMAGE)

docker-compose-up: ## Start services with docker-compose
	@echo "$(GREEN)Starting Docker Compose services...$(NC)"
	docker-compose up --build

docker-compose-down: ## Stop docker-compose services
	@echo "$(GREEN)Stopping Docker Compose services...$(NC)"
	docker-compose down

benchmark: package ## Run performance benchmark
	@echo "$(GREEN)Running performance benchmark...$(NC)"
	@for i in 100 1000 10000; do \
		echo "Testing with $$i numbers..."; \
		seq -s, 1 $$i | xargs java -jar $(JAR_FILE); \
	done

lint: ## Run checkstyle linting
	@echo "$(GREEN)Running checkstyle...$(NC)"
	mvn checkstyle:check

spotbugs: ## Run SpotBugs static analysis
	@echo "$(GREEN)Running SpotBugs...$(NC)"
	mvn spotbugs:check spotbugs:gui

ci: clean install test quality package ## Run full CI pipeline locally
	@echo "$(GREEN)✓ CI pipeline completed successfully!$(NC)"

release: ci ## Create release artifacts
	@echo "$(GREEN)Creating release artifacts...$(NC)"
	mkdir -p release
	cp $(JAR_FILE) release/
	cp README.md release/
	@echo "$(GREEN)✓ Release artifacts created in release/ directory$(NC)"

# Development helpers
watch-test: ## Watch for changes and run tests
	@echo "$(GREEN)Watching for changes...$(NC)"
	@while true; do \
		inotifywait -e modify -r src/ && mvn test; \
	done

format: ## Format code (if formatter configured)
	@echo "$(GREEN)Formatting code...$(NC)"
	mvn formatter:format

all: clean install test package ## Build everything from scratch
	@echo "$(GREEN)✓ Build completed successfully!$(NC)"

.DEFAULT_GOAL := help