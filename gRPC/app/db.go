package main

import (
	"database/sql"

	_ "github.com/go-sql-driver/mysql"
)

func Connect() (*sql.DB, error) {

	connectionString :=
		"user:PASSWORD@tcp(mysql:3306)/si_db"

	return sql.Open("mysql", connectionString)
}