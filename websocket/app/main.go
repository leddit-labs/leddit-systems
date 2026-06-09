package main

import (
	"database/sql"
	"log"
	"net/http"
)

func wsHandler(db *sql.DB, w http.ResponseWriter, r *http.Request) {
	allowedOrigins := map[string]bool{
		"http://localhost:3000": true, 			//3000 is just to emulate some frontend
		//"https://frontend.com": true,			//here a future frontend url could be
	}

	origin := r.Header.Get("Origin")

	if !allowedOrigins[origin] {
		log.Println("blocked websocket connection from origin:", origin)
		http.Error(w, "forbidden", http.StatusForbidden)
		return
	}
	

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

	log.Println("WebSocket server running on :8082")

	err := http.ListenAndServe(":8082", nil)
	if err != nil {
		log.Fatal(err)
	}
}