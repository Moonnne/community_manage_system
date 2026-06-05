FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS=""
ENV SPRING_CONFIG_ADDITIONAL_LOCATION=file:/app/config/env.yaml

COPY target/CommunityManageSystem-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
