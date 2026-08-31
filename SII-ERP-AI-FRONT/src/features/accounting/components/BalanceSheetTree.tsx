export const BalanceSheetTree = ({ data }: { data?: any }) => {
    return (
        <div className="p-4 border rounded-lg bg-white">
            <h3 className="font-semibold mb-2">Árbol de Cuentas (Balance)</h3>
            <p className="text-sm text-slate-500">
                {data ? "Cargando estructura..." : "Sin datos para mostrar."}
            </p>
        </div>
    );
};
