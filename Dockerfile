# --- Etapa 1: Builder con Maven y Java ---
FROM maven:3.9.5-eclipse-temurin-21 AS builder
# Usa Maven + JDK 21 (imagen oficial que sí existe)

WORKDIR /app

# Copia los archivos de proyecto
COPY pom.xml .
COPY src ./src

# Construye el JAR sin tests
RUN mvn clean package -DskipTests

# --- Etapa 2: Imagen ligera para ejecución ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copia el JAR generado
COPY --from=builder /app/target/server-0.0.1-SNAPSHOT.jar /app/server.jar

# Expone el puerto
EXPOSE 8080

# Ejecuta la app
CMD ["java", "-jar", "server.jar"]
