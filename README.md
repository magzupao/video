¡Perfecto! Gracias por el resumen. Lo guardo como referencia definitiva:

# 📋 Proceso completo de despliegue JHipster (Frontend/Backend separados)

## 1. Crear el proyecto

```bash
jhipster jdl video.jdl
```

## 2. Configurar conexión a BBDD

```yaml
# src/main/resources/config/application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://postgresql:5432/video
    username: video
    password: gestionvideo.*2269
```

## 3. Configurar el path del API en el frontend

**src/main/webapp/app/core/config/application-config.service.ts:**

```typescript
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ApplicationConfigService {
  private endpointPrefix = '/'; // ← CAMBIO
  private microfrontend = false;

  setEndpointPrefix(endpointPrefix: string): void {
    this.endpointPrefix = endpointPrefix;
  }

  setMicrofrontend(microfrontend = true): void {
    this.microfrontend = microfrontend;
  }

  isMicrofrontend(): boolean {
    return this.microfrontend;
  }

  getEndpointFor(api: string, microservice?: string): string {
    if (microservice) {
      return `${this.endpointPrefix}services/${microservice}/${api}`;
    }
    // Asegurar que empiece con /
    const normalizedApi = api.startsWith('/') ? api : `/${api}`;
    return `${this.endpointPrefix}${normalizedApi}`.replace('//', '/');
  }
}
```

## 4. Compilar Angular

```bash
npm run webapp:build:prod -- --base-href=/video/ --deploy-url=/video/
```

**Copiar archivos generados:**

```bash
cp -r target/classes/static/* ~/server-ssl/html/video/
```

## 5. Compilar Java (backend)

```bash
./mvnw -Pprod,api-docs clean package -DskipTests -Dskip.npm -Dskip.webpack
```

## 6. Ejecutar backend en background

```bash
java -jar target/video-*.jar --spring.profiles.active=prod
nohup java -jar target/video-*.jar --spring.profiles.active=prod

# Ver logs
tail -f video.log
```

---

## 🎯 Resultado final:

- **Frontend**: `https://guiaturist.com/video/` → servido por Nginx desde `/html/video/`
- **Backend**: `https://guiaturist.com/api/` → proxy a `http://localhost:8080/api/`

## 📝 Nginx config necesaria:

```nginx
# Frontend
location /video/ {
    alias /var/www/html/video/;
    try_files $uri $uri/ /video/index.html;
}

# Backend API
location /api/ {
    proxy_pass http://videojhipster:8080/api/;
    # o si corre fuera de Docker:
    # proxy_pass http://localhost:8080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

---

**¡Excelente trabajo! 🎉 Ahora tienes una arquitectura limpia y profesional con frontend y backend completamente separados.**

docker-compose

```
services:
  videojhipster:
    container_name: videojhipster
    image: jhipster/jhipster
    command: tail -f /dev/null # Agrega este comando para mantener el contenedor en ejecución
    volumes:
      - ~/videojhipster/video:/home/jhipster/app
      - ~/videojhipster/.m2:/home/jhipster/.m2
      - /home/dev/shared-data:/app/shared-data
    networks:
      - external_network

  postgresql:
    container_name: postgresql
    image: postgres:18.1
    volumes:
      - ./volumes/webchatbotgestion/postgresql/:/var/lib/postgresql
    environment:
      - POSTGRES_USER=video
      - POSTGRES_PASSWORD=gestionvideo.*2269
      - POSTGRES_HOST_AUTH_METHOD=trust
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U $${POSTGRES_USER}']
      interval: 5s
      timeout: 5s
      retries: 10
    networks:
      - external_network

networks:
  external_network:
    external: true

```

conf nginx

```
    # ===========================================
    # FRONTEND ANGULAR - Archivos estáticos
    # ===========================================
    location /video/ {
        alias /var/www/html/video/;
        index index.html;
        try_files $uri $uri/ /video/index.html;

        # Cache para assets
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot|map)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }

    # ===========================================
    # BACKEND API
    # ===========================================
    location /api/ {
        client_max_body_size 50m;
        proxy_read_timeout 300;
        proxy_connect_timeout 60;
        proxy_send_timeout 300;
        proxy_request_buffering off;

        # Si usas Docker:
        proxy_pass http://videojhipster:8080/api/;

        # Si NO usas Docker (JAR directo o systemd):
        # proxy_pass http://localhost:8080/api/;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # CORS
        add_header Access-Control-Allow-Origin "https://guiaturist.com" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
        add_header Access-Control-Allow-Credentials "true" always;

        if ($request_method = 'OPTIONS') {
            return 204;
        }
    }

    location /management/ {
        # Mismo proxy_pass que /api/
        proxy_pass http://videojhipster:8080/management/;
        proxy_set_header Host $host;
    }

    location /v3/api-docs/ {
        proxy_pass http://videojhipster:8080/v3/api-docs/;
        proxy_set_header Host $host;
    }
```

esto en desarrollo
docker exec -it postgresql psql -U video

esto en produccion para reutilizar el postgresql, ejecutamos linea por linea:

```
 docker exec -it postgresql psql -U gestionguia -d gestionguia

-- Crear el usuario video
CREATE USER video WITH PASSWORD 'gestionvideo.*2269';

-- Crear la base de datos video
CREATE DATABASE video OWNER video;

-- Dar privilegios
GRANT ALL PRIVILEGES ON DATABASE video TO video;

-- Verificar que se crearon
\l

-- Ver los usuarios
\du

\q

 docker exec -it postgresql psql -U video -d video
```

Configuración de tu aplicación (application-prod.yml):

```
spring:
  datasource:
    url: jdbc:postgresql://postgresql:5432/videodb
    username: video
    password: gestionvideo.*2269
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update  # o validate en prod
```
