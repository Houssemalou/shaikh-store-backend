# Étape 1 : Build avec Maven
FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app

# Pré-charger les dépendances pour améliorer le cache
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Copier le code source
COPY src ./src

# Compiler et packager sans les tests
RUN mvn -B clean package -DskipTests

# Étape 2 : Image d’exécution
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copier le jar depuis l’étape de build (nom d'étape = build)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
