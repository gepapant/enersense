import {
  inject,
  Injectable,
} from '@angular/core';

import {
  Router,
} from '@angular/router';

import {
  Observable,
} from 'rxjs';

import {
  AlertItem,
  TelemetryReading,
} from './models';

import {
  AuthService,
} from '../auth/auth.service';

export type LiveEvent =
  | {
      type: 'telemetry';
      data: TelemetryReading;
    }
  | {
      type: 'alert';
      data: AlertItem;
    };

@Injectable({
  providedIn: 'root',
})
export class LiveEventsService {

  private readonly auth =
    inject(AuthService);

  private readonly router =
    inject(Router);

  stream(): Observable<LiveEvent> {

    return new Observable<LiveEvent>(
      (subscriber) => {

        const abortController =
          new AbortController();

        let reconnectTimer:
          number | null = null;

        let stopped = false;

        const scheduleReconnect =
          (): void => {

            if (
              stopped
              ||
              abortController
                .signal
                .aborted
            ) {
              return;
            }

            reconnectTimer =
              window.setTimeout(
                () => {
                  void connect();
                },
                2000
              );
          };

        const connect =
          async (): Promise<void> => {

            const token =
              this.auth.getToken();

            if (!token) {

              this.auth.logout();

              void this.router
                .navigateByUrl(
                  '/login'
                );

              return;
            }

            try {

              const response =
                await fetch(
                  '/api/events',
                  {
                    method: 'GET',

                    headers: {
                      Accept:
                        'text/event-stream',

                      Authorization:
                        `Bearer ${token}`,
                    },

                    signal:
                      abortController
                        .signal,
                  }
                );

              if (
                response.status === 401
              ) {

                this.auth.logout();

                void this.router
                  .navigateByUrl(
                    '/login'
                  );

                return;
              }

              if (
                !response.ok
              ) {

                throw new Error(
                  `SSE request failed with status ${response.status}`
                );
              }

              if (
                !response.body
              ) {

                throw new Error(
                  'SSE response has no body'
                );
              }

              const reader =
                response.body
                  .getReader();

              const decoder =
                new TextDecoder();

              let buffer = '';

              while (!stopped) {

                const {
                  value,
                  done,
                } =
                  await reader.read();

                if (done) {
                  break;
                }

                buffer +=
                  decoder.decode(
                    value,
                    {
                      stream: true,
                    }
                  );

                buffer =
                  buffer.replace(
                    /\r\n/g,
                    '\n'
                  );

                let separatorIndex =
                  buffer.indexOf(
                    '\n\n'
                  );

                while (
                  separatorIndex !== -1
                ) {

                  const eventBlock =
                    buffer.slice(
                      0,
                      separatorIndex
                    );

                  buffer =
                    buffer.slice(
                      separatorIndex + 2
                    );

                  const liveEvent =
                    this.parseEvent(
                      eventBlock
                    );

                  if (liveEvent) {

                    subscriber.next(
                      liveEvent
                    );
                  }

                  separatorIndex =
                    buffer.indexOf(
                      '\n\n'
                    );
                }
              }

              if (!stopped) {
                scheduleReconnect();
              }

            } catch (error) {

              if (
                stopped
                ||
                abortController
                  .signal
                  .aborted
              ) {
                return;
              }

              console.error(
                'EnerSense live stream disconnected.',
                error
              );

              scheduleReconnect();
            }
          };

        void connect();

        return () => {

          stopped = true;

          abortController.abort();

          if (
            reconnectTimer !== null
          ) {

            window.clearTimeout(
              reconnectTimer
            );
          }
        };
      }
    );
  }

  private parseEvent(
    eventBlock: string
  ): LiveEvent | null {

    const lines =
      eventBlock.split('\n');

    let eventName = '';

    const dataLines:
      string[] = [];

    for (
      const line of lines
    ) {

      if (
        line.startsWith(
          'event:'
        )
      ) {

        eventName =
          line
            .slice(
              'event:'.length
            )
            .trim();

        continue;
      }

      if (
        line.startsWith(
          'data:'
        )
      ) {

        dataLines.push(
          line
            .slice(
              'data:'.length
            )
            .trimStart()
        );
      }
    }

    if (
      eventName !== 'telemetry'
      &&
      eventName !== 'alert'
    ) {

      return null;
    }

    const rawData =
      dataLines.join('\n');

    if (!rawData) {
      return null;
    }

    try {

      if (
        eventName === 'telemetry'
      ) {

        const telemetryData =
          JSON.parse(
            rawData
          ) as TelemetryReading;

        return {
          type: 'telemetry',
          data: telemetryData,
        };
      }

      const alertData =
        JSON.parse(
          rawData
        ) as AlertItem;

      return {
        type: 'alert',
        data: alertData,
      };

    } catch (error) {

      console.error(
        'Invalid SSE event received.',
        error
      );

      return null;
    }
  }
}