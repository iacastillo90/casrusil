export interface EconomicIndicators {
    uf: number;
    utm: number;
    usd: number;
}
export const indicatorsService = {
    getIndicators: async (): Promise<EconomicIndicators> => ({ uf: 0, utm: 0, usd: 0 })
};
