# ─────────────────────────────────────────────
# Etapa 1: build do Angular
# ─────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Instala dependências primeiro (aproveita cache do Docker)
# Se só o código mudar, esta camada não é reconstruída
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline --no-audit

# Copia o código-fonte e compila para produção
COPY . .
RUN npm run build -- --configuration=production

# ─────────────────────────────────────────────
# Etapa 2: Nginx servindo os assets estáticos
# ─────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

# Remove a config padrão do Nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copia nossa config customizada
COPY nginx.conf /etc/nginx/conf.d/app.conf

# Copia os arquivos compilados do Angular
# O Angular 17 gera em dist/<nome-projeto>/browser/
COPY --from=build /app/dist/ocorrencias-frontend/browser /usr/share/nginx/html

# Usuário não-root por segurança
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown nginx:nginx /var/run/nginx.pid

USER nginx

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost/index.html || exit 1

CMD ["nginx", "-g", "daemon off;"]
