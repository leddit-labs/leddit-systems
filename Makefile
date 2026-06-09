
network-create:
	docker network create gameapi-net

network:
	docker network inspect gameapi-net >/dev/null 2>&1 || docker network create gameapi-net

db-up: network
	cd db && docker compose up -d --build

rest-up: network
	cd REST && docker compose up -d --build

soap-up: network
	cd SOAP && docker compose up -d --build

grpc-up: network
	cd gRPC && docker compose up -d --build

keycloak-up: network
	cd REST/keycloak && docker compose up -d

websocket-up: network
	cd websocket && docker compose up -d --build

graphql-up: network
	cd GraphQL && docker compose up -d --build

frontend-up: network
	cd REST/frontend && docker compose up -d --build

up: keycloak-up db-up rest-up soap-up grpc-up websocket-up graphql-up frontend-up

down:
	cd REST && docker compose down
	cd SOAP && docker compose down
	cd REST/keycloak && docker compose down
	cd REST/frontend && docker compose down
	cd db && docker compose down
	cd gRPC && docker compose down
	cd websocket && docker compose down
	cd GraphQL && docker compose down

