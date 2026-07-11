import type { TokenResponse } from "../types/api";

const ACCESS_TOKEN_KEY = "yazidwms.accessToken";
const REFRESH_TOKEN_KEY = "yazidwms.refreshToken";
const USER_KEY = "yazidwms.user";

export interface StoredUser {
  userId: number;
  email: string;
  roles: string[];
}

export const tokenStore = {
  getAccessToken: () => sessionStorage.getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  getUser: (): StoredUser | null => {
    const value = localStorage.getItem(USER_KEY);
    return value ? (JSON.parse(value) as StoredUser) : null;
  },
  setSession: (token: TokenResponse) => {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, token.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, token.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify({ userId: token.userId, email: token.email, roles: token.roles }));
  },
  setAccessToken: (accessToken: string) => {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  },
  clear: () => {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
};
