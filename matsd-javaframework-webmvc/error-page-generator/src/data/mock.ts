import type { AppDetails, Error, Exception, RequestInfo, StackTraceElement } from "../types.ts";

const appDetails: AppDetails = {
  javaVersion: "21.0.1",
  matsdjavaframeworkVersion: "1.0-SNAPSHOT",
  serverInfo: "Apache Tomcat",
  contextPath: "/mon-app"
};

const requestInfo: RequestInfo = {
  method: "POST",
  url: "localhost:8080/api/users",
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

const stackTraceElements: StackTraceElement[] = [
  {
    className: "mg.itu.prom16.base.internal.handler.AbstractHandler",
    methodName: "invokeMethod",
    lineNumber: 117,
    fileName: "AbstractHandler.java"
  },
  {
    className: "mg.itu.prom16.base.ResponseRenderer",
    methodName: "doRender", 
    lineNumber: 96,
    fileName: "ResponseRenderer.java"
  },
  {
    className: "mg.itu.prom16.base.FrontServlet",
    methodName: "processRequest",
    lineNumber: 170,
    fileName: "FrontServlet.java"
  },
  {
    className: "mg.itu.prom16.base.FrontServlet",
    methodName: "service",
    lineNumber: 188,
    fileName: "FrontServlet.java"
  },
  {
    className: "jakarta.servlet.http.HttpServlet",
    methodName: "service",
    lineNumber: 658,
    fileName: "HttpServlet.java"
  },
  {
    className: "org.apache.catalina.core.ApplicationFilterChain",
    methodName: "internalDoFilter",
    lineNumber: 205,
    fileName: "ApplicationFilterChain.java"
  },
  {
    className: "org.apache.catalina.core.ApplicationFilterChain",
    methodName: "doFilter",
    lineNumber: 149,
    fileName: "ApplicationFilterChain.java"
  },
  {
    className: "org.apache.tomcat.websocket.server.WsFilter",
    methodName: "doFilter",
    lineNumber: 51,
    fileName: "WsFilter.java"
  },
  {
    className: "mg.itu.ticketing.controller.PublicController",
    methodName: "home",
    lineNumber: 12,
    fileName: "PublicController.java"
  },
  {
    className: "java.lang.reflect.Method",
    methodName: "invoke",
    lineNumber: 580
  },
  {
    className: "java.lang.Thread",
    methodName: "run",
    lineNumber: 1583
  }
];

const exception: Exception = {
  className: "mg.matsd.javaframework.NotFoundHttpException",
  message: `error: unreported exception FileNotFoundException; must be caught or declared to be thrown 
  FileReader fr = new FileReader(file);
`,
  stackTraceElements: stackTraceElements,
  stackTrace: `jakarta.servlet.ServletException: java.lang.RuntimeException: Coucou les amis
\tat mg.itu.prom16.base.internal.handler.AbstractHandler.invokeMethod(AbstractHandler.java:117)
\tat mg.itu.prom16.base.ResponseRenderer.doRender(ResponseRenderer.java:96)
\tat mg.itu.prom16.base.FrontServlet.processRequest(FrontServlet.java:170)
\tat mg.itu.prom16.base.FrontServlet.service(FrontServlet.java:188)
\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)
\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:205)
\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)
\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)
\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:174)
\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:149)
\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:166)
\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90)
\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482)
\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)
\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)
\tat org.apache.catalina.valves.AbstractAccessLogValve.invoke(AbstractAccessLogValve.java:676)
\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)
\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341)
\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:391)
\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)
\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:894)
\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1741)
\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)
\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191)
\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)
\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
\tat java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.lang.RuntimeException: Coucou les amis
\tat mg.itu.ticketing.controller.PublicController.home(PublicController.java:12)
\tat java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
\tat java.base/java.lang.reflect.Method.invoke(Method.java:580)
\tat mg.itu.prom16.base.internal.handler.AbstractHandler.invokeMethod(AbstractHandler.java:113)
\t... 26 more
`,
};

export const errorMockData: Error = {
  statusCodeReason: "Internal Server Error",
  appDetails: appDetails,
  requestInfo: requestInfo,
  exception: exception
};
