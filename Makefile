SHELL := /bin/sh

.PHONY: help build run up down logs ps test clean

help:
	@echo "Targets:"
	@echo "  make build  - Build Docker image"
	@echo "  make run    - Run container on :8080"
	@echo "  make up     - Start docker-compose stack"
	@echo "  make down   - Stop docker-compose stack"
	@echo "  make logs   - Tail docker-compose logs"
	@echo "  make ps     - Show docker-compose services"
	@echo "  make test   - Run Gradle tests"
	@echo "  make clean  - Stop stack and remove local image"

build:
	docker build -t what3words-test:latest .

run: build
	docker run --rm -p 8080:8080 --name emergencyapi \
		-e WHAT3WORDS_API_BASE_URL=https://api.what3words.com/v3 \
		-e WHAT3WORDS_API_KEY=$${WHAT3WORDS_API_KEY} \
		what3words-test:latest

up:
	docker compose up -d --build

down:
	docker compose down

logs:
	docker compose logs -f emergencyapi

ps:
	docker compose ps

test:
	./gradlew test

clean:
	docker compose down --remove-orphans
	docker rmi what3words-test:latest >/dev/null 2>&1 || true
