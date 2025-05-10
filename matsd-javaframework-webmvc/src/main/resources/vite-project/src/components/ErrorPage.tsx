import React, { useState } from 'react';
import ErrorHeader from './ErrorHeader';
import ErrorDetails from './ErrorDetails';
import StackTraceViewer from './StackTraceViewer';
import FileViewer from './FileViewer';
import type { ErrorData } from '../types/ErrorTypes';

interface ErrorPageProps {
  errorData: ErrorData;
}

const ErrorPage: React.FC<ErrorPageProps> = ({ errorData }) => {
  const [selectedTraceIndex, setSelectedTraceIndex] = useState(0);
  
  // Find the file that corresponds to the selected trace item
  const selectedFile = (() => {
    const traceItem = errorData.trace[selectedTraceIndex];
    if (traceItem.fileIndex !== undefined) {
      return errorData.files[traceItem.fileIndex];
    }
    return null;
  })();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800 gradient-animate text-white">
      <ErrorHeader type={errorData.type} message={errorData.message} />
      
      <div className="container mx-auto px-4 py-8">
        <ErrorDetails 
          type={errorData.type} 
          message={errorData.message} 
          statusCode={errorData.statusCode} 
        />
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <StackTraceViewer 
            trace={errorData.trace}
            onSelectTraceItem={setSelectedTraceIndex}
            selectedTraceIndex={selectedTraceIndex}
          />
          
          <div>
            <h3 className="text-lg font-semibold text-white mb-3 flex items-center">
              <div className="w-1 h-5 bg-blue-500 rounded mr-2"></div>
              Code source
            </h3>
            
            {selectedFile ? (
              <FileViewer file={selectedFile} />
            ) : (
              <div className="bg-slate-800 rounded-lg p-4 border border-slate-700 text-slate-400">
                Fichier source non disponible
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ErrorPage;
