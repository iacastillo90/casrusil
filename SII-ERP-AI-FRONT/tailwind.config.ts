import type { Config } from "tailwindcss";

const config: Config = {
    darkMode: ["class"],
    content: [
        "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
        "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
        "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
        "./src/features/**/*.{js,ts,jsx,tsx,mdx}",
    ],
    theme: {
        extend: {
            colors: {
                background: 'hsl(var(--background))',
                surface: 'hsl(var(--surface))',
                foreground: 'hsl(var(--foreground))',
                overlay: 'var(--overlay)',
                card: {
                    DEFAULT: 'hsl(var(--card))',
                    foreground: 'hsl(var(--card-foreground))'
                },
                popover: {
                    DEFAULT: 'hsl(var(--popover))',
                    foreground: 'hsl(var(--popover-foreground))'
                },
                primary: {
                    DEFAULT: 'hsl(var(--primary))',
                    hover: 'hsl(var(--primary-hover))',
                    foreground: 'hsl(var(--primary-foreground))',
                    subtle: 'hsl(var(--primary-subtle))',
                    'subtle-foreground': 'hsl(var(--primary-subtle-foreground))'
                },
                secondary: {
                    DEFAULT: 'hsl(var(--secondary))',
                    foreground: 'hsl(var(--secondary-foreground))'
                },
                muted: {
                    DEFAULT: 'hsl(var(--muted))',
                    foreground: 'hsl(var(--muted-foreground))'
                },
                accent: {
                    DEFAULT: 'hsl(var(--accent))',
                    foreground: 'hsl(var(--accent-foreground))'
                },
                destructive: {
                    DEFAULT: 'hsl(var(--destructive))',
                    foreground: 'hsl(var(--destructive-foreground))',
                    solid: 'hsl(var(--destructive-solid))',
                    'solid-foreground': 'hsl(var(--destructive-solid-foreground))',
                    subtle: 'hsl(var(--destructive-subtle))',
                    'subtle-foreground': 'hsl(var(--destructive-subtle-foreground))'
                },
                success: {
                    DEFAULT: 'hsl(var(--success))',
                    solid: 'hsl(var(--success-solid))',
                    'solid-foreground': 'hsl(var(--success-solid-foreground))',
                    subtle: 'hsl(var(--success-subtle))',
                    'subtle-foreground': 'hsl(var(--success-subtle-foreground))'
                },
                warning: {
                    DEFAULT: 'hsl(var(--warning))',
                    solid: 'hsl(var(--warning-solid))',
                    'solid-foreground': 'hsl(var(--warning-solid-foreground))',
                    subtle: 'hsl(var(--warning-subtle))',
                    'subtle-foreground': 'hsl(var(--warning-subtle-foreground))'
                },
                info: {
                    DEFAULT: 'hsl(var(--info))',
                    solid: 'hsl(var(--info-solid))',
                    'solid-foreground': 'hsl(var(--info-solid-foreground))',
                    subtle: 'hsl(var(--info-subtle))',
                    'subtle-foreground': 'hsl(var(--info-subtle-foreground))'
                },
                signed: {
                    DEFAULT: 'hsl(var(--signed))',
                    subtle: 'hsl(var(--signed-subtle))',
                    'subtle-foreground': 'hsl(var(--signed-subtle-foreground))'
                },
                neutral: {
                    subtle: 'hsl(var(--neutral-subtle))',
                    'subtle-foreground': 'hsl(var(--neutral-subtle-foreground))'
                },
                dte: {
                    frame: 'hsl(var(--dte-frame))'
                },
                border: 'hsl(var(--border))',
                input: 'hsl(var(--input))',
                ring: 'hsl(var(--ring))',
                chart: {
                    '1': 'hsl(var(--chart-1))',
                    '2': 'hsl(var(--chart-2))',
                    '3': 'hsl(var(--chart-3))',
                    '4': 'hsl(var(--chart-4))',
                    '5': 'hsl(var(--chart-5))'
                }
            },
            borderRadius: {
                lg: 'var(--radius)',
                md: 'calc(var(--radius) - 2px)',
                sm: 'calc(var(--radius) - 4px)'
            }
        }
    },
    plugins: [require("tailwindcss-animate")],
};
export default config;
