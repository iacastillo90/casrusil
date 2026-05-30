"use client";

import { PDFDownloadLink } from "@react-pdf/renderer";
import React from "react";

interface PDFDownloadButtonProps {
    document: React.ReactElement<any>;
    fileName: string;
    className?: string;
    children: React.ReactNode | ((props: { loading: boolean }) => React.ReactNode);
}

export default function PDFDownloadButton({ document, fileName, className, children }: PDFDownloadButtonProps) {
    return (
        <PDFDownloadLink document={document} fileName={fileName} className={className}>
            {/* @ts-ignore */}
            {children}
        </PDFDownloadLink>
    );
}
