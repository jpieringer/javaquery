FROM adoptopenjdk/openjdk16:x86_64-alpine-jdk-16.0.1_9-slim
RUN apk add --update --no-cache graphviz ttf-freefont

COPY build/libs/javaquery-full.jar /usr/local/
WORKDIR /usr/local

ENTRYPOINT ["java", "-jar", "javaquery-full.jar"]
