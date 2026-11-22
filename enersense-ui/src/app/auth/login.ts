import {
  Component,
  inject,
  signal,
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import {
  HttpErrorResponse,
} from '@angular/common/http';

import {
  ActivatedRoute,
  Router,
} from '@angular/router';

import {
  AuthService,
} from './auth.service';

@Component({
  selector: 'app-login',

  standalone: true,

  imports: [
    ReactiveFormsModule,
  ],

  templateUrl:
    './login.html',

  styleUrl:
    './login.scss',
})
export class Login {

  private readonly auth =
    inject(AuthService);

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly router =
    inject(Router);

  private readonly route =
    inject(ActivatedRoute);

  readonly loading =
    signal(false);

  readonly errorMessage =
    signal<string | null>(
      null
    );

  readonly form =
    this.formBuilder
      .nonNullable
      .group({

        email: [
          '',
          [
            Validators.required,
            Validators.email,
          ],
        ],

        password: [
          '',
          Validators.required,
        ],

      });

  submit(): void {

    if (
      this.form.invalid
    ) {

      this.form
        .markAllAsTouched();

      return;
    }

    this.loading.set(true);

    this.errorMessage.set(null);

    const request =
      this.form.getRawValue();

    this.auth
      .login(request)
      .subscribe({

        next: () => {

          const requestedUrl =
            this.route
              .snapshot
              .queryParamMap
              .get(
                'returnUrl'
              );

          const returnUrl =
            requestedUrl
            &&
            requestedUrl
              .startsWith('/')
            &&
            !requestedUrl
              .startsWith('//')
              ? requestedUrl
              : '/';

          this.router
            .navigateByUrl(
              returnUrl
            );
        },

        error: (
          error:
            HttpErrorResponse
        ) => {

          if (
            error.status === 401
          ) {

            this.errorMessage.set(
              'Invalid email or password.'
            );

          } else {

            this.errorMessage.set(
              'Could not connect to EnerSense.'
            );
          }

          this.loading.set(
            false
          );
        },

      });
  }
}