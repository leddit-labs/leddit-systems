package main

type Message struct {
	Type string `json:"type"`

	Id int `json:"id"`

	Title      string `json:"title"`
	Text       string `json:"text"`
	StarAmount int    `json:"star_amount"`
	UserId     int    `json:"user_id"`
	GameId     int    `json:"game_id"`
}

type Game struct {
	Type        string `json:"type"`
	Id          int    `json:"id"`
	Name        string `json:"name"`
	Description string `json:"description"`
}