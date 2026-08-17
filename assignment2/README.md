# Meridian — World Clock Dashboard

A real-time, multi-region clock dashboard built with React + Vite. Combines analog and digital
displays, timezone management, alarms, and dynamic UI controls in one responsive interface.

## Getting started

```bash
npm install
npm run dev
```

Then open the URL Vite prints (usually `http://localhost:5173`).

To build a production bundle:

```bash
npm run build
npm run preview
```

## Features

- **Live multi-region clocks** — analog + digital, updating every second via `Intl.DateTimeFormat`
  so DST and UTC offsets are always correct (no hand-rolled offset math).
- **Add/remove regions** — search a catalog of world cities and build your own dashboard.
- **Alarms** — set an alarm against any region's local time; it fires based on *that region's*
  clock, not your browser's local time. Alarms persist in `localStorage`.
- **Dynamic UI controls** — toggle analog display, 12h/24h format; each card shows a day/night
  badge and a day-progress strip driven by the region's actual position in its day.
- **Responsive** — grid collapses to a single column on mobile; respects `prefers-reduced-motion`.

## Architecture notes

- `src/context/TimeContext.jsx` is a single ticking clock (`requestAnimationFrame`, throttled to
  one state update per second) shared by every card. This avoids the classic world-clock
  performance trap of each clock running its own `setInterval` and independently forcing
  re-renders — the biggest risk called out when this was scoped.
- `src/utils/timezones.js` centralizes all timezone/DST math through `Intl.DateTimeFormat`.
- `src/hooks/useAlarms.js` evaluates alarms against each alarm's own timezone every tick.
- Components are memoized (`AnalogClock`, `DigitalClock`) so adding more regions doesn't scale
  render cost linearly with unrelated re-renders.

## Project structure

```
src/
  components/
    AnalogClock.jsx       SVG analog clock face with day-progress arc
    DigitalClock.jsx       Digital readout, tabular numerals
    ClockCard.jsx           Combines both + region metadata per card
    AddRegionModal.jsx     Search + add a city to the dashboard
    AlarmDrawer.jsx         Set/list/toggle alarms
  context/
    TimeContext.jsx         Central ticking time source
  hooks/
    useAlarms.js             Alarm state + trigger logic
  utils/
    timezones.js              Timezone catalog + Intl-based time math
  App.jsx
  main.jsx
  styles.css
```

## Extending it

- Swap the timezone catalog in `utils/timezones.js` for the full IANA list if you want every
  timezone rather than a curated set of major cities.
- The alarm "ring" currently shows a banner — wire in the Web Audio API or the Notifications API
  in `useAlarms.js` if you want sound or OS-level notifications.
- Drag-to-reorder cards would slot into `App.jsx`'s `regionIds` state.
