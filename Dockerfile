FROM openjdk:8
VOLUME /tmp
EXPOSE 9101
ADD ./target/servicio-oauth.jar servicio-oauth-image.jar
ENTRYPOINT ["java","-jar","-Dspring.cloud.config.uri=http://config-server:8888","/servicio-oauth-image.jar"]