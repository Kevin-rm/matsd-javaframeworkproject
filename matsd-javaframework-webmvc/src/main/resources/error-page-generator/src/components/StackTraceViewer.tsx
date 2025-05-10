import React from 'react';
import type { ErrorTraceItem } from '../types/ErrorTypes';

interface StackTraceViewerProps {
  trace: ErrorTraceItem[];
  onSelectTraceItem: (index: number) => void;
  selectedTraceIndex: number;
}

const StackTraceViewer: React.FC<StackTraceViewerProps> = ({ 
  trace, 
  onSelectTraceItem,
  selectedTraceIndex
}) => {
  return (
    <div className="bg-slate-800 rounded-lg overflow-hidden border border-slate-700 shadow-md">
      <div className="bg-slate-900 px-4 py-2 border-b border-slate-700">
        <h3 className="text-lg font-semibold text-white flex items-center">
          <div className="w-1 h-5 bg-blue-500 rounded mr-2"></div>
          Pile d'appels
        </h3>
      </div>
      
      <div className="max-h-96 overflow-y-auto">
        {trace.map((item, index) => (
          <div
            key={index}
            onClick={() => onSelectTraceItem(index)}
            className={`
              border-b border-slate-700 px-4 py-3 cursor-pointer transition
              ${selectedTraceIndex === index ? 'bg-blue-900/30' : 'hover:bg-slate-700/30'}
              ${index === 0 ? 'border-l-4 border-l-red-500' : ''}
            `}
          >
            <div className="font-mono text-sm mb-1 text-white flex justify-between">
              <span className="mr-2 overflow-hidden text-ellipsis">{item.method}</span>
              <span className="text-slate-400">ligne {item.line}</span>
            </div>
            <div className="font-mono text-xs text-blue-300 truncate">{item.file}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default StackTraceViewer;
