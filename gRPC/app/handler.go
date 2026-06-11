package main

import (
	"context"
	"database/sql"
	"io"
	"log"

	pb "grpc-app/generated"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type GameHandler struct {
	pb.UnimplementedGameServiceServer
	db          *sql.DB
	broadcaster *Broadcaster
}

func NewGameHandler(
	db *sql.DB,
) *GameHandler {

	return &GameHandler{
		db: db,

		broadcaster: NewBroadcaster(),
	}
}

// unary RPC
func (s *GameHandler) GetGame(
	ctx context.Context,
	req *pb.GetGameRequest,
) (*pb.GetGameResponse, error) {

	var game pb.GetGameResponse

	err := s.db.QueryRow(`
		SELECT id, name
		FROM game
		WHERE id = ?
	`, req.Id).Scan(
		&game.Id,
		&game.Name,
	)

	//no game found
	if err == sql.ErrNoRows {

		return nil, status.Error(
			codes.NotFound,
			"game not found",
		)
	}

	//db/internal error
	if err != nil {

		return nil, status.Error(
			codes.Internal,
			"database error",
		)
	}

	return &game, nil
}

// Bidirectional stream
func (gamehandler *GameHandler) ReviewStream(
	stream pb.GameService_ReviewStreamServer,
) error {

	gamehandler.broadcaster.AddClient(stream)

	log.Println("Client connected")

	//cleanup on disconnect
	defer func() {

		gamehandler.broadcaster.RemoveClient(stream)

		log.Println("Client disconnected")
	}()

	for {

		//receive review
		msg, err := stream.Recv()

		//client disconnected
		if err == io.EOF {
			return nil
		}

		if err != nil {

			return status.Error(
				codes.Internal,
				err.Error(),
			)
		}

		//validate review
		err = ValidateReview(msg)

		if err != nil {
			return err
		}

		//save review
		_, err = gamehandler.db.Exec(`
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

			return status.Error(
				codes.Internal,
				"failed to save review",
			)
		}

		//broadcast to all clients
		gamehandler.broadcaster.Broadcast(msg)
	}
}

