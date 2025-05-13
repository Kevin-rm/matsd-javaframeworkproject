"use client"

import { useState } from "react"
import type { AppDetails, Error, Exception as ExceptionType, ExceptionFile, RequestInfo } from "./types.ts"
import { errorMockData } from "./data/mock.ts"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ThemeProvider } from "@/components/ThemeProvider.tsx"
import CodeBlock from "@/components/CodeBlock.tsx"
import {
  AlertTriangle,
  Code,
  FileCode,
  Layers,
  Server,
  Settings,
  ChevronRight,
  AlertCircle,
  CornerDownRight,
} from "lucide-react"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { motion } from "framer-motion"

const Header = ({ statusCodeReason }: { statusCodeReason: string }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="flex items-center gap-4 mb-6"
    >
      <div className="bg-gradient-to-br from-red-600 via-red-500 to-rose-500/90 text-white rounded-full p-4 shadow-2xl shadow-red-800/30 ring-4 ring-red-500/10 backdrop-blur-md">
        <AlertTriangle className="h-7 w-7 drop-shadow-lg" />
      </div>
      <div>
        <h1 className="text-4xl font-extrabold tracking-tight drop-shadow-sm">{statusCodeReason}</h1>
        <p className="text-muted-foreground mt-1 text-lg font-medium">
          Une erreur s'est produite lors du traitement de votre requête
        </p>
      </div>
    </motion.div>
  )
}

const Exception = ({ exception }: { exception: ExceptionType }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.2 }}
    >
      <Card className="bg-gradient-to-br from-gray-900/80 to-gray-950/90 border-gray-800/80 mt-6 overflow-hidden shadow-2xl backdrop-blur-md ring-1 ring-white/10">
        <CardHeader className="pb-2">
          <div className="flex items-center gap-2">
            <AlertCircle className="h-5 w-5 text-red-500 animate-pulse" />
            <CardTitle className="text-lg font-semibold tracking-wide">Exception détectée</CardTitle>
          </div>
          <CardDescription>
            <Badge variant="destructive" className="mt-1 px-3 py-1 rounded-md text-sm font-mono shadow">
              {exception.className}
            </Badge>
          </CardDescription>
        </CardHeader>
        <CardContent>
          <p className="font-semibold text-xl text-red-400 drop-shadow">{exception.message}</p>
        </CardContent>
      </Card>
    </motion.div>
  )
}

