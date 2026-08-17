import { useMemo } from 'react'
import { useNow } from '../context/TimeContext'
import { getZonedTimeParts, getDayProgress, accentFor } from '../utils/timezones'

// The dashboard's signature: instead of just listing clocks, this plots every
// active region on one shared 24-hour ring, so you can see the day sweeping
// across the world in a single glance — which region is waking up, which is
// mid-afternoon, which has gone quiet for the night.
export default function WorldPulseBar({ regions }) {
  const now = useNow()

  const points = useMemo(
    () =>
      regions.map((region) => {
        const parts = getZonedTimeParts(now, region.id)
        const progress = getDayProgress(parts)
        return {
          id: region.id,
          city: region.city,
          progress,
          hour: parts.hours,
          minute: parts.minutes,
          accent: accentFor(region.id),
        }
      }),
    [now, regions]
  )

  if (regions.length === 0) return null

  return (
    <div className="pulse">
      <div className="pulse__label">
        <span>World pulse</span>
        <span className="pulse__hint">00:00 → 24:00 local, per region</span>
      </div>
      <div className="pulse__track">
        <div className="pulse__band pulse__band--night" />
        <div className="pulse__band pulse__band--dawn" />
        <div className="pulse__band pulse__band--day" />
        <div className="pulse__band pulse__band--dusk" />
        {points.map((p) => (
          <div
            key={p.id}
            className="pulse__marker"
            style={{ left: `${p.progress * 100}%`, '--marker-color': p.accent }}
            title={`${p.city} — ${p.hour.toString().padStart(2, '0')}:${p.minute.toString().padStart(2, '0')}`}
          >
            <span className="pulse__dot" />
            <span className="pulse__marker-label">{p.city}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
