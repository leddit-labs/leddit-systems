package main

import (
	"encoding/json"
	"html"
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

func broadcastMessage(msg Message) {
	safeMsg := sanitizeMessage(msg)

	data, _ := json.Marshal(safeMsg)

	mutex.Lock()
	defer mutex.Unlock()

	for client := range clients {
		client.WriteMessage(websocket.TextMessage, data)
	}
}
func sanitizeMessage(msg Message) Message {
	msg.Title = html.EscapeString(msg.Title)
	msg.Text = html.EscapeString(msg.Text)
	return msg
}