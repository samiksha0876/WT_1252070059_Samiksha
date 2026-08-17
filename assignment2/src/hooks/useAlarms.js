import { useCallback, useEffect, useRef, useState } from 'react'
import { getZonedTimeParts } from '../utils/timezones'

const STORAGE_KEY = 'meridian.alarms'

function loadAlarms() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

// Alarms are evaluated per-second against each alarm's OWN timezone (not
// the browser's local time), so an alarm set for 07:00 Tokyo fires when
// Tokyo hits 07:00, regardless of where the viewer is.
export function useAlarms(now) {
  const [alarms, setAlarms] = useState(loadAlarms)
  const [ringing, setRinging] = useState(null)
  const firedTodayRef = useRef(new Set())

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(alarms))
  }, [alarms])

  useEffect(() => {
    alarms.forEach((alarm) => {
      if (!alarm.enabled) return
      const { hours, minutes, seconds } = getZonedTimeParts(now, alarm.timezone)
      const key = `${alarm.id}-${now.toDateString()}-${hours}:${minutes}`
      const matches = hours === alarm.hour && minutes === alarm.minute && seconds === 0
      if (matches && !firedTodayRef.current.has(key)) {
        firedTodayRef.current.add(key)
        setRinging(alarm)
      }
    })
  }, [now, alarms])

  const addAlarm = useCallback((alarm) => {
    setAlarms((prev) => [...prev, { ...alarm, id: crypto.randomUUID(), enabled: true }])
  }, [])

  const toggleAlarm = useCallback((id) => {
    setAlarms((prev) => prev.map((a) => (a.id === id ? { ...a, enabled: !a.enabled } : a)))
  }, [])

  const removeAlarm = useCallback((id) => {
    setAlarms((prev) => prev.filter((a) => a.id !== id))
  }, [])

  const dismissRinging = useCallback(() => setRinging(null), [])

  return { alarms, addAlarm, toggleAlarm, removeAlarm, ringing, dismissRinging }
}
