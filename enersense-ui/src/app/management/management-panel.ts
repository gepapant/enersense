import {
  Component,
  DestroyRef,
  inject,
  input,
  output,
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
  takeUntilDestroyed,
} from '@angular/core/rxjs-interop';

import {
  ApiService,
} from '../core/api.service';

import {
  AuthService,
} from '../auth/auth.service';

import {
  AuthUser,
  Device,
  Facility,
  ThresholdRule,
  UserRole,
} from '../core/models';

@Component({
  selector: 'app-management-panel',

  standalone: true,

  imports: [
    ReactiveFormsModule,
  ],

  templateUrl:
    './management-panel.html',

  styleUrl:
    './management-panel.scss',
})
export class ManagementPanel {

  private readonly api =
    inject(ApiService);

  private readonly auth =
    inject(AuthService);

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly destroyRef =
    inject(DestroyRef);

  readonly facilities =
    input<Facility[]>([]);

  readonly devices =
    input<Device[]>([]);

  readonly facilityCreated =
    output<Facility>();

  readonly deviceCreated =
    output<Device>();

  readonly currentUser =
    this.auth.user;

  readonly activeTab =
    signal<
      'facility'
      | 'device'
      | 'threshold'
      | 'user'
    >('facility');

  readonly facilitySaving =
    signal(false);

  readonly deviceSaving =
    signal(false);

  readonly thresholdSaving =
    signal(false);

  readonly thresholdLoading =
    signal(false);

  readonly thresholdRules =
    signal<ThresholdRule[]>([]);

  readonly ruleUpdatingId =
    signal<number | null>(null);

  readonly ruleDeletingId =
    signal<number | null>(null);

  readonly users =
    signal<AuthUser[]>([]);

  readonly usersLoading =
    signal(false);

  readonly userSaving =
    signal(false);

  readonly userUpdatingId =
    signal<number | null>(null);

  readonly successMessage =
    signal<string | null>(null);

  readonly errorMessage =
    signal<string | null>(null);

  readonly deviceTypes = [
    'BOILER',
    'PUMP',
    'FREEZER',
    'HVAC',
    'ENERGY_METER',
  ];

  readonly metrics = [
    'POWER',
    'VOLTAGE',
    'CURRENT',
    'TEMPERATURE',
  ];

  readonly operators = [
    'GREATER_THAN',
    'GREATER_THAN_OR_EQUAL',
    'LESS_THAN',
    'LESS_THAN_OR_EQUAL',
  ];

  readonly severities = [
    'WARNING',
    'CRITICAL',
  ];

  readonly userRoles:
    UserRole[] = [
      'OPERATOR',
      'ADMIN',
    ];

  readonly facilityForm =
    this.formBuilder
      .nonNullable
      .group({

        name: [
          '',
          [
            Validators.required,
            Validators.maxLength(120),
          ],
        ],

        location: [
          '',
          [
            Validators.required,
            Validators.maxLength(200),
          ],
        ],

      });

  readonly deviceForm =
    this.formBuilder.group({

      name: [
        '',
        [
          Validators.required,
          Validators.maxLength(120),
        ],
      ],

      type: [
        'BOILER',
        Validators.required,
      ],

      facilityId: [
        null as number | null,
        Validators.required,
      ],

    });

  readonly thresholdForm =
    this.formBuilder.group({

      deviceId: [
        null as number | null,
        Validators.required,
      ],

      metric: [
        'TEMPERATURE',
        Validators.required,
      ],

      comparisonOperator: [
        'GREATER_THAN',
        Validators.required,
      ],

      thresholdValue: [
        '',
        Validators.required,
      ],

      severity: [
        'WARNING',
        Validators.required,
      ],

    });

