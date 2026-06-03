
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
	cd REST-SOAP/keycloak && docker compose up -d

websocket-up: network
	cd websocket && docker compose up -d --build

graphql-up: network
	cd GraphQL && docker compose up -d --build

frontend-up: network
	cd REST-SOAP/frontend && docker compose up -d --build

up: keycloak-up db-up game-up grpc-up websocket-up graphql-up frontend-up

down:
	cd REST-SOAP && docker compose down
	cd REST-SOAP/keycloak && docker compose down
	cd REST-SOAP/frontend && docker compose down
	cd db && docker compose down
	cd gRPC && docker compose down
	cd websocket && docker compose down
	cd GraphQL && docker compose down

