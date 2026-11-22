import {
  inject,
  Injectable,
} from '@angular/core';

import {
  HttpClient,
} from '@angular/common/http';

import {
  Observable,
} from 'rxjs';

import {
  AlertItem,
  AuthUser,
  Device,
  DeviceCreateRequest,
  Facility,
  FacilityCreateRequest,
  TelemetryReading,
  ThresholdRule,
  ThresholdRuleCreateRequest,
  UserCreateRequest,
} from './models';

@Injectable({
  providedIn: 'root',
})
export class ApiService {

  private readonly http =
    inject(HttpClient);

  getFacilities():
    Observable<Facility[]> {

    return this.http.get<
      Facility[]
    >(
      '/api/facilities'
    );
  }

  createFacility(
    request:
      FacilityCreateRequest
  ): Observable<Facility> {

    return this.http.post<
      Facility
    >(
      '/api/facilities',
      request
    );
  }

  getDevices():
    Observable<Device[]> {

    return this.http.get<
      Device[]
    >(
      '/api/devices'
    );
  }

  createDevice(
    request:
      DeviceCreateRequest
  ): Observable<Device> {

    return this.http.post<
      Device
    >(
      '/api/devices',
      request
    );
  }

  getAlerts():
    Observable<AlertItem[]> {

    return this.http.get<
      AlertItem[]
    >(
      '/api/alerts'
    );
  }

  getTelemetry(
    deviceId: number
  ): Observable<
    TelemetryReading[]
  > {

    return this.http.get<
      TelemetryReading[]
    >(
      `/api/devices/${deviceId}/telemetry`
    );
  }

  acknowledgeAlert(
    alertId: number
  ): Observable<AlertItem> {

    return this.http.patch<
      AlertItem
    >(
      `/api/alerts/${alertId}/acknowledge`,
      {}
    );
  }

  getThresholdRules(
    deviceId: number
  ): Observable<
    ThresholdRule[]
  > {

    return this.http.get<
      ThresholdRule[]
    >(
      `/api/devices/${deviceId}/thresholds`
    );
  }

  createThresholdRule(
    deviceId: number,
    request:
      ThresholdRuleCreateRequest
  ): Observable<
    ThresholdRule
  > {

    return this.http.post<
      ThresholdRule
    >(
      `/api/devices/${deviceId}/thresholds`,
      request
    );
  }

  updateThresholdRuleActive(
    ruleId: number,
    active: boolean
  ): Observable<
    ThresholdRule
  > {

    return this.http.patch<
      ThresholdRule
    >(
      `/api/thresholds/${ruleId}/active`,
      {
        active,
      }
    );
  }

  deleteThresholdRule(
    ruleId: number
  ): Observable<void> {

    return this.http.delete<void>(
      `/api/thresholds/${ruleId}`
    );
  }

  getUsers():
    Observable<AuthUser[]> {

    return this.http.get<
      AuthUser[]
    >(
      '/api/users'
    );
  }

  createUser(
    request: UserCreateRequest
  ): Observable<AuthUser> {

    return this.http.post<
      AuthUser
    >(
      '/api/users',
      request
    );
  }

  updateUserActive(
    userId: number,
    active: boolean
  ): Observable<AuthUser> {

    return this.http.patch<
      AuthUser
    >(
      `/api/users/${userId}/active`,
      {
        active,
      }
    );
  }
}