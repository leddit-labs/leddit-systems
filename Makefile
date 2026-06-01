
network-create:
	docker network create gameapi-net

network:
	docker network inspect gameapi-net >/dev/null 2>&1 || docker network create gameapi-net

db-up: network
	cd db && docker compose up -d --build

game-up: network
	cd REST-SOAP && docker compose up -d --build

grpc-up: network
	cd gRPC && docker compose up -d --build

keycloak-up: network
	cd keycloak && docker compose up

up: keycloak-up db-up game-up grpc-up

down:
	cd REST-SOAP && docker compose down
	cd db && docker compose down
	cd keycloak && docker compose down

