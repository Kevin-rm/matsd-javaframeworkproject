import React from 'react';

interface ErrorDetailsProps {
  type: string;
  message: string;
  statusCode: number;
}

const ErrorDetails: React.FC<ErrorDetailsProps> = ({ type, message, statusCode }) => {
  return (
    <div className="bg-slate-800 rounded-lg p-4 border border-slate-700 shadow-md mb-6">
      <h3 className="text-lg font-semibold text-white mb-3 flex items-center">
        <div className="w-1 h-5 bg-blue-500 rounded mr-2"></div>
        Détails de l'erreur
      </h3>
      <div className="divide-y divide-slate-700">
        <div className="flex flex-col md:flex-row py-3">
          <span className="text-slate-400 md:w-48 font-medium">Type d'exception</span>
          <span className="text-white font-mono">{type}</span>
        </div>
        <div className="flex flex-col md:flex-row py-3">
          <span className="text-slate-400 md:w-48 font-medium">Message</span>
          <span className="text-white break-words">{message}</span>
        </div>
        <div className="flex flex-col md:flex-row py-3">
          <span className="text-slate-400 md:w-48 font-medium">Code d'état HTTP</span>
          <span className="text-white">{statusCode}</span>
        </div>
      </div>
    </div>
  );
};

export default ErrorDetails;
