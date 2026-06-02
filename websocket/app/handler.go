package main

import (
	"database/sql"
	"log"

	"github.com/gorilla/websocket"
)

func handleMessage(db *sql.DB, ws *websocket.Conn, msg Message) {

	switch msg.Type {

	case "get_game":

		var game Game

		err := db.QueryRow(`
			SELECT id, name, description
			FROM game
			WHERE id = ?
		`, msg.Id).Scan(
			&game.Id,
			&game.Name,
			&game.Description,
		)

		if err != nil {
			log.Println(err)
			return
		}

		game.Type = "game"
		ws.WriteJSON(game)

	case "review":

		_, err := db.Exec(`
			INSERT INTO review (title, text, star_amount, user_id, game_id)
			VALUES (?, ?, ?, ?, ?)
		`,
			msg.Title,
			msg.Text,
			msg.StarAmount,
			msg.UserId,
			msg.GameId,
		)

		if err != nil {
			log.Println(err)
			return
		}

		broadcastMessage(msg)
	}
}