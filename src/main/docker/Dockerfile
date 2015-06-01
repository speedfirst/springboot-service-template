FROM reg.frontnode.net/ubuntu-jdk8
WORKDIR /app

VOLUME /app/log
VOLUME /app/conf

ADD ${project.build.finalName}.jar /app/
ADD start-springboot-app.sh /app/

# 5005 is assumed to be the java remote debug port if enabled
# 5055 is assumed to be the JMX port if enabled
EXPOSE ${server.port} 5005 5055

CMD ["/bin/sh", "start-springboot-app.sh"]
