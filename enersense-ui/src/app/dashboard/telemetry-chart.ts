import {
  Component,
  computed,
  input,
} from '@angular/core';

import {
  BaseChartDirective,
} from 'ng2-charts';

import {
  ChartData,
  ChartOptions,
} from 'chart.js';

import {
  TelemetryReading,
} from '../core/models';

@Component({
  selector: 'app-telemetry-chart',

  imports: [
    BaseChartDirective,
  ],

  template: `
    <section class="chart-panel">

      <div class="chart-header">

        <div>

          <p class="kicker">
            TREND ANALYSIS
          </p>

          <h3>
            Temperature History
          </h3>

          <span class="description">
            Last 20 live sensor readings
          </span>

        </div>


        <div class="live-indicator">

          <span></span>

          STREAMING

        </div>

      </div>


      <div class="chart-container">

        <canvas
          baseChart
          [data]="chartData()"
          [options]="chartOptions"
          type="line"
        >
        </canvas>

      </div>

    </section>
  `,

  styles: `
    :host {
      display: block;
      margin-bottom: 18px;
    }

    .chart-panel {
      overflow: hidden;

      border:
        1px solid #dce5e8;

      border-radius: 12px;

      background: #ffffff;

      box-shadow:
        0 4px 14px
        rgba(
          38,
          61,
          70,
          0.035
        );
    }

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      padding: 17px 20px;

      border-bottom:
        1px solid #e6edef;
    }

    .kicker {
      margin: 0;

      color: #159d96;

      font-size: 9px;
      font-weight: 800;
      letter-spacing: 0.12em;
    }

    h3 {
      margin: 3px 0;

      color: #21343d;

      font-size: 17px;
    }

    .description {
      color: #94a3a9;
      font-size: 11px;
    }

    .live-indicator {
      display: flex;
      align-items: center;
      gap: 8px;

      padding: 6px 9px;

      border:
        1px solid #cce7e4;

      border-radius: 6px;

      background: #edf8f7;
      color: #14847e;

      font-size: 9px;
      font-weight: 800;
      letter-spacing: 0.07em;
    }

    .live-indicator span {
      width: 7px;
      height: 7px;

      border-radius: 50%;

      background: #20b2aa;

      box-shadow:
        0 0 0 4px
        rgba(
          32,
          178,
          170,
          0.12
        );
    }

    .chart-container {
      position: relative;

      height: 305px;

      padding:
        18px
        20px
        14px;
    }
  `,
})
export class TelemetryChart {

  readonly readings =
    input.required<
      TelemetryReading[]
    >();


  readonly chartData =
    computed<
      ChartData<'line'>
    >(() => {

      const readings =
        [
          ...this.readings(),
        ]
          .slice(0, 20)
          .reverse();

      return {

        labels:
          readings.map(
            (reading) =>
              new Date(
                reading.recordedAt
              )
                .toLocaleTimeString(
                  'en-GB',
                  {
                    hour12: false,
                  }
                )
          ),

        datasets: [
          {

            label:
              'Temperature °C',

            data:
              readings.map(
                (reading) =>
                  reading.temperature
              ),

            borderColor:
              '#18a7a0',

            backgroundColor:
              'rgba(24, 167, 160, 0.08)',

            pointBackgroundColor:
              '#18a7a0',

            pointBorderColor:
              '#ffffff',

            pointBorderWidth: 2,

            tension: 0.35,

            pointRadius: 3,

            pointHoverRadius: 5,

            borderWidth: 2,

            fill: true,

          },
        ],
      };

    });


  readonly chartOptions:
    ChartOptions<'line'> = {

      responsive: true,

      maintainAspectRatio: false,

      animation: false,

      interaction: {
        intersect: false,
        mode: 'index',
      },

      plugins: {

        legend: {
          display: false,
        },

        tooltip: {

          backgroundColor:
            '#10232d',

          titleColor:
            '#ffffff',

          bodyColor:
            '#d9e7e9',

          padding: 10,

          displayColors: false,

        },

      },

      scales: {

        x: {

          border: {
            display: false,
          },

          ticks: {
            color: '#89999f',
            maxTicksLimit: 8,
            font: {
              size: 10,
            },
          },

          grid: {
            color:
              'rgba(99, 124, 133, 0.08)',
          },

        },

        y: {

          border: {
            display: false,
          },

          ticks: {
            color: '#89999f',
            font: {
              size: 10,
            },
          },

          grid: {
            color:
              'rgba(99, 124, 133, 0.10)',
          },

          title: {
            display: true,
            text: 'Temperature °C',
            color: '#7c9097',
            font: {
              size: 10,
            },
          },

        },

      },

    };
}