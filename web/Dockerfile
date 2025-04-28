# 1) Builder: Nuxt 앱을 빌드
FROM node:18-alpine AS builder
WORKDIR /app
COPY web/package.json web/package-lock.json* ./
RUN npm install
COPY web ./
RUN npm run build

# 2) Runtime: SSR 서버로 기동
FROM node:18-alpine
WORKDIR /app
COPY --from=builder /app ./
EXPOSE 3000
CMD ["npm","run","start"]