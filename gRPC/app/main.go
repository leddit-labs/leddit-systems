package main

import (
	"log"
	"net"

	"google.golang.org/grpc"

	pb "grpc-app/generated"
)

func main() {

	db, err := Connect()

	if err != nil {
		log.Fatal(err)
	}

	lis, err := net.Listen("tcp", ":50051")
	if err != nil {
		log.Fatal(err)
	}

	grpcServer := grpc.NewServer()

	gameHandler := NewGameHandler(db)

	pb.RegisterGameServiceServer(
		grpcServer,
		gameHandler,
	)

	log.Println("gRPC running on :50051")

	if err := grpcServer.Serve(lis); err != nil {
		log.Fatal(err)
	}
}