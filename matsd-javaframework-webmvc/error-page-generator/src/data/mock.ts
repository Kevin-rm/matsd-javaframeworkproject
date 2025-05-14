import type { AppDetails, Error, Exception, ExceptionFile, RequestInfo } from "../types.ts";

const appDetails: AppDetails = {
  javaVersion: "21.0.1",
  jakartaEEVersion: "10.0.0",
  matsdjavaframeworkVersion: "1.0-SNAPSHOT",
};

const requestInfo: RequestInfo = {
  method: "POST",
  serverName: "localhost",
  port: 8080,
  uri: "/api/users",
  headers: {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
    Accept: "application/json",
    "Content-Type": "application/json",
  },
  body: {
    userId: -1,
    action: "find",
  },
};

const exception: Exception = {
  className: "mg.matsd.javaframework.NotFoundHttpException",
  message: `error: unreported exception FileNotFoundException; must be caught or declared to be thrown 
  FileReader fr = new FileReader(file);
`,
  stackTrace: `Exception in thread "main" java.lang.RuntimeException: Something has gone wrong, aborting!
  at com.myproject.module.MyProject.badMethod(MyProject.java:22)
  at com.myproject.module.MyProject.oneMoreMethod(MyProject.java:18)
  at com.myproject.module.MyProject.anotherMethod(MyProject.java:14)
  at com.myproject.module.MyProject.someMethod(MyProject.java:10)
  at com.myproject.module.MyProject.main(MyProject.java:6)
`,
};

const exceptionFiles: ExceptionFile[] = [
  {
    fullPath: "src/controllers/SampleController.java",
    method: "show",
    sourceCode: `package mg.matsd.javaframework.servletwrapper.base;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.matsd.javaframework.servletwrapper.http.Request;
import mg.matsd.javaframework.servletwrapper.http.Response;

import java.io.IOException;

public class ServletWrapper extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request  request  = new Request(req);
        Response response = new Response(resp, request);
        try {
            handleRequest(request, response);
            if (response.isCommitted()) return;

            switch (request.getMethod()) {
                case "GET"     -> get    (request, response);
                case "POST"    -> post   (request, response);
                case "PUT"     -> put    (request, response);
                case "DELETE"  -> delete (request, response);
                case "HEAD"    -> head   (request, response);
                case "OPTIONS" -> options(request, response);
                case "TRACE"   -> trace  (request, response);
                default        -> throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleRequest(Request request, Response response) throws Exception { }

    protected void get(Request request, Response response) throws Exception {
        response.methodNotAllowed();
    }

    protected void post(Request request, Response response) throws Exception {
        response.methodNotAllowed();
    }

    protected void put(Request request, Response response) throws Exception {
        response.methodNotAllowed();
    }

    protected void delete(Request request, Response response) throws Exception {
        response.methodNotAllowed();
    }

    protected void head(Request request, Response response) throws Exception {
        response.methodNotAllowed();
    }

    protected void options(Request request, Response response) throws Exception {
        response.methodNotAllowed();
    }

    protected void trace(Request request, Response response) throws Exception {
        response.methodNotAllowed();
    }
}`,
    highlightedLine: 4,
  },
  {
    fullPath: "src/services/UserService.java",
    method: "findUser",
    sourceCode: `public class UserService {
    public User findUser(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
        // ...
    }
}`,
    highlightedLine: 3,
  },
];

export const errorMockData: Error = {
  statusCodeReason: "Internal Server Error",
  appDetails: appDetails,
  requestInfo: requestInfo,
  exception: exception,
  exceptionFiles: exceptionFiles,
};
