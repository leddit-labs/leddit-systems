

network:
	docker network inspect gameapi-net >/dev/null 2>&1 || docker network create gameapi-net

db-up: network
	cd db && docker compose up -d --build

game-up: network
	cd REST-SOAP && docker compose up -d --build

up: db-up game-up

down:
	cd REST-SOAP && docker compose down
	cd db && docker compose down
