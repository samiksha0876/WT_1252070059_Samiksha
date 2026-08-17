import { useMemo } from 'react'
import AnalogClock from './AnalogClock'
import DigitalClock from './DigitalClock'
import { useNow } from '../context/TimeContext'
import { getZonedTimeParts, getDayProgress, isDaytime } from '../utils/timezones'

const ACCENTS = ['#4FD1C5', '#F2A65A', '#8CA5F2', '#E08AC0']

function accentFor(id) {
  let hash = 0
  for (let i = 0; i < id.length; i++) hash = (hash * 31 + id.charCodeAt(i)) >>> 0
  return ACCENTS[hash % ACCENTS.length]
}

export default function ClockCard({ region, use24h, showAnalog, onRemove, alarmCount }) {
  const now = useNow()

  const parts = useMemo(() => getZonedTimeParts(now, region.id), [now, region.id])
  const dayProgress = useMemo(() => getDayProgress(parts), [parts])
  const daytime = isDaytime(dayProgress)
  const accent = useMemo(() => accentFor(region.id), [region.id])

  return (
    <article className={`clock-card ${daytime ? 'clock-card--day' : 'clock-card--night'}`}>
      <div className="clock-card__strip" style={{ '--strip-pos': `${dayProgress * 100}%` }} />

      <header className="clock-card__header">
        <div>
          <h3 className="clock-card__city">{region.city}</h3>
          <p className="clock-card__meta">
            {region.country} · {parts.offsetLabel}
          </p>
        </div>
        <div className="clock-card__badges">
          {alarmCount > 0 && (
            <span className="badge badge--alarm" title={`${alarmCount} alarm(s)`}>
              ⏰ {alarmCount}
            </span>
          )}
          <span className={`badge badge--phase ${daytime ? 'badge--day' : 'badge--night'}`}>
            {daytime ? 'Day' : 'Night'}
          </span>
          <button className="icon-btn" onClick={() => onRemove(region.id)} aria-label={`Remove ${region.city}`}>
            ×
          </button>
        </div>
      </header>

      <div className="clock-card__body">
        {showAnalog && (
          <AnalogClock
            hours={parts.hours}
            minutes={parts.minutes}
            seconds={parts.seconds}
            dayProgress={dayProgress}
            accent={accent}
          />
        )}
        <div className="clock-card__digital-wrap">
          <DigitalClock hours={parts.hours} minutes={parts.minutes} seconds={parts.seconds} use24h={use24h} />
          <p className="clock-card__date">{parts.dateLabel}</p>
        </div>
      </div>
    </article>
  )
}
