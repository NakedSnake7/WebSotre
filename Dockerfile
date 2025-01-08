# Usa una imagen base con Java (por ejemplo, OpenJDK 21)
FROM openjdk:21-jdk-slim

# Configura JAVA_HOME
ENV JAVA_HOME=/usr/local/openjdk-21
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Directorio de trabajo
WORKDIR /app

# Copia el archivo JAR de tu aplicación
COPY target/server-0.0.1-SNAPSHOT.jar /app/server.jar

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "server.jar"]
