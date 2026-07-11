import type { RoleName } from "../types/api";

export const hasAnyRole = (roles: string[] | undefined, allowed?: RoleName[]) => {
  if (!allowed || allowed.length === 0) {
    return true;
  }
  return Boolean(roles?.some((role) => allowed.includes(role as RoleName)));
};

export const canManage = (roles: string[] | undefined) => hasAnyRole(roles, ["ADMIN", "MANAGER"]);
export const canAdmin = (roles: string[] | undefined) => hasAnyRole(roles, ["ADMIN"]);
