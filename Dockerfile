FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN javac Server.java
EXPOSE 8080
CMD ["java", "Server"]
