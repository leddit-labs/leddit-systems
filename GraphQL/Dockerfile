FROM node:18

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY tsconfig.json ./
COPY prisma ./prisma

RUN npx prisma@6.15.0 generate

EXPOSE 4000

CMD ["npm", "run", "dev"]
