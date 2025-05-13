import hljs from "highlight.js/lib/core";
import java from "highlight.js/lib/languages/java";
import json from "highlight.js/lib/languages/json"
import { useEffect, useRef } from "react";
import "highlight.js/styles/atom-one-dark.min.css";

hljs.registerLanguage("java", java);
hljs.registerLanguage("json", json);

const CodeBlock = ({ code }: {
  code: string; highlightedLine?: number
}) => {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    if (ref.current) hljs.highlightElement(ref.current);
  }, [code]);

  return (
    <div className="max-h-[500px]">
      <pre className="p-0 w-full">
        <code ref={ref} className="language-java block">
          {code}
        </code>
      </pre>
    </div>
  );
};

export default CodeBlock;
