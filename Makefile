COMPOSE_FILE = ./infrastructure/docker-compose.yaml

run:
	./gradlew bootRun

postgres-up:
	docker compose -f $(COMPOSE_FILE) up -d postgres

postgres-down:
	docker compose -f $(COMPOSE_FILE) down postgres

todos:
	grep -r "TODO:"