# Etapa 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# IMPORTANTE: Copiamos la carpeta uploads al contexto de build si es necesario,
# pero lo crítico es copiarla en la etapa final.
COPY uploads ./uploads

# Empaquetar el jar saltando los tests
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/target/*.jar app.jar

# Copiar la carpeta de imágenes dentro del contenedor final
# Esto asegura que las imágenes base (/uploads/products/...) estén disponibles
COPY --from=build /app/uploads ./uploads

# Exponer el puerto 8080
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]