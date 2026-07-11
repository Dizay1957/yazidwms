import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField, MenuItem } from "@mui/material";
import { FormEvent, useEffect, useState } from "react";

export type FieldOption = { value: string | number; label: string };
export type FormField = {
  name: string;
  label: string;
  type?: "text" | "number" | "email" | "password" | "select" | "multiselect";
  required?: boolean;
  multiline?: boolean;
  options?: FieldOption[];
};

export type FormValues = Record<string, string | number | string[] | boolean | null | undefined>;

export function FormDialog({
  open,
  title,
  fields,
  initialValues,
  loading,
  submitLabel = "Save",
  onClose,
  onSubmit
}: {
  open: boolean;
  title: string;
  fields: FormField[];
  initialValues: FormValues;
  loading?: boolean;
  submitLabel?: string;
  onClose: () => void;
  onSubmit: (values: FormValues) => void;
}) {
  const [values, setValues] = useState<FormValues>(initialValues);
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    setValues(initialValues);
    setSubmitted(false);
  }, [initialValues, open]);

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    setSubmitted(true);
    const invalid = fields.some((field) => field.required && (values[field.name] === undefined || values[field.name] === "" || values[field.name] === null));
    if (!invalid) {
      onSubmit(values);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit}>
        <DialogTitle>{title}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {fields.map((field) => (
              <TextField
                key={field.name}
                select={field.type === "select" || field.type === "multiselect"}
                SelectProps={{ multiple: field.type === "multiselect" }}
                type={field.type === "number" ? "number" : field.type === "password" ? "password" : field.type === "email" ? "email" : "text"}
                label={field.label}
                value={values[field.name] ?? (field.type === "multiselect" ? [] : "")}
                required={field.required}
                multiline={field.multiline}
                minRows={field.multiline ? 3 : undefined}
                error={submitted && field.required && !values[field.name]}
                helperText={submitted && field.required && !values[field.name] ? `${field.label} is required` : " "}
                onChange={(event) => {
                  const raw = event.target.value;
                  setValues((current) => ({
                    ...current,
                    [field.name]: field.type === "number" ? Number(raw) : raw
                  }));
                }}
              >
                {(field.options ?? []).map((option) => (
                  <MenuItem key={option.value} value={option.value}>{option.label}</MenuItem>
                ))}
              </TextField>
            ))}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button disabled={loading} type="submit" variant="contained">{submitLabel}</Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
