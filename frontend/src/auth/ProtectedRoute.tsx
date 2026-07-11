import { Navigate, Outlet, useLocation } from "react-router-dom";
import { Alert, Box } from "@mui/material";
import { useAuth } from "./AuthProvider";
import { hasAnyRole } from "../utils/permissions";
import type { RoleName } from "../types/api";

export function ProtectedRoute({ roles }: { roles?: RoleName[] }) {
  const auth = useAuth();
  const location = useLocation();

  if (!auth.isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (!hasAnyRole(auth.roles, roles)) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning">You do not have permission to open this workspace area.</Alert>
      </Box>
    );
  }

  return <Outlet />;
}
