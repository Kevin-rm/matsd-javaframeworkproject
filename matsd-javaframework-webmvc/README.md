# matsd-javaframework-webmvc

The webmvc project is a lightweight MVC framework based on servlets in Java, designed to simplify web application development.

# Description

This framework provides an organized structure for web application development following the MVC (Model-View-Controller) pattern. It facilitates separation of concerns by dividing the application into three main parts : models for business logic, views for user interface, and controllers for handling HTTP requests.

# Getting Started

## 1. Integration

Begin by importing the `matsd-javaframework-webmvc` and the `matsd-javaframework-core` as dependencies into your Java web project. This can be achieved by including the framework JAR file in your project's build path.

## 2. Configuration

Configure the servlet mapping in your web.xml file to ensure proper routing of HTTP requests :

```xml
<servlet>
    <servlet-name>frontServlet</servlet-name>
    <servlet-class>mg.itu.prom16.base.FrontServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>frontServlet</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

## 3. Define your controllers

First in your web.xml file, specify the package to scan for controllers using context-param :

```xml
<context-param>
    <param-name>controller-package</param-name>
    <!-- Replace the value below with the package name of your controllers -->
    <param-value>com.example.controllers</param-value>
</context-param>
```
Then, your Java class has to be annotated with the `@Controller` annotation for it to be recognized as a controller.
Now, let's create a controller and add some examples of mappings :

```java
@Controller
@RequestMapping("/hello")
public class Welcome {
    @Get("/test")
    public String home() {
        return "Ceci est un test";
    }

    @Get("/")
    public String hello() {
        return "Hello World";
    }
    
    private void anUtilMethod() {
        System.out.println("I am very useful");
    }
}
```

In this example, accessing "/hello/test" will return "Ceci est un test", and accessing "/hello" will return "Hello World".e 
