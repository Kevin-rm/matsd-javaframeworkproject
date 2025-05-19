"use client"

import React, { useEffect, useRef, useState } from "react"
import type { AppDetails, Error, Exception, ExceptionFile, RequestInfo } from "./types.ts"
import { errorMockData } from "./data/mock.ts"
import { ThemeProvider } from "@/components/ThemeProvider.tsx"
import CodeBlock from "@/components/CodeBlock.tsx"
import { AlertCircle, ChevronRight, Code, CornerDownRight, FileCode, Info, Layers, Server, } from "lucide-react"
import { ScrollArea } from "@/components/ui/ScrollArea.tsx"
import { motion } from "framer-motion"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/Card.tsx";
import { Badge } from "@/components/ui/Badge.tsx";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/Tabs.tsx";
import { cn } from "@/lib/utils.ts";
import { Row, Table } from "@/components/Table.tsx";

const Header = ({ statusCodeReason }: { statusCodeReason: string }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: -20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="flex items-center gap-4 mb-6"
    >
      <AlertCircle className="h-10 w-10 text-red-500 animate-pulse"/>
      <div>
        <h1 className="text-3xl font-bold tracking-tight drop-shadow-sm">{statusCodeReason}</h1>
        <p className="text-muted-foreground mt-1 font-medium">
          Une erreur s'est produite lors du traitement de votre requête
        </p>
      </div>
    </motion.div>
  );
};

const Exception = ({ exception }: { exception: Exception }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay: 0.2 }}
    >
      <Card
        className="gap-4 bg-gradient-to-br from-gray-900/80 to-gray-950/90 border-gray-800/80 mt-6 overflow-hidden shadow-2xl backdrop-blur-md ring-1 ring-white/10">
        <CardHeader>
          <CardTitle className="font-semibold tracking-wide">
            <Badge variant="destructive" className="px-3 py-1 rounded-md text-sm shadow">
              {exception.className}
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="font-medium text-2xl drop-shadow">{exception.message}</p>
        </CardContent>
      </Card>
    </motion.div>
  );
};

const TabsContentCard = ({
  value,
  children,
  cardClassName = "",
  animate = true
}: {
  value: string;
  children: React.ReactNode;
  cardClassName?: string;
  animate?: boolean;
}) => {
  return (
    <TabsContent
      value={value}
      className={`${animate ? "animate-in fade-in-50 duration-300" : ""}`}
    >
      <Card className={cn(
        "bg-gradient-to-br from-gray-900 to-gray-950 border-gray-800 shadow-xl",
        cardClassName
      )}>
        {children}
      </Card>
    </TabsContent>
  );
};

