"use client";

import React from "react";
import { cn } from "@/lib/utils.ts";

export const Table = ({ children, className = "" }: {
  children: React.ReactElement<RowProps> | React.ReactElement<RowProps>[];
  className?: string;
}) => {
  return (
    <div className={cn("border border-border/30 rounded-lg overflow-hidden bg-black/20", className)}>
      <table className="w-full border-collapse">
        <tbody>
        {children}
        </tbody>
      </table>
    </div>
  );
};

type RowProps = {
  label: string;
  value: React.ReactNode;
  isLast?: boolean;
};

export const Row = ({ label, value, isLast = false }: RowProps) => {
  return (
    <tr className={!isLast ? "border-b border-border/30" : ""}>
      <td className="px-4 py-3 bg-muted/10 text-muted-foreground w-1/5">
        {label}
      </td>
      <td className="px-4 py-3">
        {value}
      </td>
    </tr>
  );
};
