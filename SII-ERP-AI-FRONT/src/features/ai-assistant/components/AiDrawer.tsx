"use client";

import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { AiChat } from "./AiChat";
import { Button } from "@/components/ui/button";
import { Bot } from "lucide-react";

export const AiDrawer = ({ children }: { children?: React.ReactNode }) => {
    return (
        <Sheet>
            <SheetTrigger asChild>
                {children || <Button variant="outline" size="icon"><Bot className="h-5 w-5" /></Button>}
            </SheetTrigger>
            <SheetContent className="w-[400px] sm:w-[540px]">
                <AiChat />
            </SheetContent>
        </Sheet>
    );
};
