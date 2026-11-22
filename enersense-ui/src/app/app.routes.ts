import {
  Routes,
} from '@angular/router';

import {
  Dashboard,
} from './dashboard/dashboard';

import {
  Login,
} from './auth/login';

import {
  authGuard,
} from './auth/auth.guard';

export const routes:
  Routes = [

    {
      path: 'login',
      component: Login,
    },

    {
      path: '',
      component: Dashboard,
      canActivate: [
        authGuard,
      ],
    },

    {
      path: '**',
      redirectTo: '',
    },

  ];