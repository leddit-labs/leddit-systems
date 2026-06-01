package main

import (
	"context"
	"database/sql"
	"io"
	"log"
	"net"

	_ "github.com/go-sql-driver/mysql"
	"google.golang.org/grpc"

	pb "grpc-app/generated"
)

type server struct {
	pb.UnimplementedGameServiceServer //if client tries an rpc not implemented. throw UNIMPLEMENTED to client
	db *sql.DB
}

func (s *server) GetGame(
	ctx context.Context,
	req *pb.GetGameRequest,
) (*pb.GetGameResponse, error) {

	var game pb.GetGameResponse

	err := s.db.QueryRow(`
		SELECT id, name, description
		FROM game
		WHERE id = ?
	`, req.Id).Scan(
		&game.Id,
		&game.Name,
		&game.Description,
	)

	if err != nil {
		return nil, err
	}

	return &game, nil
}

func (s *server) ReviewStream(stream pb.GameService_ReviewStreamServer) error {

	for {
		//messages from client
		msg, err := stream.Recv()
		
		//client closed stream - EOF meaning end of file
		if err == io.EOF {
			return nil
		}

		//check for errors
		if err != nil {
			return err
		}

		//save to db
		_, err = s.db.Exec(`
			INSERT INTO review (title, text, star_amount, user_id, game_id)
			VALUES (?, ?, ?, ?, ?)
		`,
			msg.Title,
			msg.Text,
			msg.StarAmount,
			msg.UserId,
			msg.GameId,
		)

		//database errors
		if err != nil {
			return err
		}

		//send response back to client
		_ = stream.Send(&pb.ReviewMessage{
			GameId:  msg.GameId,
			Title: msg.Text,
			UserId:  msg.UserId,
		})
	}
}

func main() {
	connectionString := "user:PASSWORD@tcp(mysql:3306)/si_db"

	db, err := sql.Open("mysql", connectionString)
	if err != nil {
		log.Fatal(err)
	}

	lis, err := net.Listen("tcp", ":50051")
	if err != nil {
		log.Fatal(err)
	}

	grpcServer := grpc.NewServer()

	pb.RegisterGameServiceServer(grpcServer, &server{db: db})

	log.Println("gRPC running on :50051")

	if err := grpcServer.Serve(lis); err != nil {
		log.Fatal(err)
	}
}