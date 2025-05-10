import React from 'react';

interface ErrorHeaderProps {
  type: string;
  message: string;
}

const ErrorHeader: React.FC<ErrorHeaderProps> = ({ type, message }) => {
  return (
    <div className="bg-gradient-to-r from-red-600 to-red-800 p-6 shadow-lg">
      <h1 className="text-2xl font-bold text-white mb-2">Une erreur s'est produite</h1>
      <p className="text-red-100 font-medium text-lg">{type}</p>
      <p className="text-white/80 mt-2">{message}</p>
    </div>
  );
};

export default ErrorHeader;
