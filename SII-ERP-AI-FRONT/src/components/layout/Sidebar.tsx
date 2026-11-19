'use client';

import { useEffect, useMemo, useState, type ElementType } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
    Boxes,
    Calculator,
    ChevronDown,
    ChevronLeft,
    ChevronRight,
    Cloud,
    FileBarChart,
    LayoutDashboard,
    PackageSearch,
    Receipt,
    Settings,
    ShieldCheck,
    ShoppingCart,
    Users,
    Wallet,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { APP_ROUTES, NAV_GROUPS, type NavIconName, type NavItem } from '@/config/routes';
import { useAuthStore } from '@/features/auth/stores/auth.store';
import { useUiStore } from '@/stores/ui.store';
import { Button } from '@/components/ui/button';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';

const GROUP_ICONS: Record<NavIconName, ElementType> = {
    dashboard: LayoutDashboard,
    sales: Receipt,
    purchases: ShoppingCart,
    tax: Cloud,
    accounting: Calculator,
    treasury: Wallet,
    people: Users,
    operations: Boxes,
    audit: ShieldCheck,
    documents: FileBarChart,
    modules: PackageSearch,
    settings: Settings,
};

const ALL_HREFS = NAV_GROUPS.flatMap((group) => group.items.map((item) => item.href));

/**
 * Marca el ítem activo por coincidencia exacta o por prefijo de ruta.
 * Si existe un ítem más específico en el menú, el padre deja de estar activo
 * (por ejemplo `/banking` no se marca en `/banking/reconciliation`).
 */
function isItemActive(href: string, pathname: string): boolean {
    if (pathname === href) return true;
    if (!pathname.startsWith(`${href}/`)) return false;

    return !ALL_HREFS.some(
        (other) =>
            other !== href &&
            other.startsWith(`${href}/`) &&
            (pathname === other || pathname.startsWith(`${other}/`))
    );
}

interface SidebarProps {
    className?: string;
}

