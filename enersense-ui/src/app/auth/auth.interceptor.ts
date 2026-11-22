import {
  inject,
} from '@angular/core';

import {
  HttpErrorResponse,
  HttpInterceptorFn,
} from '@angular/common/http';

import {
  Router,
} from '@angular/router';

import {
  catchError,
  throwError,
} from 'rxjs';

import {
  AuthService,
} from './auth.service';

export const authInterceptor:
  HttpInterceptorFn =
    (
      request,
      next
    ) => {

      const auth =
        inject(AuthService);

      const router =
        inject(Router);

      const isApiRequest =
        request.url
          .startsWith('/api/');

      const isLoginRequest =
        request.url
          .startsWith(
            '/api/auth/login'
          );

      const isBootstrapRequest =
        request.url
          .startsWith(
            '/api/auth/bootstrap'
          );

      const isPublicAuthRequest =
        isLoginRequest
        || isBootstrapRequest;

      let outgoingRequest =
        request;

      if (
        isApiRequest
        && !isPublicAuthRequest
      ) {

        const token =
          auth.getToken();

        if (token) {

          outgoingRequest =
            request.clone({

              setHeaders: {

                Authorization:
                  `Bearer ${token}`,

              },

            });
        }
      }

      return next(
        outgoingRequest
      )
        .pipe(

          catchError(
            (
              error:
                HttpErrorResponse
            ) => {

              if (
                error.status === 401
                &&
                !isPublicAuthRequest
              ) {

                auth.logout();

                void router
                  .navigateByUrl(
                    '/login'
                  );
              }

              return throwError(
                () => error
              );
            }
          )

        );
    };