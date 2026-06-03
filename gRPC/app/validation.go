package main

import (
	pb "grpc-app/generated"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

func ValidateReview(
	msg *pb.ReviewMessage,
) error {

	if msg.Title == "" {
		return status.Error(
			codes.InvalidArgument,
			"title is required",
		)
	}

	if msg.Text == "" {
		return status.Error(
			codes.InvalidArgument,
			"text is required",
		)
	}

	if msg.GameId <= 0 {
		return status.Error(
			codes.InvalidArgument,
			"invalid game_id",
		)
	}

	if msg.UserId <= 0 {
		return status.Error(
			codes.InvalidArgument,
			"invalid user_id",
		)
	}

	if msg.StarAmount < 1 || msg.StarAmount > 5 {
		return status.Error(
			codes.InvalidArgument,
			"star_amount must be between 1 and 5",
		)
	}

	return nil
}