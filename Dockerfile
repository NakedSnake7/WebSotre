# Usa una imagen base de Java
FROM eclipse-temurin:17-jdk-alpine

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR generado
COPY target/server-0.0.1-SNAPSHOT.jar

# Exponer el puerto usado por la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
