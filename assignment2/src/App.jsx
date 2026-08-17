import { useEffect, useMemo, useState } from 'react'
import { TimeProvider, useNow } from './context/TimeContext'
import ClockCard from './components/ClockCard'
import AddRegionModal from './components/AddRegionModal'
import AlarmDrawer from './components/AlarmDrawer'
import { useAlarms } from './hooks/useAlarms'
import { TIMEZONE_CATALOG, getDefaultRegions } from './utils/timezones'

const REGIONS_KEY = 'meridian.regions'

function loadRegionIds() {
  try {
    const raw = localStorage.getItem(REGIONS_KEY)
    return raw ? JSON.parse(raw) : getDefaultRegions()
  } catch {
    return getDefaultRegions()
  }
}

function DashboardHeader({
  use24h,
  setUse24h,
  showAnalog,
  setShowAnalog,
  onAddRegion,
  onOpenAlarms,
  alarmCount,
  localLabel,
}) {
  return (
    <header className="dashboard-header">
      <div className="dashboard-header__brand">
        <span className="dashboard-header__mark" aria-hidden="true" />
        <div>
          <h1>Meridian</h1>
          <p>World clock dashboard · {localLabel}</p>
        </div>
      </div>

      <div className="dashboard-header__controls">
        <div className="control-group" role="group" aria-label="Display options">
          <button
            className={`chip ${showAnalog ? 'chip--active' : ''}`}
            onClick={() => setShowAnalog((v) => !v)}
          >
            Analog
          </button>
          <button className={`chip ${use24h ? 'chip--active' : ''}`} onClick={() => setUse24h((v) => !v)}>
            24h
          </button>
        </div>

        <button className="btn btn--ghost" onClick={onOpenAlarms}>
          Alarms{alarmCount > 0 ? ` (${alarmCount})` : ''}
        </button>
        <button className="btn btn--primary" onClick={onAddRegion}>
          + Add region
        </button>
      </div>
    </header>
  )
}

function RingingBanner({ alarm, onDismiss }) {
  if (!alarm) return null
  const tz = TIMEZONE_CATALOG.find((t) => t.id === alarm.timezone)
  return (
    <div className="ringing-banner" role="alert">
      <span className="ringing-banner__pulse" aria-hidden="true" />
      <div>
        <strong>{alarm.label}</strong>
        <span>
          {' '}
          — {alarm.hour.toString().padStart(2, '0')}:{alarm.minute.toString().padStart(2, '0')} in{' '}
          {tz?.city ?? alarm.timezone}
        </span>
      </div>
      <button className="btn btn--ghost" onClick={onDismiss}>
        Dismiss
      </button>
    </div>
  )
}

function DashboardBody() {
  const now = useNow()
  const [regionIds, setRegionIds] = useState(loadRegionIds)
  const [use24h, setUse24h] = useState(true)
  const [showAnalog, setShowAnalog] = useState(true)
  const [modal, setModal] = useState(null) // 'add' | 'alarms' | null

  const { alarms, addAlarm, toggleAlarm, removeAlarm, ringing, dismissRinging } = useAlarms(now)

  useEffect(() => {
    localStorage.setItem(REGIONS_KEY, JSON.stringify(regionIds))
  }, [regionIds])

  const regions = useMemo(
    () => regionIds.map((id) => TIMEZONE_CATALOG.find((tz) => tz.id === id)).filter(Boolean),
    [regionIds]
  )

  const alarmCountByRegion = useMemo(() => {
    const map = {}
    alarms.forEach((a) => {
      if (a.enabled) map[a.timezone] = (map[a.timezone] || 0) + 1
    })
    return map
  }, [alarms])

  const localLabel = useMemo(
    () =>
      new Intl.DateTimeFormat('en-US', {
        weekday: 'long',
        hour: '2-digit',
        minute: '2-digit',
      }).format(now),
    [now]
  )

  const addRegion = (tz) => setRegionIds((prev) => (prev.includes(tz.id) ? prev : [...prev, tz.id]))
  const removeRegion = (id) => setRegionIds((prev) => prev.filter((r) => r !== id))

  return (
    <div className="dashboard">
      <DashboardHeader
        use24h={use24h}
        setUse24h={setUse24h}
        showAnalog={showAnalog}
        setShowAnalog={setShowAnalog}
        onAddRegion={() => setModal('add')}
        onOpenAlarms={() => setModal('alarms')}
        alarmCount={alarms.filter((a) => a.enabled).length}
        localLabel={localLabel}
      />

      <RingingBanner alarm={ringing} onDismiss={dismissRinging} />

      {regions.length === 0 ? (
        <div className="empty-state">
          <p>No regions yet. Add one to start tracking the day as it moves around the world.</p>
          <button className="btn btn--primary" onClick={() => setModal('add')}>
            + Add your first region
          </button>
        </div>
      ) : (
        <section className="clock-grid">
          {regions.map((region) => (
            <ClockCard
              key={region.id}
              region={region}
              use24h={use24h}
              showAnalog={showAnalog}
              onRemove={removeRegion}
              alarmCount={alarmCountByRegion[region.id] || 0}
            />
          ))}
        </section>
      )}

      {modal === 'add' && (
        <AddRegionModal activeIds={regionIds} onAdd={addRegion} onClose={() => setModal(null)} />
      )}
      {modal === 'alarms' && (
        <AlarmDrawer
          alarms={alarms}
          onAdd={addAlarm}
          onToggle={toggleAlarm}
          onRemove={removeAlarm}
          onClose={() => setModal(null)}
        />
      )}
    </div>
  )
}

export default function App() {
  return (
    <TimeProvider>
      <DashboardBody />
    </TimeProvider>
  )
}
