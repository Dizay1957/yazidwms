export const currency = (value: number | string | undefined) =>
  new Intl.NumberFormat(undefined, { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(Number(value ?? 0));

export const number = (value: number | string | undefined) =>
  new Intl.NumberFormat(undefined, { maximumFractionDigits: 0 }).format(Number(value ?? 0));

export const dateTime = (value?: string) => (value ? new Date(value).toLocaleString() : "-");

export const labelize = (value?: string) =>
  value ? value.replaceAll("_", " ").toLowerCase().replace(/\b\w/g, (letter: string) => letter.toUpperCase()) : "-";
