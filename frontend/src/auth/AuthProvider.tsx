import { createContext, PropsWithChildren, useContext, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, setUnauthorizedHandler, unwrap } from "../api/client";
import { tokenStore, type StoredUser } from "./tokenStore";
import type { TokenResponse, User } from "../types/api";

interface AuthContextValue {
  user: StoredUser | null;
  profile?: User;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  roles: string[];
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<StoredUser | null>(() => tokenStore.getUser());
  const queryClient = useQueryClient();

  setUnauthorizedHandler(() => {
    setUser(null);
    queryClient.clear();
  });

  const loginMutation = useMutation({
    mutationFn: async ({ email, password }: { email: string; password: string }) =>
      unwrap<TokenResponse>(api.post("/auth/login", { email, password })),
    onSuccess: (token) => {
      tokenStore.setSession(token);
      setUser({ userId: token.userId, email: token.email, roles: token.roles });
      queryClient.invalidateQueries();
    }
  });

  const profileQuery = useQuery({
    queryKey: ["profile"],
    queryFn: () => unwrap<User>(api.get("/profile")),
    enabled: Boolean(user)
  });

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      profile: profileQuery.data,
      isAuthenticated: Boolean(user),
      roles: user?.roles ?? [],
      login: async (email, password) => {
        await loginMutation.mutateAsync({ email, password });
      },
      logout: async () => {
        const refreshToken = tokenStore.getRefreshToken();
        if (refreshToken) {
          await api.post("/auth/logout", { refreshToken }).catch(() => undefined);
        }
        tokenStore.clear();
        queryClient.clear();
        setUser(null);
      }
    }),
    [loginMutation, profileQuery.data, queryClient, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
