package main

import (
	"database/sql"
	"log"

	_ "github.com/go-sql-driver/mysql"
)

func initDB() *sql.DB {

	dsn := "user:PASSWORD@tcp(mysql:3306)/si_db"

	db, err := sql.Open("mysql", dsn)
	if err != nil {
		log.Fatal(err)
	}

	return db
}