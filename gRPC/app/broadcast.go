package main

import (
	"log"
	"sync"

	pb "grpc-app/generated"
)

type Broadcaster struct {

	// connected clients
	Clients map[pb.GameService_ReviewStreamServer]bool

	Mutex sync.Mutex
}

func NewBroadcaster() *Broadcaster {

	return &Broadcaster{
		Clients: make(
			map[pb.GameService_ReviewStreamServer]bool,
		),
	}
}


// Register new client
func (b *Broadcaster) AddClient(
	stream pb.GameService_ReviewStreamServer,
) {

	b.Mutex.Lock()
	defer b.Mutex.Unlock()

	b.Clients[stream] = true
}


//remove disconnected client
func (b *Broadcaster) RemoveClient(
	stream pb.GameService_ReviewStreamServer,
) {

	b.Mutex.Lock()
	defer b.Mutex.Unlock()

	delete(b.Clients, stream)
}

func (b *Broadcaster) Broadcast(
	msg *pb.ReviewMessage,
) {

	b.Mutex.Lock()
	defer b.Mutex.Unlock()

	for client := range b.Clients {

		err := client.Send(&pb.ReviewMessage{
			GameId:     msg.GameId,
			Title:      msg.Title,
			Text:       msg.Text,
			StarAmount: msg.StarAmount,
			UserId:     msg.UserId,
		})

		if err != nil {

			log.Println(
				"Failed sending to client:",
				err,
			)

			delete(b.Clients, client)
		}
	}
}