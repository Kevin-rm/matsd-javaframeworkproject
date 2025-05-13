import hljs from "highlight.js/lib/core";
import java from "highlight.js/lib/languages/java";
import { useEffect, useRef } from "react";
import "highlight.js/styles/atom-one-dark.min.css";

hljs.registerLanguage("java", java);

const CodeBlock = ({ code }: { code: string }) => {
  const ref = useRef<HTMLElement>(null);

  useEffect(() => {
    if (ref.current) hljs.highlightElement(ref.current);
  }, [code]);

  return (
    <pre>
      <code ref={ref} className="language-java">
        {code}
      </code>
    </pre>
  );
};

export default CodeBlock;
