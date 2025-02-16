# matsd-javaframeworkproject

A lightweight, modular Java framework designed for developer productivity, offering independent yet interoperable components. It provides essential building blocks for modern applications while ensuring flexibility in adoption.

## Key Features

- Modular Design
- Ease of Use
- Extensibility
- Highly Configurable

## Projects

1.  **core**: Foundation layer providing essential utilities for all framework components.
2.  **webmvc**: MVC implementation for building modern web applications with minimal boilerplate.
3.  **orm**: Implements an Object-Relational Mapping solution for database interactions.
4.  **security**: Comprehensive security suite including authentication, authorization, and common security patterns.
5.  **validation**: Flexible validation framework supporting both built-in and custom validation rules for data integrity.

## Get Started

### Prerequisites

- Java 19 or higher
- Maven (optional if you use an IDE with built-in Maven support)

### Installation Options

To start using this framework you have several options, but here are two of them:

#### Option 1: Add as Maven Dependency (Recommended)

1. Add the GitHub Package Registry to your `pom.xml`:
   ```xml
   <repositories>
       <repository>
           <id>github</id>
           <url>https://maven.pkg.github.com/Kevin-rm/matsd-javaframeworkproject</url>
       </repository>
   </repositories>
   ```
2. Configure GitHub Authentication:
   - Generate a [Personal Access Token (PAT)](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) with the ``read:packages`` scope.
   - Add the credentials to your ``~/.m2/settings.xml``:
   ```xml
    <servers>
        <server>
            <id>github</id>
            <username>your-github-username</username>
            <password>your-PAT</password>
        </server>
    </servers>
    ```
3. Add dependencies to your project.

#### Option 2: Build from Source

1. Clone the repository:
    ```bash
    git clone https://github.com/Kevin-rm/matsd-javaframeworkproject.git
    cd matsd-javaframeworkproject
    ```
2. Build with Maven:
    ```bash
    mvn clean install
    ```
3. Import the built JARs into your project or reference them in your local Maven repository.

## Contributions

I welcome contributions to the framework! If you're interested, please follow these guidelines:

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix.
3.  Write tests for your changes.
4.  Ensure all tests pass.
5.  Submit a pull request with a clear description of your changes.
