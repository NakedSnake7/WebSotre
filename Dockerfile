# Imagen base con OpenJDK 21
FROM eclipse-temurin:21-jdk AS build

# Define el directorio de trabajo
WORKDIR /app

# Copia todo el proyecto
COPY . .

# Construye el proyecto
RUN ./mvnw clean package -DskipTests

# Imagen ligera para ejecutar el JAR
FROM eclipse-temurin:21-jdk-jammy

# Define el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR generado
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto (Render asignará el correcto dinámicamente)
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]

