import {
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';

import {
  DatePipe,
  DecimalPipe,
} from '@angular/common';

import {
  Router,
} from '@angular/router';

import {
  forkJoin,
} from 'rxjs';

import {
  takeUntilDestroyed,
} from '@angular/core/rxjs-interop';

import {
  AlertItem,
  Device,
  Facility,
  TelemetryReading,
} from '../core/models';

import {
  ApiService,
} from '../core/api.service';

import {
  LiveEventsService,
} from '../core/live-events.service';

import {
  AuthService,
} from '../auth/auth.service';

import {
  TelemetryChart,
} from './telemetry-chart';

import {
  ManagementPanel,
} from '../management/management-panel';

@Component({
  selector: 'app-dashboard',

  imports: [
    DatePipe,
    DecimalPipe,
    TelemetryChart,
    ManagementPanel,
  ],

  templateUrl:
    './dashboard.html',

  styleUrl:
    './dashboard.scss',
})
export class Dashboard
  implements OnInit {

  private readonly api =
    inject(ApiService);

  private readonly liveEvents =
    inject(LiveEventsService);

  private readonly auth =
    inject(AuthService);

  private readonly router =
    inject(Router);

  private readonly destroyRef =
    inject(DestroyRef);

  readonly facilities =
    signal<Facility[]>([]);

  readonly devices =
    signal<Device[]>([]);

  readonly telemetry =
    signal<TelemetryReading[]>([]);

  readonly alerts =
    signal<AlertItem[]>([]);

  readonly selectedDeviceId =
    signal<number | null>(null);

  readonly loading =
    signal(true);

  readonly errorMessage =
    signal<string | null>(null);

  readonly currentUser =
    this.auth.user;

  readonly isAdmin =
    this.auth.isAdmin;

  readonly selectedDevice =
    computed(() =>
      this.devices().find(
        (device) =>
          device.id ===
          this.selectedDeviceId()
      )
    );

  readonly latestReading =
    computed(
      () =>
        this.telemetry()[0]
        ?? null
    );

  readonly activeAlertsCount =
    computed(
      () =>
        this.alerts().filter(
          (alert) =>
            !alert.acknowledged
        ).length
    );

  ngOnInit(): void {

    this.loadInitialData();

    this.connectToLiveEvents();
  }

  logout(): void {

    this.auth.logout();

    this.router.navigateByUrl(
      '/login'
    );
  }

  selectDevice(
    deviceId: number
  ): void {

    this.selectedDeviceId.set(
      deviceId
    );

    this.api
      .getTelemetry(deviceId)
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: (readings) => {

          this.telemetry.set(
            readings.slice(
              0,
              50
            )
          );

          this.errorMessage.set(
            null
          );
        },

        error: () => {

          this.errorMessage.set(
            'Could not load telemetry.'
          );
        },

      });
  }

  onFacilityCreated(
    facility: Facility
  ): void {

    this.facilities.update(
      (facilities) =>
        [
          ...facilities,
          facility,
        ].sort(
          (first, second) =>
            first.id - second.id
        )
    );
  }

  onDeviceCreated(
    device: Device
  ): void {

    this.devices.update(
      (devices) =>
        [
          ...devices,
          device,
        ].sort(
          (first, second) =>
            first.id - second.id
        )
    );
  }

  acknowledgeAlert(
    alertId: number
  ): void {

    this.api
      .acknowledgeAlert(
        alertId
      )
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next:
          (updatedAlert) => {

            this.alerts.update(
              (alerts) =>
                alerts.map(
                  (alert) =>
                    alert.id ===
                    updatedAlert.id
                      ? updatedAlert
                      : alert
                )
            );
          },

        error: () => {

          this.errorMessage.set(
            'Could not acknowledge alert.'
          );
        },

      });
  }

  private loadInitialData():
    void {

    forkJoin({

      facilities:
        this.api.getFacilities(),

      devices:
        this.api.getDevices(),

      alerts:
        this.api.getAlerts(),

    })
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe({

        next: ({
          facilities,
          devices,
          alerts,
        }) => {

          this.facilities.set(
            facilities
          );

          this.devices.set(
            devices
          );

          this.alerts.set(
            alerts
          );

          if (
            devices.length > 0
          ) {

            this.selectDevice(
              devices[0].id
            );
          }

          this.loading.set(
            false
          );
        },

        error: () => {

          this.errorMessage.set(
            'Could not load dashboard data.'
          );

          this.loading.set(
            false
          );
        },

      });
  }

  private connectToLiveEvents():
    void {

    this.liveEvents
      .stream()
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe(
        (event) => {

          if (
            event.type ===
            'telemetry'
          ) {

            if (
              event.data.deviceId
              ===
              this.selectedDeviceId()
            ) {

              this.telemetry.update(
                (readings) =>
                  [
                    event.data,
                    ...readings,
                  ].slice(
                    0,
                    50
                  )
              );
            }

            return;
          }

          if (
            event.type ===
            'alert'
          ) {

            this.alerts.update(
              (alerts) => [
                event.data,
                ...alerts,
              ]
            );
          }

        }
      );
  }
}