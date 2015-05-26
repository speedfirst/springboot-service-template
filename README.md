<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents** 

- [RPC Service Template](#rpc-service-template)
- [How to Run](#how-to-run)
- [Jetty](#jetty)
- [DB Connection](#db-connection)
- [MyBatis Integration](#mybatis-integration)
- [MongoDB Integration](#mongodb-integration)
- [Logging](#logging)
   - [DBAppender](#DBAppender)
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

# MongoDB Integration
To enable mongo support, add the following dependency to the project.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```
Create a model class, whose name should be same (except the initial capital letter) to the collection name in mongo. For example, there is a collection name "book", then create a class "Book". Fill all the necessary fields, and mark `@org.springframework.data.annotation.Id` to the filed which will be mapped to "_id" field in mongo document (or called "row" in mongo collection).

```java
public class Book {
    @Id
    public String _id;

    public int id;

    public String name;

    public double price;
}
```

After that, create a Repository interface which extends MongoRepository.

```java
public interface BookRepository extends MongoRepository<Book, Integer> {

   Book findBookByName(String name);
}
```

The base MongoRepository has defined a batch of CRUD methods that could be used. You can
add more by some rules whchi can be identified by the converter, like "findBookByName".
It will be translated to command `db.book.find({"name": "xxx"})` in mongo shell.

Then mark `@EnableMongoRepository` in the main config class (or specify mongo repository
in application context xml file). This will enable springboot to auto detect all "mongo 
repository interfaces" and dynamic construct proxy classes.

Finally, specify the host name, port and database name in `pplication.properties` file.

```
# mongo
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=local
```

Start the project and have a try.

```
# get a book by name
curl localhost:8080/book/mydoc

# insert a new book
curl -X POST -H 'Content-Type:application/json' -d '{"id": 10, "name": "zero to one", "price" : 31.25}' localhost:8080/book/new

```

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

## DBAppender

Sometimes you want to append business logs to database for backtrace. You can easily configure
this in logback.xml.

First add DBAppender and business logger in `logback.xml` file.

```xml
<appender name="DB" class="ch.qos.logback.classic.db.DBAppender">
    <connectionSource class="ch.qos.logback.core.db.DataSourceConnectionSource">
        <dataSource class="com.mchange.v2.c3p0.ComboPooledDataSource">
            <driverClass>com.mysql.jdbc.Driver</driverClass>
            <jdbcUrl>jdbc:mysql://localhost:3306/biz_log</jdbcUrl>
            <user>root</user>
            <password></password>
        </dataSource>
    </connectionSource>
</appender>


<logger name="biz" level="INFO" additivity="false">
    <appender-ref ref="DB"/>
</logger>
```

Note here we're using c3p0 pooled datasource to improve performance. Otherwise, the average
delay of log writing may be 10+ ms so thus the TPS will only be ~100. To make it work, remember to add c3p0 in the dependencies of `pom.xml`.

```xml
<dependency>
    <groupId>com.mchange</groupId>
    <artifactId>c3p0</artifactId>
    <version>0.9.5</version>
</dependency>
```
> Tomcat conneciton pool should work too here. But I can't find a configuring sample for it.

Before using the DBAppender, do create the database and tables. logback won't create it for you. [src/main/resources/init-logback-mysql.sql](src/main/resources/init-logback-mysql.sql) is the script to create tables. For scripts of creating tables for other databases, you can find
them [here](https://github.com/qos-ch/logback/tree/master/logback-classic/src/main/resources/ch/qos/logback/classic/db/script).

Then logging the content as usual by the logger name starting with "biz".

```java
private Logger bizLogger = LoggerFactory.getLogger("biz.hello");
//...
bizLogger.info("hello is invoked from {}", req.getRemoteAddr());
```

Finally you will see the logs in `logging_event` table.

```
MySQL [biz_log]> select timestmp, formatted_message, logger_name, level_string, caller_class from logging_event;
+---------------+---------------------------------+-------------+--------------+--------------------------------+
| timestmp      | formatted_message               | logger_name | level_string | caller_class                   |
+---------------+---------------------------------+-------------+--------------+--------------------------------+
| 1432630075323 | hello is invoked from 127.0.0.1 | biz.hello   | INFO         | app.controller.HelloController |
+---------------+---------------------------------+-------------+--------------+--------------------------------+
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

After that, push the result image to registry.

```
docker push reg.frontnode.net/service-template:latest
```
Finally, run the service within docker. You may encounter errors if you are
still trying to connect database in local host, but the basic hello controller
should still work.

```
docker run -d -p 8080:8080 reg.frontnode.net/service-template:latest
curl localhost:8080
```