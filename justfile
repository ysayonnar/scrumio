COMPOSE_FILE := "./infrastructure/docker-compose.yaml"

run:
    export $(grep -v '^#' .env | xargs) && ./gradlew bootRun

up:
    docker compose -f {{ COMPOSE_FILE }} up -d --build

down:
    docker compose -f {{ COMPOSE_FILE }} down

apply-migrations:
    ./gradlew flywayMigrate
    ./gradlew flywayInfo
    ./gradlew flywayValidate

test:
    ./gradlew test

demo-counter:
    @echo "--- Sending 10 parallel increments ---"
    for i in 1 2 3 4 5 6 7 8 9 10; do curl -s -o /dev/null -X POST "http://localhost:8080/api/v1/concurrency/counter/increment" & done; wait
    @echo "--- Final counter values ---"
    curl -s "http://localhost:8080/api/v1/concurrency/counter"
    @echo ""

front:
    cd frontend && npm install && npm run dev

test-rps:
    rm -f jmeter/results.jtl
    rm -rf jmeter/report
    jmeter -n \
    -j logs/jmeter.log \
    -t jmeter/business-load-test.jmx \
    -l jmeter/results.jtl \
    -e -o jmeter/report
