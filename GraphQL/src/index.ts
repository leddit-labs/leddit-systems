import "reflect-metadata";
import { ApolloServer } from "apollo-server";
import { buildSchema } from "type-graphql";
import { PrismaClient } from "@prisma/client";
import { GameResolver } from "./resolvers/GameResolver";
import { ApolloServerPluginLandingPageLocalDefault } from "apollo-server-core";

const prisma = new PrismaClient();

async function main() {
// Builds the GraphQL schema from TypeGraphQL resolvers and types.
// Generates the SDL schema automatically — no manual SDL file needed.
  const schema = await buildSchema({
    resolvers: [GameResolver],
    validate: false,
  });

// By creating an ApolloServer instance and using .listen(), all GraphQL requests go through it, and those protections are active automatically — no extra config needed.
const server = new ApolloServer({ 
  schema,
  introspection: true,
  plugins: [
    ApolloServerPluginLandingPageLocalDefault({ embed: true }),
  ],
});

  const { url } = await server.listen(4000);
  console.log(`GraphQL API ready at ${url}`);
}

main().catch(console.error);