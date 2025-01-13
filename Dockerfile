# Usa una imagen base con Maven y Java
FROM maven:3.9-openjdk-21 AS builder
# Configura el directorio de trabajo
WORKDIR /app

# Copia todo el proyecto al contenedor
COPY . .

# Construye el archivo JAR
RUN mvn clean package -DskipTests

# Usa una imagen más ligera para la ejecución
FROM openjdk:21-jdk-slim

# Configura el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR generado desde el builder
COPY --from=builder /app/target/server-0.0.1-SNAPSHOT.jar /app/server.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "server.jar"]