const App = () => {
  const [error] = useState<Error>(errorMockData)
  const [selectedFileIndex, setSelectedFileIndex] = useState<number>(0)

  const SourcesTab = ({ exceptionFiles }: { exceptionFiles?: ExceptionFile[] }) => {
    if (!exceptionFiles || exceptionFiles.length === 0) return null

    return (
      <TabsContent value="sources" className="animate-in fade-in-50 duration-300">
        <Card className="bg-gradient-to-br from-gray-900 to-gray-950 border-gray-800 shadow-xl">
          <CardContent className="p-0">
            <div className="grid grid-cols-12 min-h-[500px]">
              {/* Left column - File list */}
              <div className="col-span-12 md:col-span-4 border-r border-gray-800 overflow-hidden">
                <div className="p-4 border-b border-gray-800 bg-muted/10 sticky top-0 z-10">
                  <div className="flex items-center gap-2">
                    <FileCode className="h-4 w-4 text-muted-foreground" />
                    <h3 className="font-medium text-sm text-muted-foreground">FICHIERS SOURCES</h3>
                  </div>
                </div>
                <ScrollArea className="h-[calc(500px-57px)]">
                  <div className="divide-y divide-gray-800/50">
                    {exceptionFiles.map((file, index) => (
                      <button
                        key={index}
                        onClick={() => setSelectedFileIndex(index)}
                        className={`w-full text-left px-4 py-3 hover:bg-gray-800/50 transition-colors ${
                          selectedFileIndex === index ? "bg-gray-800/70 border-l-2 border-red-500" : ""
                        }`}
                      >
                        <div className="flex items-start gap-3">
                          <Code
                            className={`h-5 w-5 mt-0.5 ${selectedFileIndex === index ? "text-red-400" : "text-muted-foreground"}`}
                          />
                          <div className="flex flex-col">
                            <span className="font-medium truncate">{file.fullPath}</span>
                            <span className="text-xs text-muted-foreground flex items-center gap-1 mt-1">
                              <CornerDownRight className="h-3 w-3" />
                              {file.method}
                            </span>
                          </div>
                        </div>
                      </button>
                    ))}
                  </div>
                </ScrollArea>
              </div>

              {/* Right column - Code view */}
              <div className="col-span-12 md:col-span-8 overflow-hidden">
                <div className="p-4 border-b border-gray-800 bg-muted/10 sticky top-0 z-10">
                  <div className="flex items-center gap-2">
                    <div className="flex items-center gap-1 text-muted-foreground">
                      <span className="font-mono text-sm">{exceptionFiles[selectedFileIndex].fullPath}</span>
                      <ChevronRight className="h-4 w-4" />
                      <span className="text-sm font-medium text-red-400">
                        Ligne {exceptionFiles[selectedFileIndex].highlightedLine}
                      </span>
                    </div>
                  </div>
                </div>
                <ScrollArea className="h-[calc(500px-57px)] p-4">
                  <div className="font-mono">
                    <CodeBlock
                      code={exceptionFiles[selectedFileIndex].sourceCode}
                      highlightedLine={exceptionFiles[selectedFileIndex].highlightedLine}
                    />
                  </div>
                </ScrollArea>
              </div>
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    )
  }

  const StackTraceTab = ({ stackTrace }: { stackTrace: string }) => {
    return (
      <TabsContent value="stack-trace" className="animate-in fade-in-50 duration-300">
        <Card className="bg-gradient-to-br from-gray-900 to-gray-950 border-gray-800 shadow-xl">
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <Layers className="h-5 w-5 text-amber-500" />
            <CardTitle className="text-lg font-medium">Stack Trace</CardTitle>
          </CardHeader>
          <CardContent>
            <ScrollArea className="h-[400px] rounded-md border border-border/30 bg-black/20 p-4">
              <div className="space-y-1 text-gray-300 font-mono text-sm">
                {stackTrace.split("\n").map((line, index) => (
                  <div key={index} className="py-1 flex">
                    <span className="text-muted-foreground mr-4">{index + 1}.</span>
                    <span>{line}</span>
                  </div>
                ))}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </TabsContent>
    )
  }

  const RequestInfoTab = ({ requestInfo }: { requestInfo: RequestInfo }) => {
    return (
      <TabsContent value="request" className="animate-in fade-in-50 duration-300">
        <Card className="bg-gradient-to-br from-gray-900 to-gray-950 border-gray-800 shadow-xl">
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <Server className="h-5 w-5 text-blue-500" />
            <CardTitle className="text-lg font-medium">Informations de requête</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              <div>
                <h3 className="text-md font-medium mb-3 flex items-center gap-2">
                  <Badge variant="outline" className="rounded-md font-normal">
                    Détails de base
                  </Badge>
                </h3>
                <div className="border border-border/30 rounded-lg overflow-hidden bg-black/20">
                  <table className="w-full border-collapse">
                    <tbody>
                    <tr className="border-b border-border/30">
                      <td className="px-4 py-3 bg-muted/10 font-medium text-muted-foreground w-1/4">Méthode</td>
                      <td className="px-4 py-3">
                        <Badge
                          variant={requestInfo.method === "GET" ? "secondary" : "default"}
                          className="rounded-md"
                        >
                          {requestInfo.method}
                        </Badge>
                      </td>
                    </tr>
                    <tr className="border-b border-border/30">
                      <td className="px-4 py-3 bg-muted/10 font-medium text-muted-foreground">URL</td>
                      <td className="px-4 py-3 font-mono text-sm">
                        {`${requestInfo.serverName}:${requestInfo.port}${requestInfo.uri}`}
                      </td>
                    </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <div>
                <h3 className="text-md font-medium mb-3 flex items-center gap-2">
                  <Badge variant="outline" className="rounded-md font-normal">
                    Headers
                  </Badge>
                </h3>
                <div className="border border-border/30 rounded-lg overflow-hidden bg-black/20">
                  <table className="w-full border-collapse">
                    <tbody>
                    {Object.entries(requestInfo.headers).map(([key, value], index, arr) => (
                      <tr key={key} className={index < arr.length - 1 ? "border-b border-border/30" : ""}>
                        <td className="px-4 py-3 bg-muted/10 font-medium text-muted-foreground w-1/4">{key}</td>
                        <td className="px-4 py-3 break-all font-mono text-sm">{value}</td>
                      </tr>
                    ))}
                    </tbody>
                  </table>
                </div>
              </div>

              {requestInfo.body && (
                <div>
                  <h3 className="text-md font-medium mb-3 flex items-center gap-2">
                    <Badge variant="outline" className="rounded-md font-normal">
                      Request Body
                    </Badge>
                  </h3>
                  <CodeBlock code={JSON.stringify(error.requestInfo.body, null, 2)} fileName="request.json" />
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    )
  }

  const AppDetailsTab = ({ appDetails }: { appDetails: AppDetails }) => {
    return (
      <TabsContent value="app-details" className="animate-in fade-in-50 duration-300">
        <Card className="bg-gradient-to-br from-gray-900 to-gray-950 border-gray-800 shadow-xl">
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <Settings className="h-5 w-5 text-green-500" />
            <CardTitle className="text-lg font-medium">Détails de l'application</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="border border-border/30 rounded-lg overflow-hidden bg-black/20">
              <table className="w-full border-collapse">
                <tbody>
                <tr className="border-b border-border/30">
                  <td className="px-4 py-3 bg-muted/10 font-medium text-muted-foreground w-1/4">Version Java</td>
                  <td className="px-4 py-3">
                    <TooltipProvider>
                      <Tooltip>
                        <TooltipTrigger>
                          <Badge variant="outline" className="rounded-md font-mono">
                            {appDetails.javaVersion}
                          </Badge>
                        </TooltipTrigger>
                        <TooltipContent>
                          <p>Version de Java utilisée par l'application</p>
                        </TooltipContent>
                      </Tooltip>
                    </TooltipProvider>
                  </td>
                </tr>
                <tr className="border-b border-border/30">
                  <td className="px-4 py-3 bg-muted/10 font-medium text-muted-foreground">Version Jakarta EE</td>
                  <td className="px-4 py-3">
                    <TooltipProvider>
                      <Tooltip>
                        <TooltipTrigger>
                          <Badge variant="outline" className="rounded-md font-mono">
                            {appDetails.jakartaEEVersion}
                          </Badge>
                        </TooltipTrigger>
                        <TooltipContent>
                          <p>Version de Jakarta EE utilisée par l'application</p>
                        </TooltipContent>
                      </Tooltip>
                    </TooltipProvider>
                  </td>
                </tr>
                <tr>
                  <td className="px-4 py-3 bg-muted/10 font-medium text-muted-foreground">Version du Framework</td>
                  <td className="px-4 py-3">
                    <TooltipProvider>
                      <Tooltip>
                        <TooltipTrigger>
                          <Badge variant="outline" className="rounded-md font-mono">
                            {appDetails.matsdjavaframeworkVersion}
                          </Badge>
                        </TooltipTrigger>
                        <TooltipContent>
                          <p>Version du framework utilisée par l'application</p>
                        </TooltipContent>
                      </Tooltip>
                    </TooltipProvider>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    )
  }

  return (
    <ThemeProvider defaultTheme="dark">
      <div className="min-h-screen bg-gradient-to-br from-gray-950 via-gray-900/90 to-black px-4 py-10 relative overflow-x-hidden">
        {/* Decorative blurred background shapes */}
        <div className="pointer-events-none fixed inset-0 z-0">
          <div className="absolute top-[-10%] left-[-10%] w-[400px] h-[400px] bg-pink-500/10 rounded-full blur-3xl animate-spin-slow" />
          <div className="absolute bottom-[-10%] right-[-10%] w-[350px] h-[350px] bg-blue-500/10 rounded-full blur-3xl animate-pulse" />
        </div>
        <div className="max-w-6xl mx-auto relative z-10">
          <Header statusCodeReason={error.statusCodeReason} />
          <Exception exception={error.exception} />

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className="mt-10"
          >
            <Tabs defaultValue="sources" className="w-full">
              <TabsList className="bg-gradient-to-r from-gray-900/80 to-gray-950/90 border border-gray-800/80 rounded-xl overflow-hidden mb-8 p-1 w-full grid grid-cols-4 gap-1 shadow-lg backdrop-blur-md">
                <TabsTrigger
                  value="sources"
                  className="data-[state=active]:bg-muted/30 data-[state=active]:shadow-lg flex items-center gap-2 transition-all"
                >
                  <FileCode className="h-4 w-4" />
                  <span className="hidden sm:inline">Sources</span>
                </TabsTrigger>
                <TabsTrigger
                  value="stack-trace"
                  className="data-[state=active]:bg-muted/30 data-[state=active]:shadow-lg flex items-center gap-2 transition-all"
                >
                  <Layers className="h-4 w-4" />
                  <span className="hidden sm:inline">Piles d'appel</span>
                </TabsTrigger>
                <TabsTrigger
                  value="request"
                  className="data-[state=active]:bg-muted/30 data-[state=active]:shadow-lg flex items-center gap-2 transition-all"
                >
                  <Server className="h-4 w-4" />
                  <span className="hidden sm:inline">Requête</span>
                </TabsTrigger>
                <TabsTrigger
                  value="app-details"
                  className="data-[state=active]:bg-muted/30 data-[state=active]:shadow-lg flex items-center gap-2 transition-all"
                >
                  <Settings className="h-4 w-4" />
                  <span className="hidden sm:inline">Application</span>
                </TabsTrigger>
              </TabsList>

              {/* Fichiers Source Tab - Two Column Layout */}
              <SourcesTab exceptionFiles={error.exceptionFiles} />

              {/* Stack Trace Tab */}
              <StackTraceTab stackTrace={error.exception.stackTrace} />

              {/* Request Info Tab */}
              <RequestInfoTab requestInfo={error.requestInfo} />

              {/* App Info Tab */}
              <AppDetailsTab appDetails={error.appDetails} />
            </Tabs>
          </motion.div>
        </div>
        {/* Footer */}
        <footer className="w-full mt-16 text-center text-xs text-muted-foreground/70 z-10 relative">
          <span>
            Généré par <span className="font-semibold text-primary">matsd-javaframework</span> &mdash; {new Date().getFullYear()}
          </span>
        </footer>
      </div>
    </ThemeProvider>
  )
}

export default App
