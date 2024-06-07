# matsd-javaframework-webmvc

The webmvc project is a lightweight MVC framework based on servlets in Java, designed to simplify web application development.

## Description

This framework provides an organized structure for web application development following the MVC (Model-View-Controller) pattern. It facilitates separation of concerns by dividing the application into three main parts : models for business logic, views for user interface, and controllers for handling HTTP requests.

## Getting Started

### 1. Integration

Begin by importing the `matsd-javaframework-webmvc` and the `matsd-javaframework-core` as dependencies into your Java web project. This can be achieved by including the framework JAR file in your project's build path.

### 2. Configuration

Configure the servlet mapping in your web.xml file to ensure proper routing of HTTP requests :

```xml
<servlet>
    <servlet-name>frontServlet</servlet-name>
    <servlet-class>mg.itu.prom16.base.FrontServlet</servlet-class>

    <!-- The location of the container configuration file (required) -->
    <init-param>
        <param-name>containerConfigLocation</param-name>
        <param-value>/WEB-INF/container.xml</param-value>
    </init-param>
</servlet>

<servlet-mapping>
    <servlet-name>frontServlet</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

### 3. Define your controllers

Specify the package to scan for controllers in the configuration file located at the path defined by `containerConfigLocation` (i.e., /WEB-INF/container.xml). Below is an example demonstrating how to configure component scanning :

```xml
<managed-instances xmlns="http://www.matsd.mg/javaframework/schema/managedinstances"
                   xmlns:container="http://www.matsd.mg/javaframework/schema/container">

    <!-- You can declare managedinstances here -->

    <!-- Define component scanning to automatically detect and register controllers -->
    <container:component-scan base-package="com.example.controllers"/>
</managed-instances>
```
Then, annotate your Java class with the `@Controller` annotation to recognize it as a controller. Here's an example:

```java
@Controller
@RequestMapping("/hello")
public class Welcome {
    
    @Get("/test")
    public String home() {
        return "This is a test";
    }

    @Get("/")
    public String hello() {
        return "Hello World";
    }

    // This is a utility method, not exposed as a route
    private void anUtilMethod() {
        System.out.println("I am very useful");
    }
}
```

Note:

* Ensure that the path specified in `@RequestMapping` and its variants starts with a leading `/`. Omitting the `/` at the beginning will result in an error.
* Controller methods that handle HTTP requests should not be private. They need to be public to be accessible by the framework.
