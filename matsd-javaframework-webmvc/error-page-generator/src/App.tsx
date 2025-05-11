import { useState } from "react";
import type { Error, Exception, ExceptionFile } from "./types.ts";
import { errorMockData } from "./data/mock.ts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card.tsx";
import { Badge } from "@/components/ui/Badge.tsx";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/Tabs.tsx";
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
    <Card className="bg-gray-900 border-gray-800 mt-6">
      <CardHeader className="flex flex-row items-center gap-4">
        <CardTitle>
          <Badge variant="destructive" className="px-3 py-2 rounded-3xl text-sm">
            {exception.className}
          </Badge>
        </CardTitle>
      </CardHeader>
      <CardContent>
        <p className="font-medium text-2xl">{exception.message}</p>
      </CardContent>
    </Card>
  );
};

const App = () => {
  const [error] = useState<Error>(errorMockData);
  const [selectedFileIndex, setSelectedFileIndex] = useState<number>(0);

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
      <div className="min-h-screen bg-gray-950 px-6 py-8">
        <div className="max-w-6xl mx-auto">
          <Header statusCodeReason={error.statusCodeReason}/>
          <Exception exception={error.exception}/>
          <Tabs defaultValue="sources" className="mt-8">
            <TabsList className="bg-gray-900 border border-gray-800 rounded-lg overflow-hidden mb-6">
              <TabsTrigger value="sources">Sources</TabsTrigger>
              <TabsTrigger value="stack-trace">Piles d'appel</TabsTrigger>
              <TabsTrigger value="request">Requête</TabsTrigger>
              <TabsTrigger value="app-details">Application</TabsTrigger>
            </TabsList>

            {/* Fichiers Source Tab - Two Column Layout */}
            <TabsContent value="sources" className="space-y-6">
              <Card className="bg-gray-900 border-gray-800">
                <div className="grid grid-cols-12 min-h-[400px]">
                  {/* Left column - File list */}
                  <div className="col-span-4 border-r border-gray-800 overflow-y-auto">
                    <div className="p-4 border-b border-gray-800">
                      <h3 className="font-medium text-sm text-gray-400 uppercase">Fichiers sources</h3>
                    </div>
                    <div className="divide-y divide-gray-800">
                      {error.exceptionFiles && error.exceptionFiles.map((file, index) => (
                        <button
                          key={index}
                          onClick={() => setSelectedFileIndex(index)}
                          className={`w-full text-left px-4 py-3 hover:bg-gray-800 transition-colors ${
                            selectedFileIndex === index ? 'bg-gray-800 border-l-2 border-red-500' : ''
                          }`}
                        >
                          <div className="flex flex-col">
                            <span className="font-medium truncate">{file.fullPath}</span>
                            <span className="text-xs text-gray-500">{file.method}</span>
                          </div>
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Right column - Code view */}
                  <div className="col-span-8 overflow-auto">
                    {error.exceptionFiles && error.exceptionFiles.length > 0 && (
                      <div>
                        <div
                          className="p-4 border-b border-gray-800 sticky top-0 bg-gray-900 flex justify-between items-center">
                          <div className="flex items-center gap-2">
                            <span className="font-medium">{error.exceptionFiles[selectedFileIndex].fullPath}</span>
                          </div>
                        </div>
                        <div className="font-mono">
                          {renderCodeWithHighlight(error.exceptionFiles[selectedFileIndex])}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </Card>
            </TabsContent>

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
            </TabsContent>

            {/* Request Info Tab */}
            <TabsContent value="request">
              <Card className="bg-gray-900 border-gray-800">
                <CardHeader>
                  <CardTitle className="text-lg font-medium">Informations de requête</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-6">
                    <div>
                      <h3 className="text-md font-medium mb-2">Détails de base</h3>
                      <div className="border border-gray-800 rounded-md overflow-hidden">
                        <table className="w-full border-collapse">
                          <tbody>
                          <tr className="border-b border-gray-800">
                            <td className="px-4 py-3 bg-gray-800/50 font-medium text-gray-400 w-1/4">Méthode</td>
                            <td className="px-4 py-3">{error.requestInfo.method}</td>
                          </tr>
                          <tr className="border-b border-gray-800">
                            <td className="px-4 py-3 bg-gray-800/50 font-medium text-gray-400">URL</td>
                            <td
                              className="px-4 py-3">{`${error.requestInfo.serverName}:${error.requestInfo.port}${error.requestInfo.uri}`}</td>
                          </tr>
                          <tr>
                            <td className="px-4 py-3 bg-gray-800/50 font-medium text-gray-400">Status</td>
                            <td className="px-4 py-3">{error.statusCodeReason}</td>
                          </tr>
                          </tbody>
                        </table>
                      </div>
                    </div>

                    <div>
                      <h3 className="text-md font-medium mb-2">Headers</h3>
                      <div className="border border-gray-800 rounded-md overflow-hidden">
                        <table className="w-full border-collapse">
                          <tbody>
                          {Object.entries(error.requestInfo.headers).map(([key, value], index, arr) => (
                            <tr key={key} className={index < arr.length - 1 ? "border-b border-gray-800" : ""}>
                              <td className="px-4 py-3 bg-gray-800/50 font-medium text-gray-400 w-1/4">{key}</td>
                              <td className="px-4 py-3 break-all">{value}</td>
                            </tr>
                          ))}
                          </tbody>
                        </table>
                      </div>
                    </div>

                    {error.requestInfo.body && (
                      <div>
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
            <TabsContent value="app-details">
              <Card className="bg-gray-900 border-gray-800">
                <CardHeader>
                  <CardTitle className="text-lg font-medium">Détails de l'application</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="border border-gray-800 rounded-md overflow-hidden">
                    <table className="w-full border-collapse">
                      <tbody>
                      <tr className="border-b border-gray-800">
                        <td className="px-4 py-3 bg-gray-800/50 font-medium text-gray-400 w-1/4">Version Java</td>
                        <td className="px-4 py-3">{error.appDetails.javaVersion}</td>
                      </tr>
                      <tr className="border-b border-gray-800">
                        <td className="px-4 py-3 bg-gray-800/50 font-medium text-gray-400">Version Jakarta EE</td>
                        <td className="px-4 py-3">{error.appDetails.jakartaEEVersion}</td>
                      </tr>
                      <tr>
                        <td className="px-4 py-3 bg-gray-800/50 font-medium text-gray-400">Version du Framework</td>
                        <td className="px-4 py-3">{error.appDetails.matsdjavaframeworkVersion}</td>
                      </tr>
                      </tbody>
                    </table>
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
