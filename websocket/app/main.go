package main

import (
	"database/sql"
	"log"
	"net/http"
)

func wsHandler(db *sql.DB, w http.ResponseWriter, r *http.Request) {

	ws, err := upgradeConn(w, r)
	if err != nil {
		log.Println(err)
		return
	}

	defer ws.Close()

	addClient(ws)
	log.Println("client connected")

	for {
		var msg Message

		err := ws.ReadJSON(&msg)
		if err != nil {
			log.Println("client disconnected:", err)
			removeClient(ws)
			break
		}

		handleMessage(db, ws, msg)
	}
}

func main() {

	db := initDB()

	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		wsHandler(db, w, r)
	})

	log.Println("WebSocket server running on :8081")

	err := http.ListenAndServe(":8081", nil)
	if err != nil {
		log.Fatal(err)
	}
}