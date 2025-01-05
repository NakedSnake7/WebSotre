# Usa una imagen oficial de Java con Maven preinstalado
FROM maven:3.8.5-openjdk-17 AS build

# Establece el directorio de trabajo
WORKDIR /app

# Copia el contenido de tu repositorio al contenedor
COPY . .

# Compila el proyecto y construye el JAR
RUN mvn clean package -DskipTests

# Usa una imagen ligera de Java para producción
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
