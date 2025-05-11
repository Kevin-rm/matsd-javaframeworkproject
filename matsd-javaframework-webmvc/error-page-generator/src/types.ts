export type ErrorFile = {
  path: string;
  content: string;
  highlight: {
    line: number;
    startColumn?: number;
    endColumn?: number;
  } | null;
}

export type ErrorTraceItem = {
  file: string;
  line: number;
  method: string;
  fileIndex?: number; // index in the files array
}

export type ErrorData = {
  type: string;
  message: string;
  statusCode: number;
  trace: ErrorTraceItem[];
  files: ErrorFile[];
}

export type Exception = {
  className: string;
  message: string;
};

export type AppDetails = {
  javaVersion: string;
  jakartaEEVersion: string;
  matsdjavaframeworkVersion: string;
};

export type File = {
  exception: Exception;
  fullPath: string;
  method: string;
  highlightedLine: number;
  code: string;
};

export type RequestInfo = {
  method: string;
  serverName: string;
  port: number;
  uri: string;
  headers: Record<string, string>;
  body?: Record<string, unknown>;
  statusCodeReason: string;
};

export type Error = {
  exception: Exception;
  files: File[];
  requestInfo: RequestInfo;
  appDetails: AppDetails;
};
