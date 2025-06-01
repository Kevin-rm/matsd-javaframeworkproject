"use client";

import hljs from "highlight.js/lib/core";
import java from "highlight.js/lib/languages/java";
import json from "highlight.js/lib/languages/json";
import React, { useState } from "react";
import "highlight.js/styles/atom-one-dark.min.css";
import { Check, Copy } from "lucide-react";
import { ScrollArea, ScrollBar } from "@/components/ui/ScrollArea.tsx";
import { cn } from "@/lib/utils.ts";

hljs.registerLanguage("java", java);
hljs.registerLanguage("json", json);

const CodeBlock = ({
  className = "",
  maxHeight,
  code,
  language = "java"
}: {
  className?: string;
  maxHeight?: string;
  code: string;
  language?: "java" | "json";
}) => {
  const [copied, setCopied] = useState(false);
  const [hoveredLine, setHoveredLine] = useState<number | null>(null);
  const codeLines = code.trim().split("\n");

  const copyToClipboard = async () => {
    await navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <ScrollArea className={className} style={maxHeight ? { maxHeight } : undefined}>
      <button
        className="absolute top-3 right-4 z-10 p-1.5 rounded-sm bg-background/80 backdrop-blur-sm shadow ring-1 ring-white/10 hover:ring-white/30 transition-all"
        onClick={copyToClipboard}
        aria-label="Copier le code"
      >
        {copied ? <Check className="h-4 w-4 text-green-400"/> : <Copy className="h-4 w-4"/>}
      </button>
      <pre className="text-sm p-3 leading-relaxed">
          {codeLines.map((line, index) => {
            const lineNumber = index + 1;

            return (
              <div key={index} className="grid group">
                <code
                  className={cn(
                    "pl-3", `language-${language}`, hoveredLine === lineNumber && "bg-white/10"
                  )}
                  dangerouslySetInnerHTML={{ __html: hljs.highlight(line, { language }).value }}
                  onMouseEnter={() => setHoveredLine(lineNumber)}
                  onMouseLeave={() => setHoveredLine(null)}
                />
              </div>
            );
          })}
        </pre>
      <ScrollBar orientation="horizontal"/>
    </ScrollArea>
  );
};

export default React.memo(CodeBlock);
