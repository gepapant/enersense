export interface Facility {
  id: number;
  name: string;
  location: string;
}

export interface FacilityCreateRequest {
  name: string;
  location: string;
}

export interface Device {
  id: number;
  name: string;
  type: string;
  status: string;
  facilityId: number;
  facilityName: string;
}

export interface DeviceCreateRequest {
  name: string;
  type: string;
  facilityId: number;
}

export interface TelemetryReading {
  id: string;
  deviceId: number;
  power: number;
  voltage: number;
  current: number;
  temperature: number;
  recordedAt: string;
}

export interface AlertItem {
  id: number;
  deviceId: number;
  deviceName: string;
  ruleId: number;
  message: string;
  severity: string;
  createdAt: string;
  acknowledged: boolean;
  acknowledgedAt: string | null;
}

export interface ThresholdRule {
  id: number;
  deviceId: number;
  metric: string;
  comparisonOperator: string;
  thresholdValue: number;
  severity: string;
  active: boolean;
}

export interface ThresholdRuleCreateRequest {
  metric: string;
  comparisonOperator: string;
  thresholdValue: number;
  severity: string;
}

export type UserRole =
  'ADMIN'
  | 'OPERATOR';

export interface AuthUser {
  id: number;
  email: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
}

export interface UserCreateRequest {
  email: string;
  password: string;
  role: UserRole;
}

export interface UserActiveRequest {
  active: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  expiresAt: string;
  user: AuthUser;
}