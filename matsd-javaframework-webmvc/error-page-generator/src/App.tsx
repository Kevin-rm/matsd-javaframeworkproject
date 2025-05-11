import { useState } from "react";
import type { Error, Exception, ExceptionFile } from "./types.ts";
import { errorMockData } from "./data/mock.ts";
import { Card, CardContent, CardHeader, CardTitle } from "./components/ui/card";
import { Badge } from "./components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./components/ui/tabs";
import { Separator } from "./components/ui/separator";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./components/ui/collapsible";
import { ThemeProvider } from "@/components/ThemeProvider.tsx";

const Header = ({ statusCodeReason }: { statusCodeReason: string }) => {
  return (
    <div className="flex items-center gap-4">
      <div className="bg-red-500 rounded-full p-2">
        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24"
             stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
        </svg>
      </div>
      <div>
        <h1 className="text-2xl font-semibold">{statusCodeReason}</h1>
      </div>
    </div>
  );
};

const Exception = ({ exception }: { exception: Exception }) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{exception.className}</CardTitle>
      </CardHeader>
      <CardContent>
        {exception.message}
      </CardContent>
    </Card>
  );
};

const App = () => {
  const [error] = useState<Error>(errorMockData);
  const [openSections, setOpenSections] = useState<Record<string, boolean>>({
    requestInfo: false,
    appDetails: false
  });

  const toggleSection = (section: string) => {
    setOpenSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const renderCodeWithHighlight = (file: ExceptionFile) => {
    const lines = file.sourceCode.split('\n');
    return (
      <pre className="text-sm overflow-x-auto">
        {lines.map((line, index) => (
          <div
            key={index}
            className={`px-4 py-1 flex ${index + 1 === file.highlightedLine ? 'bg-red-900/30 border-l-2 border-red-500' : ''}`}
          >
            <span className="text-gray-500 w-8 inline-block select-none">{index + 1}</span>
            <code>{line}</code>
          </div>
        ))}
      </pre>
    );
  };

  return (
    <ThemeProvider defaultTheme="dark">
      <div className="min-h-screen bg-gray-950 px-6 py-12">
        <div className="max-w-6xl mx-auto">
          <Header statusCodeReason={error.statusCodeReason}/>
          <Exception exception={error.exception}/>
        </div>

        {/* Main Content */}
        <div className="max-w-6xl mx-auto py-8 px-6">
          <Tabs defaultValue="stack-trace" className="w-full">
            <TabsList className="bg-gray-900 border border-gray-800 rounded-lg overflow-hidden mb-6">
              <TabsTrigger value="stack-trace">Stack Trace</TabsTrigger>
              <TabsTrigger value="request">Request</TabsTrigger>
              <TabsTrigger value="app-info">Application Info</TabsTrigger>
            </TabsList>

            {/* Stack Trace Tab */}
            <TabsContent value="stack-trace" className="space-y-6">
              <Card className="bg-gray-900 border-gray-800">
                <CardHeader>
                  <CardTitle className="text-lg font-medium">Stack Trace</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-1 text-gray-400 font-mono text-sm">
                    {error.exception.stackTrace.split('\n').map((line, index) => (
                      <div key={index} className="py-1">{line}</div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              {error.exceptionFiles && error.exceptionFiles.map((file, index) => (
                <Card key={index} className="bg-gray-900 border-gray-800">
                  <CardHeader className="pb-2">
                    <CardTitle className="text-lg flex items-center gap-3">
                      <span>{file.fullPath}</span>
                      <Badge variant="outline" className="bg-gray-800 text-red-400 border-gray-700">
                        {file.method}
                      </Badge>
                    </CardTitle>
                  </CardHeader>
                  <Separator className="bg-gray-800"/>
                  <CardContent className="p-0 overflow-hidden font-mono">
                    {renderCodeWithHighlight(file)}
                  </CardContent>
                </Card>
              ))}
            </TabsContent>

            {/* Request Info Tab */}
            <TabsContent value="request">
              <Card className="bg-gray-900 border-gray-800">
                <CardHeader>
                  <CardTitle className="text-lg font-medium">Request Information</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div className="font-medium text-gray-400">Method</div>
                      <div>{error.requestInfo.method}</div>

                      <div className="font-medium text-gray-400">URL</div>
                      <div>{`${error.requestInfo.serverName}:${error.requestInfo.port}${error.requestInfo.uri}`}</div>

                      <div className="font-medium text-gray-400">Status</div>
                      <div>{error.statusCodeReason}</div>
                    </div>

                    <Collapsible
                      open={openSections.requestInfo}
                      onOpenChange={() => toggleSection('requestInfo')}
                      className="mt-4 border border-gray-800 rounded-lg overflow-hidden"
                    >
                      <CollapsibleTrigger
                        className="flex items-center justify-between w-full p-4 bg-gray-800 hover:bg-gray-700">
                        <span className="font-medium">Headers</span>
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          className={`h-5 w-5 transition-transform ${openSections.requestInfo ? 'rotate-180' : ''}`}
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7"/>
                        </svg>
                      </CollapsibleTrigger>
                      <CollapsibleContent className="p-4 bg-gray-900">
                        <div className="space-y-2">
                          {Object.entries(error.requestInfo.headers).map(([key, value]) => (
                            <div key={key} className="grid grid-cols-2">
                              <div className="font-medium text-gray-400">{key}</div>
                              <div className="break-all">{value}</div>
                            </div>
                          ))}
                        </div>
                      </CollapsibleContent>
                    </Collapsible>

                    {error.requestInfo.body && (
                      <div className="mt-4">
                        <h3 className="text-md font-medium mb-2">Request Body</h3>
                        <div className="bg-gray-800 p-4 rounded-lg">
                        <pre className="text-sm overflow-x-auto">
                          {JSON.stringify(error.requestInfo.body, null, 2)}
                        </pre>
                        </div>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* App Info Tab */}
            <TabsContent value="app-info">
              <Card className="bg-gray-900 border-gray-800">
                <CardHeader>
                  <CardTitle className="text-lg font-medium">Application Details</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    <div className="grid grid-cols-2 gap-2">
                      <div className="font-medium text-gray-400">Java Version</div>
                      <div>{error.appDetails.javaVersion}</div>

                      <div className="font-medium text-gray-400">Jakarta EE Version</div>
                      <div>{error.appDetails.jakartaEEVersion}</div>

                      <div className="font-medium text-gray-400">Framework Version</div>
                      <div>{error.appDetails.matsdjavaframeworkVersion}</div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </ThemeProvider>
  );
};

export default App;
