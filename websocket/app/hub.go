package main

import (
	"encoding/json"
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

func broadcastMessage(msg any) {
	data, _ := json.Marshal(msg)

	mutex.Lock()
	defer mutex.Unlock()

	for client := range clients {
		client.WriteMessage(websocket.TextMessage, data)
	}
}