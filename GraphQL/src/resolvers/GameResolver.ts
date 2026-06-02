import { Resolver, Query, Mutation, Arg, Int } from "type-graphql";
import { PrismaClient } from "@prisma/client";
import { Game } from "../types/Game";

const prisma = new PrismaClient();

@Resolver(Game)
export class GameResolver {
  // Query 1: Get all games with search
  @Query(() => [Game])
  async games(
    @Arg("search", { nullable: true }) search?: string,
  ) {
    return prisma.game.findMany({
      where: search
        ? { name: { contains: search } }
        : undefined,
    });
}

  // Query 2: Get one game by ID
  @Query(() => Game, { nullable: true })
  async game(@Arg("id", () => Int) id: number) {
    return prisma.game.findUnique({ where: { id } });
  }

  // Mutation 1: Create a game
  @Mutation(() => Game)
async createGame(
  @Arg("name") name: string,
  @Arg("yearPublished", () => Int, { nullable: true }) yearPublished?: number,
  @Arg("minPlayers", () => Int, { nullable: true }) minPlayers?: number,
  @Arg("maxPlayers", () => Int, { nullable: true }) maxPlayers?: number
) {
  return prisma.game.create({
    data: { 
      name, 
      year_published: yearPublished, 
      min_players: minPlayers, 
      max_players: maxPlayers 
    },
  });
}

  // Mutation 2: Update game availability
  @Mutation(() => Game)
  async updateAvailability(
    @Arg("id", () => Int) id: number,
    @Arg("available") available: boolean
  ) {
    return prisma.game.update({
      where: { id },
      data: { available },
    });
  }
}