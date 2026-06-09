import { Resolver, Query, Mutation, Arg, Int } from "type-graphql";
import { PrismaClient } from "@prisma/client";
import { Game } from "../types/Game";

const prisma = new PrismaClient();

@Resolver(Game)
export class GameResolver {

  // Query 1
  @Query(() => [Game])
  async getAllGames(@Arg("search", { nullable: true }) search?: string) {
    return prisma.game.findMany({
      where: search ? { name: { contains: search } } : undefined,
    });
  }

  // Query 2
  @Query(() => Game, { nullable: true })
  async getGameById(@Arg("id", () => Int) id: number) {
    return prisma.game.findUnique({ where: { id } });
  }

  // Mutation 1
  @Mutation(() => Game)
  async createGame(@Arg("name") name: string) {
    return prisma.game.create({ data: { name } });
  }

  // Mutation 2
  @Mutation(() => Game)
  async deleteGame(@Arg("id", () => Int) id: number) {
    return prisma.game.delete({ where: { id } });
  }
}