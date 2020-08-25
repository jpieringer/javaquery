FROM adoptopenjdk/openjdk14:x86_64-alpine-jdk-14.0.2_12-slim
RUN apk add --update --no-cache graphviz ttf-freefont

COPY build/libs/javaquery-full.jar /usr/local/
WORKDIR /usr/local

ENTRYPOINT ["java", "-jar", "javaquery-full.jar"]
