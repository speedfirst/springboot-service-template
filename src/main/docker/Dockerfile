FROM 	reg.frontnode.net/ubuntu-jdk8

LABEL	APP_NAME=${project.artifactId}
LABEL	APP_VERSION=${project.version}

RUN \
        groupadd --gid 2000 app && \
        useradd -m -d /app --uid 2000 -g app app && \
        mkdir -p /app/conf && \
        mkdir -p /app/log
	

#ADD 	${project.build.finalName}.jar 	/app/app.jar
ADD 	${project.build.finalName}.jar 	/app/
ADD 	start-springboot-app.sh 	/app/

RUN \
        chown -R app:app /app

# 5005 is assumed to be the java remote debug port if enabled
# 5055 is assumed to be the JMX port if enabled
EXPOSE ${server.port} 5005 5055


VOLUME 	/app/log
VOLUME 	/app/conf

WORKDIR /app
USER	app

CMD 	/bin/sh ./start-springboot-app.sh
