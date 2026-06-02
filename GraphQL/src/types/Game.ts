import { ObjectType, Field, Int, Float } from "type-graphql";

@ObjectType()
export class Game {
  @Field(() => Int)
  id: number;

  @Field()
  name: string;

  @Field({ nullable: true })
  slug?: string;

  @Field(() => Int, { nullable: true })
  yearPublished?: number;

  @Field(() => Float, { nullable: true })
  bggRating?: number;

  @Field(() => Float, { nullable: true })
  difficultyRating?: number;

  @Field({ nullable: true })
  description?: string;

  @Field(() => Int, { nullable: true })
  playingTime?: number;

  @Field({ nullable: true })
  available?: boolean;

  @Field(() => Int, { nullable: true })
  minPlayers?: number;

  @Field(() => Int, { nullable: true })
  maxPlayers?: number;

  @Field(() => Int, { nullable: true })
  minimumAge?: number;
}