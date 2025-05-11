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
  className: "java.lang.IllegalArgumentException",
  message: "ID must be positive",
  stackTrace: `at src/services/UserService.java:3
at src/controllers/SampleController.java:4`,
};

const exceptionFiles: ExceptionFile[] = [
  {
    fullPath: "src/controllers/SampleController.java",
    method: "show",
    sourceCode: `public class SampleController {
    public void show() {
        String value = null;
        System.out.println(value.length()); // Provoque NullPointerException
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