const App = () => {
  const [error] = useState<Error>(() => (window as any).ERROR_DATA || errorMockData);

  const SourcesTabsContent = ({ exceptionFiles }: { exceptionFiles?: ExceptionFile[] }) => {
    const [selectedFileIndex, setSelectedFileIndex] = useState<number>(0);
    const buttonRefs = useRef<(HTMLButtonElement | null)[]>([]);

    useEffect(() => {
      if (buttonRefs.current && buttonRefs.current[selectedFileIndex] !== null)
        buttonRefs.current[selectedFileIndex].scrollIntoView({ block: "nearest" });
    }, [selectedFileIndex]);

    if (!exceptionFiles) return;

    return (
      <TabsContentCard value="sources" cardClassName="p-0">
        <CardContent className="p-0">
          <div className="grid grid-cols-12 min-h-[580px]">
            <div className="col-span-12 md:col-span-4 border-r border-gray-800 overflow-hidden">
              <div className="p-4 border-b border-gray-800 bg-muted/10 sticky top-0 z-10">
                <div className="flex items-center gap-2">
                  <FileCode className="h-5 w-5 text-muted-foreground"/>
                  <h3 className="font-medium text-sm text-muted-foreground">FICHIERS SOURCES</h3>
                </div>
              </div>
              <ScrollArea className="h-[calc(580px-57px)] hide-scrollbar">
                <div className="divide-y divide-gray-800/50">
                  {exceptionFiles.map((file, index) => {
                    const isSelected = selectedFileIndex === index;

                    return <button
                      ref={el => { buttonRefs.current[index] = el; }}
                      key={index}
                      onClick={() => setSelectedFileIndex(index)}
                      className={cn(
                        "w-full text-left px-4 py-3 hover:bg-gray-800/50 transition-colors cursor-pointer border-0",
                        isSelected ? "bg-gray-800/70 border-l-2 border-red-500" : ""
                      )}
                    >
                      <div className="flex items-start gap-3">
                        <Code
                          className={cn("h-5 w-5 mt-0.5", isSelected ? "text-red-400" : "text-muted-foreground")}
                        />
                        <div className="flex flex-col">
                          <span className="text-sm truncate">{file.fullPath}</span>
                          <span className="text-xs text-muted-foreground flex items-center gap-1 mt-1">
                            <CornerDownRight className="h-3 w-3"/>
                            {file.method}
                          </span>
                        </div>
                      </div>
                    </button>;
                  })}
                </div>
              </ScrollArea>
            </div>

            <div className="col-span-12 md:col-span-8 overflow-hidden">
              <div className="p-4 border-b border-gray-800 bg-muted/10 sticky top-0 z-10">
                <div className="flex items-center gap-2">
                  <div className="flex items-center gap-1 text-muted-foreground">
                    <span className="font-mono text-sm">{exceptionFiles[selectedFileIndex].fullPath}</span>
                    <ChevronRight className="h-4 w-4"/>
                    <span className="text-sm font-medium text-red-400">
                        Ligne {exceptionFiles[selectedFileIndex].highlightedLine}
                      </span>
                  </div>
                </div>
              </div>
              <CodeBlock
                className="h-[calc(580px-57px)]"
                code={exceptionFiles[selectedFileIndex].sourceCode}
                highlightedLine={exceptionFiles[selectedFileIndex].highlightedLine}
              />
            </div>
          </div>
        </CardContent>
      </TabsContentCard>
    );
  };

  const StackTraceTabsContent = ({ stackTrace }: { stackTrace: string }) => {
    return (
      <TabsContent value="stack-trace" className="animate-in fade-in-50 duration-300">
        <Card className="bg-gradient-to-br from-gray-900 to-gray-950 border-gray-800 shadow-xl">
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <Layers className="h-5 w-5 text-amber-500"/>
            <CardTitle className="text-xl font-medium">Piles d'appel</CardTitle>
          </CardHeader>
          <CardContent>
            <CodeBlock
              className="rounded-md border border-border/30 bg-black/20"
              maxHeight="400px"
              code={stackTrace}
              showLineNumbers={false}
            />
          </CardContent>
        </Card>
      </TabsContent>
    );
  };

  const RequestInfoTabsContent = ({ requestInfo }: { requestInfo: RequestInfo }) => {
    return (
      <TabsContent value="request" className="animate-in fade-in-50 duration-300">
        <Card className="bg-gradient-to-br from-gray-900 to-gray-950 border-gray-800 shadow-xl">
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <Server className="h-5 w-5 text-blue-500"/>
            <CardTitle className="text-xl font-medium">Informations de requête</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              <div>
                <h3 className="text-md font-medium mb-3 flex items-center gap-2">
                  <Badge variant="outline" className="rounded-md font-normal">
                    Détails de base
                  </Badge>
                </h3>
                <Table>
                  <Row label="Méthode" value={
                    <Badge
                      variant={requestInfo.method === "GET" ? "secondary" : "default"}
                      className="rounded-md"
                    >
                      {requestInfo.method}
                    </Badge>
                  }/>
                  <Row
                    label="URL"
                    value={<span className="font-mono text-sm">{requestInfo.url}</span>}
                    isLast
                  />
                </Table>
              </div>

              <div>
                <h3 className="text-md font-medium mb-3 flex items-center gap-2">
                  <Badge variant="outline" className="rounded-md font-normal">
                    En-têtes
                  </Badge>
                </h3>
                <Table>
                  {Object.entries(requestInfo.headers).map(([key, value], index, arr) => (
                    <Row
                      key={key}
                      label={key}
                      value={<span className="break-all font-mono text-sm">{value}</span>}
                      isLast={index === arr.length - 1}
                    />
                  ))}
                </Table>
              </div>

              {requestInfo.body && (
                <div>
                  <h3 className="text-md font-medium mb-3 flex items-center gap-2">
                    <Badge variant="outline" className="rounded-md font-normal">
                      Corps de la requête
                    </Badge>
                  </h3>
                  <CodeBlock
                    className="rounded-md border border-border/30 bg-black/20"
                    maxHeight="400px"
                    code={JSON.stringify(error.requestInfo.body, null, 2)}
                    language="json"
                    showLineNumbers={false}
                  />
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    );
  };

  const AppDetailsTabsContent = ({ appDetails }: { appDetails: AppDetails }) => {
    const InternalRow = ({ label, badgeText }: {
      label: string;
      badgeText: string;
    }) => {
      return (
        <Row label={label} value={
          <Badge variant="outline" className="rounded-md">
            {badgeText}
          </Badge>
        }/>
      );
    };

    return (
      <TabsContent value="app-details" className="animate-in fade-in-50 duration-300">
        <Card className="bg-gradient-to-br from-gray-900 to-gray-950 border-gray-800 shadow-xl">
          <CardHeader className="flex flex-row items-center gap-2 pb-2">
            <Info className="h-5 w-5 text-green-500"/>
            <CardTitle className="text-xl font-medium">Détails de l'application</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <InternalRow label="Version Java" badgeText={appDetails.javaVersion}/>
              <InternalRow label="Version Jakarta EE" badgeText={appDetails.jakartaEEVersion}/>
              <InternalRow label="Version matsd-javaframework" badgeText={appDetails.matsdjavaframeworkVersion}/>
            </Table>
          </CardContent>
        </Card>
      </TabsContent>
    );
  };

  return (
    <ThemeProvider defaultTheme="dark">
      <div
        className="min-h-screen bg-gradient-to-br from-gray-950 via-gray-900/90 to-black px-8 py-12 relative overflow-x-hidden">
        <div className="pointer-events-none fixed inset-0 z-0">
          <div
            className="absolute top-[-10%] left-[-10%] w-[400px] h-[400px] bg-pink-500/10 rounded-full blur-3xl animate-spin-slow"/>
          <div
            className="absolute bottom-[-10%] right-[-10%] w-[350px] h-[350px] bg-blue-500/10 rounded-full blur-3xl animate-pulse"/>
        </div>
        <div className="px-6 mx-auto relative">
          <Header statusCodeReason={error.statusCodeReason}/>
          <Exception exception={error.exception}/>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className="mt-10"
          >
            <Tabs defaultValue="sources">
              <TabsList
                className="bg-gradient-to-r from-gray-900/80 to-gray-950/90 border border-gray-800/80 overflow-hidden mb-2 grid grid-cols-4 shadow-lg backdrop-blur-md">
                <TabsTrigger value="sources">
                  <FileCode/>
                  <span className="hidden sm:inline">Sources</span>
                </TabsTrigger>
                <TabsTrigger value="stack-trace">
                  <Layers/>
                  <span className="hidden sm:inline">Piles d'appel</span>
                </TabsTrigger>
                <TabsTrigger value="request">
                  <Server/>
                  <span className="hidden sm:inline">Requête</span>
                </TabsTrigger>
                <TabsTrigger value="app-details">
                  <Info/>
                  <span className="hidden sm:inline">Application</span>
                </TabsTrigger>
              </TabsList>

              <SourcesTabsContent exceptionFiles={error.exceptionFiles}/>
              <StackTraceTabsContent stackTrace={error.exception.stackTrace}/>
              <RequestInfoTabsContent requestInfo={error.requestInfo}/>
              <AppDetailsTabsContent appDetails={error.appDetails}/>
            </Tabs>
          </motion.div>
        </div>
      </div>
    </ThemeProvider>
  );
};

export default App;
