FROM openjdk:17-slim
WORKDIR /app
COPY target/it-asset-system-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8080", "--spring.datasource.url=jdbc:mysql://mysql:3306/it_asset_manage?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai"]
