import hljs from "highlight.js/lib/core";
import java from "highlight.js/lib/languages/java";
import json from "highlight.js/lib/languages/json";
import React, { useEffect, useRef, useState } from "react";
import "highlight.js/styles/atom-one-dark.min.css";
import { Check, Copy } from "lucide-react";
import { ScrollArea, ScrollBar } from "@/components/ui/ScrollArea.tsx";

hljs.registerLanguage("java", java);
hljs.registerLanguage("json", json);

const CodeBlock = ({ className = "", code, language = "java" }: {
  className?: string;
  code: string;
  language?: "java" | "json";
  highlightedLine?: number;
}) => {
  const ref = useRef<HTMLElement>(null);
  const [copied, setCopied] = useState<boolean>(false);

  useEffect(() => {
    if (ref.current) hljs.highlightElement(ref.current);
  }, [code, language]);

  const copyToClipboard = async () => {
    await navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <ScrollArea className={className}>
      <pre>
        <code ref={ref} className={`text-sm language-${language} block`}>
          {code}
        </code>
      </pre>
      <div className="absolute top-3 right-3 z-10">
        <button
          className="p-1.5 rounded-sm bg-background/80 backdrop-blur-sm shadow ring-1 ring-white/10 hover:ring-white/30 transition-all"
          onClick={copyToClipboard}
          aria-label="Copier le code"
        >
          {copied ? <Check className="h-4 w-4 text-green-400" /> : <Copy className="h-4 w-4" />}
        </button>
      </div>
      <ScrollBar orientation="horizontal"></ScrollBar>
    </ScrollArea>
  );
};

export default React.memo(CodeBlock);
