import React from "react";

export const Table = ({ children, className = "" }: {
  children: React.ReactNode;
  className?: string;
}) => {
  return (
    <div className={`border border-border/30 rounded-lg overflow-hidden bg-black/20 ${className}`}>
      <table className="w-full border-collapse">
        <tbody>
        {children}
        </tbody>
      </table>
    </div>
  );
};

export const TableRow = ({ label, value, isLast = false, labelWidth = "w-1/4" }: {
  label: string;
  value: React.ReactNode;
  isLast?: boolean;
  labelWidth?: string;
}) => {
  return (
    <tr className={!isLast ? "border-b border-border/30" : ""}>
      <td className={`px-4 py-3 bg-muted/10 font-medium text-muted-foreground ${labelWidth}`}>
        {label}
      </td>
      <td className="px-4 py-3">
        {value}
      </td>
    </tr>
  );
};
