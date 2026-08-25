FROM eclipse-temurin:17-jre
WORKDIR app
COPY /target/library-app.jar app.jar
RUN apt update
ENV APP_NAME=sagars_library
RUN mkdir sagartesting
RUN echo "Hello from Docker build" > message.txt
ENTRYPOINT ["java","-jar","app.jar"]

