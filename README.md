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

