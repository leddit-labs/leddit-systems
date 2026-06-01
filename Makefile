
network-create:
	docker network create gameapi-net

db-up:
	cd db && docker compose up -d --build

game-up:
	cd REST-SOAP &&	docker compose up -d --build

grpc-up:
	cd gRPC && docker compose up -d --build

up: network-create db-up game-up grpc-up
