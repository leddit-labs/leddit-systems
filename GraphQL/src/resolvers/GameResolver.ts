import { Resolver, Query, Mutation, Arg, Int } from "type-graphql";
import { PrismaClient } from "@prisma/client";
import { Game } from "../types/Game";

const prisma = new PrismaClient();

@Resolver(Game)
export class GameResolver {

  // Query 1: Get all games with optional search filter
  @Query(() => [Game])
  async getAllGames(@Arg("search", { nullable: true }) search?: string) {
    return prisma.game.findMany({
      where: search ? { name: { contains: search } } : undefined,
    });
  }

  // Query 2: Get a single game by ID
  @Query(() => Game, { nullable: true })
  async getGameById(@Arg("id", () => Int) id: number) {
    return prisma.game.findUnique({ where: { id } });
  }

  // Mutation 1: Create a new game
  @Mutation(() => Game)
  async createGame(@Arg("name") name: string) {
    return prisma.game.create({ data: { name } });
  }

  // Mutation 2: Delete a game by ID
  @Mutation(() => Game)
  async deleteGame(@Arg("id", () => Int) id: number) {
    return prisma.game.delete({ where: { id } });
  }
}