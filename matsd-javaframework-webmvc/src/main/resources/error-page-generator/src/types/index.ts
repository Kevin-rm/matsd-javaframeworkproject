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
