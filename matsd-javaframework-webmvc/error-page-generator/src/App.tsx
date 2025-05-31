"use client"

import React, { useState } from "react"
import type { AppDetails, Error, Exception, RequestInfo } from "./types.ts"
import { errorMockData } from "./data/mock.ts"
import { ThemeProvider } from "@/components/ThemeProvider.tsx"
import CodeBlock from "@/components/CodeBlock.tsx"
import { AlertCircle, Eye, FileText, Info, Layers, Server } from "lucide-react"
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

  const DefaultStyledRow = ({ label, value, isLast }: {
    label: string;
    value: string;
    isLast?: boolean;
  }) => {
    return <Row
      label={label}
      value={<span className="break-all font-mono text-sm">{value}</span>}
      isLast={isLast}
    />;
  };

  const StackTraceTabsContent = ({ stackTrace }: { stackTrace: string }) => {
    const [viewMode, setViewMode] = useState<"explorer" | "raw">("explorer");

    return (
      <TabsContentCard value="stack-trace">
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <div className="flex items-center gap-2">
            <Layers className="h-5 w-5 text-amber-500"/>
            <CardTitle className="text-xl font-medium">Piles d'appel</CardTitle>
          </div>

          <Tabs defaultValue={viewMode}>
            <TabsList
              className="bg-gradient-to-r from-gray-900/80 to-gray-950/90 border border-gray-800/80 overflow-hidden mb-2 grid grid-cols-2 shadow-lg backdrop-blur-md">
              <TabsTrigger value="explorer" onClick={() => setViewMode("explorer")}>
                <Eye/>
                <span>Explorateur</span>
              </TabsTrigger>
              <TabsTrigger value="raw" onClick={() => setViewMode("raw")}>
                <FileText/>
                <span>Format brut</span>
              </TabsTrigger>
            </TabsList>
          </Tabs>

        </CardHeader>
        <CardContent>
          <CodeBlock
            className="rounded-md border border-border/30 bg-black/20"
            maxHeight="400px"
            code={stackTrace}
          />
        </CardContent>
      </TabsContentCard>
    );
  };

  const RequestInfoTabsContent = ({ requestInfo }: { requestInfo: RequestInfo }) => {
    return (
      <TabsContentCard value="request">
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
                <DefaultStyledRow label="URL" value={requestInfo.url} isLast/>
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
                  <DefaultStyledRow
                    key={key}
                    label={key}
                    value={value}
                    isLast={index === arr.length - 1}
                  />
                ))}
              </Table>
            </div>

            <div>
              <h3 className="text-md font-medium mb-3 flex items-center gap-2">
                <Badge variant="outline" className="rounded-md font-normal">
                  Corps de la requête
                </Badge>
              </h3>
              {requestInfo.body ?
                <CodeBlock
                  className="rounded-md border border-border/30 bg-black/20"
                  maxHeight="400px"
                  code={JSON.stringify(error.requestInfo.body, null, 2)}
                  language="json"
                /> :
                <div className="p-4 rounded-md border border-border/30 bg-black/20">
                  <span className="text-sm font-mono">Pas de données</span>
                </div>}
            </div>
          </div>
        </CardContent>
      </TabsContentCard>
    );
  };

  const AppDetailsTabsContent = ({ appDetails }: { appDetails: AppDetails }) => {
    const InternalRow = ({ label, badgeText }: {
      label: string;
      badgeText: string;
    }) => {
      return <Row label={label} value={
        <Badge variant="outline" className="rounded-md">
          {badgeText}
        </Badge>
      }/>;
    };

    return (
      <TabsContentCard value="app-details">
        <CardHeader className="flex flex-row items-center gap-2 pb-2">
          <Info className="h-5 w-5 text-green-500"/>
          <CardTitle className="text-xl font-medium">Détails de l'application</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <InternalRow label="Version Java" badgeText={appDetails.javaVersion}/>
            <InternalRow label="Version matsd-javaframework" badgeText={appDetails.matsdjavaframeworkVersion}/>
            <DefaultStyledRow label="Serveur d'application" value={appDetails.serverInfo}/>
            <DefaultStyledRow label="Chemin du contexte" value={appDetails.contextPath}/>
          </Table>
        </CardContent>
      </TabsContentCard>
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
            <Tabs defaultValue="stack-trace">
              <TabsList
                className="bg-gradient-to-r from-gray-900/80 to-gray-950/90 border border-gray-800/80 overflow-hidden mb-2 grid grid-cols-3 shadow-lg backdrop-blur-md">
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

              <StackTraceTabsContent  stackTrace={error.exception.stackTrace}/>
              <RequestInfoTabsContent requestInfo={error.requestInfo}/>
              <AppDetailsTabsContent  appDetails={error.appDetails}/>
            </Tabs>
          </motion.div>
        </div>
      </div>
    </ThemeProvider>
  );
};

export default App;
