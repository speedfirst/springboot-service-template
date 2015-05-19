<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents** 

- [RPC Service Template](#rpc-service-template)
- [How to Run](#how-to-run)
- [Jetty](#jetty)
- [DB Connection](#db-connection)
- [MyBatis Integration](#mybatis-integration)
- [Logging](#logging)
- [Docker Image Build](#docker-image-build)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

RPC Service Template
======================================

A service code template based on springboot, jetty and mybatis etc.

# How to Run
Ensure you have alread installed JDK (>= JDK 7), then

```
mvn package; java -jar service-template-0.0.1-SNAPSHOT.jar
```
The service should start to listen to 8080 port. Try the hello service by typing.

```
curl -i localhost:8080
```

# Jetty
Springboot default uses Tomcat as the embeded container. This project use such configuration to disable Tomcat and enable jetty 9.

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
	<exclusions>
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```
# DB Connection

As default, springboot uses [tomcat db connection pool](http://people.apache.org/~fhanik/tomcat/jdbc-pool.html) to create DataSource
object, which you can autowired in your. Tomcat's db connection pool is a great
replacement of commons-dbcp. 

You can configure datasource/connection pool by changing values in the
"src/main/resources/application.properties". Besides here are a [manifest](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
of properties you can configure to spring boot.

# MyBatis Integration

To enable MyBatis, the project adds dependencies:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
	<groupId>org.mybatis</groupId>
	<artifactId>mybatis-spring</artifactId>
	<version>1.2.2</version>
</dependency>
<dependency>
	<groupId>org.mybatis</groupId>
	<artifactId>mybatis</artifactId>
	<version>3.2.8</version>
</dependency>
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>5.1.34</version>
</dependency>
```

In "src/main/resources/mapper", you can add mapper xml as you expected.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="mapper.VillageMapper">
    <resultMap id="Blog_result" type="model.Village" >
        <id column="vid" property="vid" />
        <result column="name" property="name"/>
        <result column="district" property="district"/>
    </resultMap>

    <!-- resultType与resultMap不能同时使用 -->
    <select id="getVillage" parameterType="int" resultMap="Blog_result">
        SELECT vid, name, district FROM village WHERE vid = #{vid}
    </select>

    <insert id="insertVillage" parameterType="model.Village" flushCache="true" statementType="PREPARED"
            useGeneratedKeys="true" keyProperty="vid" timeout="20">
        INSERT INTO village(name, district) VALUES(#{name}, #{district})
    </insert>
</mapper>
```

To test MyBatis, first install mysql in local host and use src/main/resources/init-db.sql to create _village_ table. Then start the service.

To create a new village, type commands as below.

```
curl -i -X POST -H "Content-Type:application/json" -d '{"name":"panyu", "district":"guangzhou"}' localhost:8080/village/new
```
If everything is OK, you'd see 201 response code and the "village" object you created.

Then you can get it by village id:

```
curl localhost:8080/village/1
```

Note the 404 error if you provided some not existed vid.

# Logging
We're using slf4j as the logging interface while logback as the logging backend.

When logging is not configured, the log is outputed to STDOUT.

For simple logging, specify "logging.file" or "logging.path" properties in `application.properties` file to specify where to output log, see [here](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-logging.html#boot-features-custom-log-configuration).

For customized logging configuration, create `logback.xml` somewhere and set its path to "logging.config" properties.

```
logging.config=classpath:config/logback.xml
```

Go [here](http://logback.qos.ch/manual/configuration.html) for logback configuration syntax explanations.

> Please ensure the log file is written to disk if you configure a file log appender. If not, it may be due to the file permission problem.

A typical logging java code looks like:


```java
private Logger logger = LoggerFactory.getLogger(this.getClass());

@RequestMapping("/")
public String index(HttpServletRequest req) {
    logger.info("Request to / coming from {}", req.getRemoteAddr());
    return "Hello, this is a web app based on springboot\n";
}
```
# Docker Image Build

In pom.xml, maven-docker-plugin is used to generate docker image.

```xml
<properties>
	<docker.image.prefix>reg.frontnode.net</docker.image.prefix>
	<docker.image.tag>latest</docker.image.tag>
</properties>
<build>
        <plugins>
      		<plugin>
            	<groupId>com.spotify</groupId>
            	<artifactId>docker-maven-plugin</artifactId>
            	<version>0.2.3</version>
            	<configuration>
                	<imageName>${docker.image.prefix}/${project.artifactId}:${docker.image.tag}</imageName>
                	<dockerDirectory>src/main/docker</dockerDirectory>
                	<resources>
                    	<resource>
                        	<targetPath>/</targetPath>
                        	<directory>${project.build.directory}</directory>
                        	<include>${project.build.finalName}.jar</include>
                    	</resource>
                	</resources>
            	</configuration>
        	</plugin>
        </plugins>
    </build>
```

To build a docker image, first create the `Dockerfile` under `src/main/docker`. Normally it should copy the generated jar and run `java -jar app.jar`. Then run 

```
mvn docker:build
```

It should take a while to pull the necessary layers. After it succeeds, you should see it in the result of `docker images`.

> Note, before building docker image, you must ensure the artifact generated by `mvn package` exists. Or you can bind the plugin to the maven package phase.

Finally, push the result image to registry.

```
docker push reg.frontnode.net/service-template:latest
```