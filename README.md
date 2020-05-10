### A custom spring boot starter project.

  Mainly provides interface for requesting log information printing function, which simplifies the need to write an interface requesting log function for each project.

  [中文注释](./README_CN.md) 


### How to use

1. **git clone https://github.com/727474430/web-logging-spring-boot-starter.git**

2. **cd web-logging-spring-boot-starter**

3. **mvn install**

4. **Introducing dependencies in spring boot project that need to be used**

   ```xml
   <dependency>
       <groupId>com.raindrop</groupId>
       <artifactId>web-logging-spring-boot-starter</artifactId>
       <version>1.1.RELEASE</version>
   </dependency>
   ```

5. **Add following attributes in application.properties/application.yml**

   * web.log.enable=true

     Whether to open the log printing function. default is **"false"** not open. select true is open

   * web.log.exclude-path=/disable;/nomapping

     Need to exclude the path, Use **";"** split multiple request paths

   * web.log.print-header

     Need to printing request header, User **";"** split multiple request headers



### Example

* application.properties

  ```properties
  web.log.enable=true
  web.log.print-header=Host;Connection;
  web.log.exclude-path=/disable
  ```

* application.yml

  ```yaml
  web:
    log:
      enable: true
      exclude-path: /disable
      print-header: Host;Connection;
  ```


### Screenshots

![](src/main/resources/img/properties.png)

![](src/main/resources/img/anno.png)

![](src/main/resources/img/logging.png)


[![](https://jitpack.io/v/727474430/web-logging-spring-boot-starter.svg)](https://jitpack.io/#727474430/web-logging-spring-boot-starter)