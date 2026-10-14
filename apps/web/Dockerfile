FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# Caddy serve os arquivos estáticos e faz o proxy de /api; o Caddyfile vem do compose
FROM caddy:2-alpine
COPY --from=build /app/dist /srv
