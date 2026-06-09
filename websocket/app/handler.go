package main

import (
	"database/sql"
	"log"

	"github.com/gorilla/websocket"
)

func sendError(
	ws *websocket.Conn,
	code int,
	message string,
) {

	err := ws.WriteJSON(ErrorResponse{
		Type:    "error",
		Code:    code,
		Message: message,
	})

	if err != nil {
		log.Println("failed to send error:", err)
	}
}

func handleMessage(
	db *sql.DB,
	ws *websocket.Conn,
	msg Message,
) {

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
		if err == sql.ErrNoRows {
			sendError(
				ws,
				404,
				"game not found",
			)
			return
		}

		if err != nil {
			log.Println(err)
			sendError(
				ws,
				500,
				"database error",
			)
			return
		}

		game.Type = "game"
		err = ws.WriteJSON(game)
		if err != nil {
			log.Println(err)
		}

	case "review":

		_, err := db.Exec(`
			INSERT INTO review (
				title,
				text,
				star_amount,
				user_id,
				game_id
			)
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
			sendError(
				ws,
				500,
				"failed to save review",
			)
			return
		}

		review := ReviewMessage{
			Type:       "review",
			Title:      msg.Title,
			Text:       msg.Text,
			StarAmount: msg.StarAmount,
			UserId:     msg.UserId,
			GameId:     msg.GameId,
		}

		broadcastReview(review)
	}
}