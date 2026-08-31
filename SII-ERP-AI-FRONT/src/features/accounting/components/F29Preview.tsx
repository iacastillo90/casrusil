export const F29Preview = ({ report }: { report?: any }) => {
    return (
        <div className="p-4 border rounded-lg bg-white mt-4">
            <h3 className="font-semibold mb-2">Vista Previa F29</h3>
            <p className="text-sm text-slate-500">
                {report ? "Borrador de Declaración..." : "Sin datos de declaración."}
            </p>
        </div>
    );
};