export function Sidebar({ className }: SidebarProps) {
    const pathname = usePathname();
    const { isSidebarCollapsed, toggleSidebar } = useUiStore();
    const role = useAuthStore((state) => state.user?.role);
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

    const [mounted, setMounted] = useState(false);
    const [closedGroups, setClosedGroups] = useState<Record<string, boolean>>({});
    const [openGroup, setOpenGroup] = useState<string | null>(null);

    useEffect(() => setMounted(true), []);

    useEffect(() => {
        setOpenGroup(null);
    }, [pathname]);

    const visibleGroups = useMemo(() => {
        if (!role) return [];

        return NAV_GROUPS
            .map((group) => ({
                ...group,
                items: group.items.filter((item) => item.roles.includes(role)),
            }))
            .filter((group) => group.items.length > 0);
    }, [role]);

    const toggleGroup = (label: string) => {
        setClosedGroups((current) => ({ ...current, [label]: !current[label] }));
    };

    const renderItemLink = (item: NavItem) => {
        const active = isItemActive(item.href, pathname);

        return (
            <Link
                key={item.href}
                href={item.href}
                aria-current={active ? 'page' : undefined}
                onClick={() => setOpenGroup(null)}
                className={cn(
                    'flex items-center rounded-md px-2 py-1.5 text-sm transition-colors',
                    active
                        ? 'bg-primary font-medium text-primary-foreground'
                        : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                )}
            >
                <span className="truncate">{item.label}</span>
            </Link>
        );
    };

    return (
        <aside
            className={cn(
                'fixed left-0 top-0 z-40 h-screen border-r bg-card transition-all duration-300',
                isSidebarCollapsed ? 'w-16' : 'w-64',
                className
            )}
        >
            <div className="flex h-full flex-col">
                {/* Logo */}
                <div className={cn('flex h-16 items-center border-b px-4', isSidebarCollapsed ? 'justify-center' : 'justify-between')}>
                    <Link href={APP_ROUTES.PROTECTED.DASHBOARD} className="flex items-center space-x-2 overflow-hidden">
                        <Calculator className="h-6 w-6 shrink-0" />
                        {!isSidebarCollapsed && <span className="text-lg font-bold whitespace-nowrap">SII-ERP-AI</span>}
                    </Link>
                </div>

                {/* Navigation */}
                <nav aria-label="Menú principal" className="flex-1 space-y-1 overflow-y-auto px-2 py-4">
                    {!mounted ? (
                        <div aria-hidden="true" className="space-y-2 px-2 py-2">
                            {Array.from({ length: 8 }).map((_, index) => (
                                <div key={index} className="h-8 animate-pulse rounded-md bg-muted" />
                            ))}
                        </div>
                    ) : visibleGroups.length === 0 ? (
                        <p className="px-2 py-4 text-sm text-muted-foreground">
                            {isAuthenticated
                                ? 'Tu rol todavía no tiene módulos asignados.'
                                : 'Inicia sesión para ver tu menú.'}
                        </p>
                    ) : isSidebarCollapsed ? (
                        <div className="space-y-1">
                            {visibleGroups.map((group) => {
                                const Icon = GROUP_ICONS[group.icon];
                                const groupActive = group.items.some((item) => isItemActive(item.href, pathname));
                                const baseClass = cn(
                                    'mx-auto flex h-10 w-10 items-center justify-center rounded-md transition-colors',
                                    groupActive
                                        ? 'bg-primary-subtle text-primary-subtle-foreground'
                                        : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                                );

                                if (group.items.length === 1) {
                                    const item = group.items[0];
                                    return (
                                        <Link
                                            key={group.label}
                                            href={item.href}
                                            title={group.label}
                                            aria-label={group.label}
                                            aria-current={groupActive ? 'page' : undefined}
                                            className={baseClass}
                                        >
                                            <Icon className="h-5 w-5" />
                                        </Link>
                                    );
                                }

                                return (
                                    <Popover
                                        key={group.label}
                                        open={openGroup === group.label}
                                        onOpenChange={(open) => setOpenGroup(open ? group.label : null)}
                                    >
                                        <PopoverTrigger asChild>
                                            <button
                                                type="button"
                                                aria-label={group.label}
                                                title={group.label}
                                                className={baseClass}
                                                onMouseEnter={() => setOpenGroup(group.label)}
                                            >
                                                <Icon className="h-5 w-5" />
                                            </button>
                                        </PopoverTrigger>
                                        <PopoverContent
                                            side="right"
                                            align="start"
                                            sideOffset={8}
                                            className="w-64 p-2"
                                            onMouseLeave={() => setOpenGroup(null)}
                                        >
                                            <p className="px-2 pb-1 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                                {group.label}
                                            </p>
                                            <div className="space-y-0.5">
                                                {group.items.map(renderItemLink)}
                                            </div>
                                        </PopoverContent>
                                    </Popover>
                                );
                            })}
                        </div>
                    ) : (
                        <div className="space-y-2">
                            {visibleGroups.map((group, index) => {
                                const Icon = GROUP_ICONS[group.icon];
                                const panelId = `nav-group-${index}`;
                                const isOpen = !closedGroups[group.label];

                                return (
                                    <div key={group.label}>
                                        <button
                                            type="button"
                                            onClick={() => toggleGroup(group.label)}
                                            aria-expanded={isOpen}
                                            aria-controls={panelId}
                                            className="flex w-full items-center gap-2 rounded-md px-2 py-2 text-left text-xs font-semibold uppercase tracking-wide text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
                                        >
                                            <Icon className="h-4 w-4 shrink-0" />
                                            <span className="flex-1 truncate">{group.label}</span>
                                            <ChevronDown
                                                className={cn(
                                                    'h-3.5 w-3.5 shrink-0 transition-transform',
                                                    !isOpen && '-rotate-90'
                                                )}
                                            />
                                        </button>
                                        <div id={panelId} hidden={!isOpen} className="mt-0.5 space-y-0.5 pl-3">
                                            {group.items.map(renderItemLink)}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </nav>

                {/* Collapse Toggle */}
                <div className="flex justify-end border-t p-2">
                    <Button
                        variant="ghost"
                        size="icon"
                        onClick={toggleSidebar}
                        aria-label={isSidebarCollapsed ? 'Expandir menú' : 'Contraer menú'}
                        title={isSidebarCollapsed ? 'Expandir menú' : 'Contraer menú'}
                        className="ml-auto"
                    >
                        {isSidebarCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
                    </Button>
                </div>
            </div>
        </aside>
    );
}
