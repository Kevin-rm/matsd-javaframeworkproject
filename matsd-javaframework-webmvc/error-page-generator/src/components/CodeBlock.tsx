import hljs from "highlight.js/lib/core";
import java from "highlight.js/lib/languages/java";
import { useEffect, useRef, useState } from "react";
import "highlight.js/styles/atom-one-dark.min.css";
import { Button } from "@/components/ui/button";
import { Check, Copy } from 'lucide-react';
import { cn } from "@/lib/utils";

hljs.registerLanguage("java", java);

interface CodeBlockProps {
  code: string;
  highlightedLine?: number;
  fileName?: string;
}

const CodeBlock = ({ code, highlightedLine, fileName }: CodeBlockProps) => {
  const ref = useRef<HTMLElement>(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (ref.current) hljs.highlightElement(ref.current);
  }, [code]);

  const copyToClipboard = async () => {
    await navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const lines = code.split("\n");

  return (
    <div className="relative group rounded-xl overflow-hidden border border-border/30 bg-black/30 backdrop-blur-md shadow-xl ring-1 ring-white/10">
      {fileName && (
        <div className="flex items-center justify-between px-4 py-2 bg-muted/40 border-b border-border/30 backdrop-blur-md">
          <span className="text-xs font-mono text-muted-foreground">{fileName}</span>
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8 opacity-80 hover:opacity-100 transition-opacity hover:bg-primary/20"
            onClick={copyToClipboard}
            aria-label="Copier le code"
          >
            {copied ? <Check className="h-4 w-4 text-green-400" /> : <Copy className="h-4 w-4" />}
          </Button>
        </div>
      )}
      <div className="relative overflow-auto max-h-[500px] scrollbar-thin scrollbar-thumb-muted-foreground/20 scrollbar-track-transparent">
        <div className="flex">
          <div className="text-right py-4 pr-2 select-none bg-muted/10 border-r border-border/30">
            {lines.map((_, i) => (
              <div
                key={i}
                className={cn(
                  "px-3 text-xs font-mono text-muted-foreground/60 transition-all duration-200",
                  highlightedLine === i + 1 && "bg-red-500/20 text-red-400 font-bold shadow-inner rounded"
                )}
              >
                {i + 1}
              </div>
            ))}
          </div>
          <pre className="p-0 m-0 w-full overflow-visible">
            <code ref={ref} className="language-java p-4 block">
              {code}
            </code>
          </pre>
        </div>
      </div>
      {!fileName && (
        <Button
          variant="ghost"
          size="icon"
          className="absolute top-3 right-3 opacity-80 hover:opacity-100 transition-opacity bg-background/80 backdrop-blur-sm shadow"
          onClick={copyToClipboard}
          aria-label="Copier le code"
        >
          {copied ? <Check className="h-4 w-4 text-green-400" /> : <Copy className="h-4 w-4" />}
        </Button>
      )}
    </div>
  );
};

export default CodeBlock;
