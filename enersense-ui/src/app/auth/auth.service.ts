import {
  computed,
  inject,
  Injectable,
  signal,
} from '@angular/core';

import {
  HttpClient,
} from '@angular/common/http';

import {
  Observable,
  tap,
} from 'rxjs';

import {
  AuthResponse,
  AuthUser,
  LoginRequest,
} from '../core/models';

export const AUTH_TOKEN_KEY =
  'enersense_access_token';

const AUTH_USER_KEY =
  'enersense_user';

const AUTH_EXPIRY_KEY =
  'enersense_token_expiry';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private readonly http =
    inject(HttpClient);

  private readonly token =
    signal<string | null>(
      localStorage.getItem(
        AUTH_TOKEN_KEY
      )
    );

  readonly user =
    signal<AuthUser | null>(
      this.loadStoredUser()
    );

  readonly isAuthenticated =
    computed(() => {

      const token =
        this.token();

      const expiresAt =
        localStorage.getItem(
          AUTH_EXPIRY_KEY
        );

      if (
        !token
        || !expiresAt
      ) {
        return false;
      }

      const expirationTime =
        Date.parse(expiresAt);

      if (
        Number.isNaN(
          expirationTime
        )
      ) {
        return false;
      }

      return expirationTime
        > Date.now();
    });

  readonly isAdmin =
    computed(
      () =>
        this.user()?.role
        === 'ADMIN'
    );

  readonly isOperator =
    computed(
      () =>
        this.user()?.role
        === 'OPERATOR'
    );

  login(
    request: LoginRequest
  ): Observable<AuthResponse> {

    return this.http
      .post<AuthResponse>(
        '/api/auth/login',
        request
      )
      .pipe(
        tap(
          (response) =>
            this.storeSession(
              response
            )
        )
      );
  }

  logout(): void {

    localStorage.removeItem(
      AUTH_TOKEN_KEY
    );

    localStorage.removeItem(
      AUTH_USER_KEY
    );

    localStorage.removeItem(
      AUTH_EXPIRY_KEY
    );

    this.token.set(null);

    this.user.set(null);
  }

  getToken():
    string | null {

    if (
      !this.isAuthenticated()
    ) {

      this.logout();

      return null;
    }

    return this.token();
  }

  private storeSession(
    response: AuthResponse
  ): void {

    localStorage.setItem(
      AUTH_TOKEN_KEY,
      response.token
    );

    localStorage.setItem(
      AUTH_USER_KEY,
      JSON.stringify(
        response.user
      )
    );

    localStorage.setItem(
      AUTH_EXPIRY_KEY,
      response.expiresAt
    );

    this.token.set(
      response.token
    );

    this.user.set(
      response.user
    );
  }

  private loadStoredUser():
    AuthUser | null {

    const storedUser =
      localStorage.getItem(
        AUTH_USER_KEY
      );

    if (!storedUser) {
      return null;
    }

    try {

      return JSON.parse(
        storedUser
      ) as AuthUser;

    } catch {

      localStorage.removeItem(
        AUTH_USER_KEY
      );

      return null;
    }
  }
}