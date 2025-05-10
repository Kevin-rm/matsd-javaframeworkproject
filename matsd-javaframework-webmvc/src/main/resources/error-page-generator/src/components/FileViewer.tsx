import React from 'react';
import type { ErrorFile } from '../types';

interface FileViewerProps {
  file: ErrorFile;
}

const FileViewer: React.FC<FileViewerProps> = ({ file }) => {
  const lines = file.content.split('\n');
  const highlightLine = file.highlight?.line;
  
  return (
    <div className="bg-slate-800 rounded-lg overflow-hidden border border-slate-700 shadow-md">
      <div className="bg-slate-900 px-4 py-2 border-b border-slate-700 flex items-center justify-between">
        <h4 className="font-mono text-sm text-white truncate">{file.path}</h4>
        <div className="flex space-x-2">
          <button className="text-xs px-2 py-1 bg-blue-600 rounded hover:bg-blue-700 transition text-white">
            Copier
          </button>
        </div>
      </div>
      
      <div className="overflow-x-auto">
        <pre className="p-0 m-0">
          <code className="block p-4 text-sm font-mono leading-6">
            {lines.map((line, index) => {
              const lineNumber = index + 1;
              const isHighlighted = highlightLine === lineNumber;
              
              return (
                <div 
                  key={lineNumber}
                  className={`flex ${isHighlighted ? 'error-line' : ''}`}
                >
                  <div className="w-12 text-right select-none pr-3 text-slate-500 border-r border-slate-700 mr-3">
                    {lineNumber}
                  </div>
                  <div className={`flex-1 ${isHighlighted ? 'text-red-200' : 'text-slate-300'}`}>
                    {line || ' '}
                  </div>
                </div>
              );
            })}
          </code>
        </pre>
      </div>
    </div>
  );
};

export default FileViewer;
