import { Button, Stack, Typography } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";

export function NotFoundPage() {
  return (
    <Stack spacing={2} alignItems="center" justifyContent="center" sx={{ minHeight: "60vh" }}>
      <Typography variant="h4">Page not found</Typography>
      <Typography color="text.secondary">The workspace area you requested does not exist.</Typography>
      <Button component={RouterLink} to="/" variant="contained">Go to dashboard</Button>
    </Stack>
  );
}
