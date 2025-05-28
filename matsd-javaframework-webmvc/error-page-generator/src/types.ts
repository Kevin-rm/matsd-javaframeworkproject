export type AppDetails = {
  javaVersion: string;
  matsdjavaframeworkVersion: string;
  serverInfo: string;
  contextPath: string;
};

export type RequestInfo = {
  method: string;
  url: string;
  headers: Record<string, string>;
  body?: Record<string, unknown>;
};

export type Exception = {
  className: string;
  message: string;
  stackTrace: string;
};

export type Error = {
  statusCodeReason: string;
  appDetails: AppDetails;
  requestInfo: RequestInfo;
  exception: Exception;
};
