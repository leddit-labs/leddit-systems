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
  year_published?: number;

  @Field(() => Float, { nullable: true })
  bgg_rating?: number;

  @Field(() => Float, { nullable: true })
  difficulty_rating?: number;

  @Field({ nullable: true })
  description?: string;

  @Field(() => Int, { nullable: true })
  playing_time?: number;

  @Field({ nullable: true })
  available?: boolean;

  @Field(() => Int, { nullable: true })
  min_players?: number;

  @Field(() => Int, { nullable: true })
  max_players?: number;

  @Field(() => Int, { nullable: true })
  minimum_age?: number;
}