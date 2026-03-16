COMPOSE_FILE = ./infrastructure/docker-compose.yaml

run:
	export $$(grep -v '^#' .env | xargs) && ./gradlew bootRun

postgres-up:
	docker compose -f $(COMPOSE_FILE) up -d postgres

postgres-down:
	docker compose -f $(COMPOSE_FILE) down postgres

up:
	docker compose -f $(COMPOSE_FILE) up -d --build

down:
	docker compose -f $(COMPOSE_FILE) down

todos:
	grep -r "TODO:"

apply-migrations:
	./gradlew flywayMigrate
	./gradlew flywayInfo
	./gradlew flywayValidate

test:
	./gradlew test
