FROM reg.frontnode.net/java:8u45
VOLUME /tmp
ADD ${project.build.finalName}.jar app.jar
RUN bash -c 'touch /app.jar'
EXPOSE ${server.port}
ENTRYPOINT ["java", "-jar", "/app.jar"]