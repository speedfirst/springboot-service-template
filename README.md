<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents** 

- [RPC Service Template](#rpc-service-template)
- [How to Run](#how-to-run)
- [Jetty](#jetty)
- [DB Connection](#db-connection)
- [MyBatis Integration](#mybatis-integration)
   - [Multiple Datasources and Mappers Configuration](#multiple-datasources-and-mappers-configuration)
- [Redis Integration](#redis-integration)
- [MongoDB Integration](#mongodb-integration)
- [Logging](#logging)
   - [DBAppender](#DBAppender)
- [Docker Image Build](#docker-image-build)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

Springboot RPC Service Template
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

## Multiple Datasources and Mappers Configuration
> To see the sample code, checkout to git branch "multi-datasources".

You may use more than one data source (as well as SessionFactory instances). To do this
you have to disable Springboot's auto data source configuration by excluding them from
`@EnableAutoConfiguration`.

```java
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class AppConfig {
    // ...
}
```

Suppose you have two sets of data source properties in `application.properties`, one has prefix "app.village.ds", while another has "app.city.ds".

```
# data source configuration 1
app.village.ds.url=jdbc:mysql://localhost:3306/village
app.village.ds.username=root
app.village.ds.password=
app.village.ds.max-active=200
app.village.ds.max-idle=10
app.village.ds.min-idle=10
app.village.ds.initial-size=15
app.village.ds.validation-query=select 1

# data source configuration 2
app.city.ds.url=jdbc:mysql://localhost:3306/city
app.city.ds.username=root
app.city.ds.password=
app.city.ds.max-active=200
app.city.ds.max-idle=10
app.city.ds.min-idle=10
app.city.ds.initial-size=15
app.city.ds.validation-query=select 1
```

With them you can easily create two datasources bean in `AppConfig`.

```java
@Bean
@ConfigurationProperties(prefix = "app.village.ds")
public DataSource dataSource1( ) {
    return DataSourceBuilder.create().build();
}

@Bean
@ConfigurationProperties(prefix = "app.city.ds")
public DataSource dataSource2( ) {
    return DataSourceBuilder.create().build();
}
```
> Note if not specified bean name, the method name is used for bean name. Thus here defines
> two beans with name "dataSource1" and "DataSource 2".

> Note, do not use `@Primary` in the on, otherwise all the references marked with
> `@Autowired` will *ONLY* use the bean of `@Primary` instead of autowiring by bean name.

Then inject 2 data sources into 2 session factories.

```java
@Bean(name = "sqlSessionFactory1")
@Autowired
@Qualifier("dataSource1")
public SqlSessionFactory sqlSessionFactory1(DataSource dataSource1) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource1);
    return sessionFactory.getObject();
}

@Bean(name = "sqlSessionFactory2")
@Autowired
@Qualifier("dataSource2")
public SqlSessionFactory sqlSessionFactory2(DataSource dataSource2) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource2);
    return sessionFactory.getObject();
}
```

> Note use `@Autowired` to inject the data source beans into the methods. Do not directly
> invoke like this `sessionFactory.setDataSource(dataSource1())`. This is because 
> `dataSource1()` is marked with `@ConfigurationProperties` which takes effect to
> the result of `dataSource1()`. If you directly use `dataSource1()` method, you will get
> a data source object with empty url, username, password, ...

After that, defines two `MapperScannerConfigurer` beans. Note here you need specify the
session factory bean *name*, instead of their references. Meanwhile, define the package
where you want to search mapper interfaces and mapper xml configurations. You don not
need `@MapperScan` annotation anymore.

```java
@Bean
public MapperScannerConfigurer mapperScannerConfigurer1() {
    MapperScannerConfigurer configurer = new MapperScannerConfigurer();
    configurer.setBasePackage("app.mapper.ds1");
    configurer.setSqlSessionFactoryBeanName("sqlSessionFactory1");
    return configurer;
}

@Bean
public MapperScannerConfigurer mapperScannerConfigurer2() {
    MapperScannerConfigurer configurer = new MapperScannerConfigurer();
    configurer.setBasePackage("app.mapper.ds2");
    configurer.setSqlSessionFactoryBeanName("sqlSessionFactory2");
    return configurer;
}
```

Finally, create `VillageMapper` interface and `VillageMapper.xml` in package `app.mapper.ds1`;
`CityMapper` interface and `CityMapper.xml` in `app.mapper.ds2`. And autowire the mappers in
somewhere you want to use.

# Redist Integration
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-redis</artifactId>
</dependency>
```

This will auto inject `RedisConnectionFactory` and `StringRedisTemplate` bean, which you can
inject directly to your application.

```java
@RestController
public class PhoneController {

    @Autowired
    private void setRedisTemplate(StringRedisTemplate redisTemplate) {
        kvOps = redisTemplate.opsForValue();
        hashOps = redisTemplate.opsForHash();
        listOps = redisTemplate.opsForList();
    }

    private ValueOperations<String, String> kvOps;

    private HashOperations<String, String, String> hashOps;

    private ListOperations<String, String> listOps;

    @Autowired
    private ObjectMapper mapper;

    @RequestMapping(method = RequestMethod.GET, value="/phone/{id}")
    public Phone getPhone(@PathVariable String id) throws IOException {
        String value = kvOps.get("phone:" + id);
        if (value == null) {
            throw new NotFoundException();
        }

        Phone phone = mapper.readValue(value, Phone.class);
        return phone;
    }

    @RequestMapping(method = RequestMethod.POST, value="/phone/new", consumes = "application/json")
    public HttpEntity<Phone> createPhone(@RequestBody Phone phone) throws JsonProcessingException {
        String value = mapper.writeValueAsString(phone);
        kvOps.set("phone:" + phone.id, value);
        listOps.rightPush("phones", value);
        return new ResponseEntity<>(phone, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value="/phones")
    public List<Phone> listPhones() throws IOException {
        List<String> values = listOps.range("phones", 0, -1);
        List<Phone> phones = new ArrayList<>(values.size());
        for (String value: values) {
            phones.add(mapper.readValue(value, Phone.class));
        }
        return phones;
    }
}
```

> Note here `StringRedisTemplate` is a redis client assuming that the value of redis is simply
> string, to "GenericStringSerializer" is used here. In this case, we use json to serialize 
> the object. You can customize your dedicated `RedisTemplate` bean in `AppConfig` by
> specifying serializer (see [here](http://stackoverflow.com/questions/27521672/how-autowired-redistemplatestring-long) for an example).

To configure redis, set following properties in `application.properties`:

```
spring.redis.database=0
spring.redis.host=localhost
spring.redis.password=
spring.redis.port=6379
spring.redis.pool.max-idle=8
spring.redis.pool.min-idle=0
spring.redis.pool.max-active=8
spring.redis.pool.max-wait=-1
```

The redis pool also supports sentinel if you add following properties.

```
spring.redis.sentinel.master= # name of Redis server
spring.redis.sentinel.nodes= # comma-separated list of host:port pairs
```

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

There are several file templates located under `src/main/docker` which will be converted to
the real files that are necessary to build docker images. The generated files will appear under `target` after running `mvn package`.

* Dockerfile - the docker image build script
* build-result.properties - the build result meta info that you can directly source into your script.
* build-docker-img.sh - A utility to build docker image if you like. This is mainly for developers' convinience. Ops will build docker images by their toolkits based on `build-result.properties`
* start-springboot-app.sh - A bootstrap script which is used to start the application in docker.

A typical Dockerfile looks like

```
FROM reg.frontnode.net/ubuntu-jdk8
WORKDIR /app

VOLUME /app/log
VOLUME /app/conf

ADD service-template-0.0.1-SNAPSHOT-1432985823263-0a55a963.jar /app/app.jar
ADD start-springboot-app.sh /app/

# 5005 is assumed to be the java remote debug port if enabled
# 5055 is assumed to be the JMX port if enabled
EXPOSE 8080 5005 5055

CMD ["/bin/sh", "start-springboot-app.sh"]
```
By using such Dockerfile, we have some contracts to follow.

* `reg.frontnode.net/ubuntu-jdk8` is the standard java base image we'll use internally. Always use it unless ops/architects tell you to update it.
* The work directory of java application is always "/app", and the app's all-in-one jar file in docker image is always "/app/app.jar"
* Always mount "/app/config" and "/app/log". Those dirs should be the root of the input/output of your application. For example, when you specify the log output file, you may set it to "log/app.log". Because the workdir in docker is /app, so you the real path is "/app/log/app.log". This setting is also good in your development laptop, where your work dir is normally where `pom.xml` is. Of course you can add more volumes if you need.
* Always declare to expose 3 ports - service port, java remote debugger port and remote JMX port. These information is useful for docker manager to configure port forwarding. Note:
   * declare the port doesn't mean you have to listen it. For example, you can totally disable remote debuging
   * The service port comes from the properties defined in your pom.xml. It's automatically injected to Dockerfile.
   
After `mvn package`, you can simply run the scripts to build docker image and run it.

```
sh target/build-docker-img.sh
```

This will build an image named `reg.frontnode.net/app/service-template-0.0.1-SNAPSHOT-1432985823263-0a55a963:latest`.

> Note, since docker search can't list all the tags of a docker repository, we always use "latest" and append the tag (or version) to the image name.

Finally run it.

```
docker run -d reg.frontnode.net/app/service-template-0.0.1-SNAPSHOT-1432985823263-0a55a963:latest
```

You may try to configure how to run the docker images such as specifying port forwarding by "-p/-P", specifying the volume mapping "-v" or inject some java options like

```
docker run -e "APP_DEBUG=true" -d reg.frontnode.net/app/service-template-0.0.1-SNAPSHOT-1432985823263-0a55a963:latest
```

This will start the java remote debug agent listening on 5005. Check the content of "start-springboot-app.sh" for all the possible environment variables.
