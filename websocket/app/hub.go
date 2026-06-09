package main

import (
	"encoding/json"
	"html"
	"log"
	"sync"

	"github.com/gorilla/websocket"
)

var clients = make(map[*websocket.Conn]bool)
var mutex sync.Mutex

func addClient(conn *websocket.Conn) {
	mutex.Lock()
	clients[conn] = true
	mutex.Unlock()
}

func removeClient(conn *websocket.Conn) {
	mutex.Lock()
	delete(clients, conn)
	mutex.Unlock()
}

func broadcastReview(msg ReviewMessage) {
	safeMsg := sanitizeReview(msg)

	data, err := json.Marshal(safeMsg)
	if err != nil {
		log.Println("failed to marshal message:", err)
		return
	}

	mutex.Lock()
	defer mutex.Unlock()

	for client := range clients {

		err := client.WriteMessage(
			websocket.TextMessage,
			data,
		)

		if err != nil {
			log.Println("failed to broadcast:", err)

			client.Close()
			delete(clients, client)
		}
	}
}

func sanitizeReview(msg ReviewMessage,) ReviewMessage {
	msg.Title = html.EscapeString(msg.Title)
	msg.Text = html.EscapeString(msg.Text)
	return msg
}