  readonly userForm =
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
          [
            Validators.required,
            Validators.minLength(8),
            Validators.maxLength(128),
          ],
        ],

        role: [
          'OPERATOR' as UserRole,
          Validators.required,
        ],

      });

  selectTab(
    tab:
      'facility'
      | 'device'
      | 'threshold'
      | 'user'
  ): void {

    this.activeTab.set(tab);

    this.clearMessages();

    if (
      tab === 'threshold'
    ) {
      this.prepareThresholdTab();
    }

    if (
      tab === 'user'
    ) {
      this.loadUsers();
    }
  }

  submitFacility(): void {

    if (
      this.facilityForm.invalid
    ) {

      this.facilityForm
        .markAllAsTouched();

      return;
    }

    this.facilitySaving.set(true);

    this.clearMessages();

    const value =
      this.facilityForm
        .getRawValue();

    this.api
      .createFacility({
        name:
          value.name.trim(),

        location:
          value.location.trim(),
      })
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: (facility) => {

          this.facilityCreated.emit(
            facility
          );

          this.facilityForm.reset({
            name: '',
            location: '',
          });

          this.successMessage.set(
            `Facility "${facility.name}" created successfully.`
          );

          this.facilitySaving.set(false);
        },

        error: () => {

          this.errorMessage.set(
            'Could not create facility.'
          );

          this.facilitySaving.set(false);
        },

      });
  }

  submitDevice(): void {

    if (
      this.deviceForm.invalid
    ) {

      this.deviceForm
        .markAllAsTouched();

      return;
    }

    const value =
      this.deviceForm
        .getRawValue();

    if (
      value.facilityId === null
      || !value.name
      || !value.type
    ) {
      return;
    }

    this.deviceSaving.set(true);

    this.clearMessages();

    this.api
      .createDevice({

        name:
          value.name.trim(),

        type:
          value.type,

        facilityId:
          value.facilityId,

      })
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: (device) => {

          this.deviceCreated.emit(
            device
          );

          this.deviceForm.reset({
            name: '',
            type: 'BOILER',
            facilityId:
              value.facilityId,
          });

          this.successMessage.set(
            `Device "${device.name}" created successfully.`
          );

          this.deviceSaving.set(false);
        },

        error: () => {

          this.errorMessage.set(
            'Could not create device.'
          );

          this.deviceSaving.set(false);
        },

      });
  }

  submitThresholdRule():
    void {

    this.clearMessages();

    if (
      this.thresholdForm.invalid
    ) {

      this.thresholdForm
        .markAllAsTouched();

      return;
    }

    const value =
      this.thresholdForm
        .getRawValue();

    if (
      value.deviceId === null
      || !value.metric
      || !value.comparisonOperator
      || !value.severity
    ) {
      return;
    }

    const thresholdValue =
      this.parseThresholdValue(
        value.thresholdValue
      );

    if (
      thresholdValue === null
    ) {

      this.errorMessage.set(
        'Enter a valid threshold value.'
      );

      return;
    }

    this.thresholdSaving.set(true);

    this.api
      .createThresholdRule(
        value.deviceId,
        {
          metric:
            value.metric,

          comparisonOperator:
            value.comparisonOperator,

          thresholdValue,

          severity:
            value.severity,
        }
      )
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: (rule) => {

          this.thresholdRules.update(
            (rules) => [
              ...rules,
              rule,
            ]
          );

          this.thresholdForm
            .patchValue({
              thresholdValue: '',
            });

          this.successMessage.set(
            'Threshold rule created successfully.'
          );

          this.thresholdSaving.set(false);
        },

        error: () => {

          this.errorMessage.set(
            'Could not create threshold rule.'
          );

          this.thresholdSaving.set(false);
        },

      });
  }

  loadThresholdRules():
    void {

    const deviceId =
      this.thresholdForm
        .controls
        .deviceId
        .value;

    this.thresholdRules.set([]);

    this.clearMessages();

    if (
      deviceId === null
    ) {
      return;
    }

    this.thresholdLoading.set(true);

    this.api
      .getThresholdRules(
        deviceId
      )
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: (rules) => {

          this.thresholdRules.set(
            rules
          );

          this.thresholdLoading.set(false);
        },

        error: () => {

          this.errorMessage.set(
            'Could not load threshold rules.'
          );

          this.thresholdLoading.set(false);
        },

      });
  }

  setRuleActive(
    rule: ThresholdRule
  ): void {

    this.clearMessages();

    this.ruleUpdatingId.set(
      rule.id
    );

    this.api
      .updateThresholdRuleActive(
        rule.id,
        !rule.active
      )
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: (updatedRule) => {

          this.thresholdRules.update(
            (rules) =>
              rules.map(
                (currentRule) =>
                  currentRule.id
                  === updatedRule.id
                    ? updatedRule
                    : currentRule
              )
          );

          this.ruleUpdatingId.set(null);
        },

        error: () => {

          this.errorMessage.set(
            'Could not update threshold rule.'
          );

          this.ruleUpdatingId.set(null);
        },

      });
  }

  deleteRule(
    rule: ThresholdRule
  ): void {

    const confirmed =
      window.confirm(
        `Delete ${rule.metric} threshold rule?`
      );

    if (!confirmed) {
      return;
    }

    this.clearMessages();

    this.ruleDeletingId.set(
      rule.id
    );

    this.api
      .deleteThresholdRule(
        rule.id
      )
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: () => {

          this.thresholdRules.update(
            (rules) =>
              rules.filter(
                (currentRule) =>
                  currentRule.id
                  !== rule.id
              )
          );

          this.successMessage.set(
            'Threshold rule deleted.'
          );

          this.ruleDeletingId.set(null);
        },

        error: () => {

          this.errorMessage.set(
            'Could not delete threshold rule.'
          );

          this.ruleDeletingId.set(null);
        },

      });
  }

  loadUsers(): void {

    this.usersLoading.set(true);

    this.clearMessages();

    this.api
      .getUsers()
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: (users) => {

          this.users.set(users);

          this.usersLoading.set(false);
        },

        error: () => {

          this.errorMessage.set(
            'Could not load users.'
          );

          this.usersLoading.set(false);
        },

      });
  }

  submitUser(): void {

    this.clearMessages();

    if (
      this.userForm.invalid
    ) {

      this.userForm
        .markAllAsTouched();

      return;
    }

    const value =
      this.userForm
        .getRawValue();

    this.userSaving.set(true);

    this.api
      .createUser({
        email:
          value.email
            .trim()
            .toLowerCase(),

        password:
          value.password,

        role:
          value.role,
      })
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: (user) => {

          this.users.update(
            (users) => [
              ...users,
              user,
            ].sort(
              (a, b) =>
                a.id - b.id
            )
          );

          this.userForm.reset({
            email: '',
            password: '',
            role: 'OPERATOR',
          });

          this.successMessage.set(
            `User "${user.email}" created successfully.`
          );

          this.userSaving.set(false);
        },

        error: (
          error:
            HttpErrorResponse
        ) => {

          if (
            error.status === 409
          ) {

            this.errorMessage.set(
              'A user with this email already exists.'
            );

          } else {

            this.errorMessage.set(
              'Could not create user.'
            );
          }

          this.userSaving.set(false);
        },

      });
  }

  setUserActive(
    user: AuthUser
  ): void {

    if (
      user.id ===
      this.currentUser()?.id
    ) {

      this.errorMessage.set(
        'You cannot deactivate your own account.'
      );

      return;
    }

    this.clearMessages();

    this.userUpdatingId.set(
      user.id
    );

    this.api
      .updateUserActive(
        user.id,
        !user.active
      )
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: (updatedUser) => {

          this.users.update(
            (users) =>
              users.map(
                (currentUser) =>
                  currentUser.id
                  === updatedUser.id
                    ? updatedUser
                    : currentUser
              )
          );

          this.userUpdatingId.set(null);
        },

        error: () => {

          this.errorMessage.set(
            'Could not update user.'
          );

          this.userUpdatingId.set(null);
        },

      });
  }

  operatorSymbol(
    operator: string
  ): string {

    switch (operator) {

      case 'GREATER_THAN':
        return '>';

      case 'GREATER_THAN_OR_EQUAL':
        return '≥';

      case 'LESS_THAN':
        return '<';

      case 'LESS_THAN_OR_EQUAL':
        return '≤';

      default:
        return operator;
    }
  }

  thresholdUnit(
    metric: string | null
  ): string {

    switch (metric) {

      case 'TEMPERATURE':
        return '°C';

      case 'POWER':
        return 'W';

      case 'VOLTAGE':
        return 'V';

      case 'CURRENT':
        return 'A';

      default:
        return '';
    }
  }

  private parseThresholdValue(
    rawValue: string | null
  ): number | null {

    if (
      rawValue === null
    ) {
      return null;
    }

    const normalized =
      rawValue
        .trim()
        .replace(',', '.');

    if (
      normalized.length === 0
    ) {
      return null;
    }

    const parsed =
      Number(normalized);

    if (
      !Number.isFinite(parsed)
    ) {
      return null;
    }

    return parsed;
  }

  private prepareThresholdTab():
    void {

    if (
      this.devices().length === 0
    ) {
      return;
    }

    const currentDeviceId =
      this.thresholdForm
        .controls
        .deviceId
        .value;

    if (
      currentDeviceId === null
    ) {

      this.thresholdForm
        .controls
        .deviceId
        .setValue(
          this.devices()[0].id
        );
    }

    this.loadThresholdRules();
  }

  private clearMessages():
    void {

    this.successMessage.set(null);

    this.errorMessage.set(null);
  }
}