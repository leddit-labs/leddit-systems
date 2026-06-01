

db-up:
	cd db && docker compose up -d --build

game-up:
	cd REST-SOAP &&	docker compose up -d --build

up: db-up game-up
