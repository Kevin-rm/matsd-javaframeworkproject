export type AppDetails = {
  javaVersion: string;
  jakartaEEVersion: string;
  matsdjavaframeworkVersion: string;
};

export type RequestInfo = {
  method: string;
  serverName: string;
  port: number;
  uri: string;
  headers: Record<string, string>;
  body?: Record<string, unknown>;
};

export type Exception = {
  className: string;
  message: string;
  stackTrace: string;
};

export type ExceptionFile = {
  fullPath: string;
  method: string;
  sourceCode: string;
  highlightedLine: number;
};

export type Error = {
  statusCodeReason: string;
  appDetails: AppDetails;
  requestInfo: RequestInfo;
  exception: Exception;
  exceptionFiles?: ExceptionFile[];
};
