# ==========================================
# RailFlow Frontend - Multi-stage Production Dockerfile
# Stage 1: Build React/Vite assets
# ==========================================
FROM node:20-alpine AS builder

WORKDIR /app

# Cache dependencies
COPY package*.json ./
RUN npm ci

# Build production bundle
COPY . .
RUN npm run build

# ==========================================
# Stage 2: Serve with lightweight Nginx Alpine
# ==========================================
FROM nginx:1.25-alpine

# Copy custom Nginx reverse proxy configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled static assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
