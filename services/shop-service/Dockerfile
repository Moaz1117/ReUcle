FROM node:20-alpine AS build
WORKDIR /app
COPY package.json server.js ./
RUN npm install --omit=dev

FROM node:20-alpine
WORKDIR /app
COPY --from=build /app ./
EXPOSE 3009
CMD ["node","server.js"]
