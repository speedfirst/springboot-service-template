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
<dependency>
	<groupId>commons-dbcp</groupId>
	<artifactId>commons-dbcp</artifactId>
	<version>1.4</version>
</dependency>
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