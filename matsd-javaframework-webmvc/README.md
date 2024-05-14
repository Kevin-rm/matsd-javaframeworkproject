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
    <servlet-class>mg.itu.prom16.FrontServlet</servlet-class>
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
Then, your Java class has to be annotated with the `@Controller` annotation for it to be recognized as a controller. Your class should look like this :

```java
@Controller
public class Welcome {
    /* Your methods */
}
```
