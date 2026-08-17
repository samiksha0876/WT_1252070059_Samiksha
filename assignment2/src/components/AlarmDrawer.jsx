import { useState } from 'react'
import { TIMEZONE_CATALOG } from '../utils/timezones'

export default function AlarmDrawer({ alarms, onAdd, onToggle, onRemove, onClose }) {
  const [timezone, setTimezone] = useState(TIMEZONE_CATALOG[0].id)
  const [hour, setHour] = useState(7)
  const [minute, setMinute] = useState(0)
  const [label, setLabel] = useState('')

  const submit = (e) => {
    e.preventDefault()
    onAdd({
      timezone,
      hour: Number(hour),
      minute: Number(minute),
      label: label.trim() || 'Alarm',
    })
    setLabel('')
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal modal--drawer" onClick={(e) => e.stopPropagation()}>
        <div className="modal__header">
          <h2>Alarms</h2>
          <button className="icon-btn" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>

        <form className="alarm-form" onSubmit={submit}>
          <div className="alarm-form__row">
            <label>
              Region
              <select value={timezone} onChange={(e) => setTimezone(e.target.value)}>
                {TIMEZONE_CATALOG.map((tz) => (
                  <option key={tz.id} value={tz.id}>
                    {tz.city}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <div className="alarm-form__row alarm-form__row--time">
            <label>
              Hour
              <input type="number" min="0" max="23" value={hour} onChange={(e) => setHour(e.target.value)} />
            </label>
            <label>
              Minute
              <input type="number" min="0" max="59" value={minute} onChange={(e) => setMinute(e.target.value)} />
            </label>
            <label className="alarm-form__label-field">
              Label
              <input
                type="text"
                placeholder="Wake up"
                value={label}
                onChange={(e) => setLabel(e.target.value)}
                maxLength={30}
              />
            </label>
          </div>
          <button type="submit" className="btn btn--primary">
            Set alarm
          </button>
        </form>

        <ul className="alarm-list">
          {alarms.length === 0 && <li className="modal__empty">No alarms set yet.</li>}
          {alarms.map((alarm) => {
            const tz = TIMEZONE_CATALOG.find((t) => t.id === alarm.timezone)
            return (
              <li key={alarm.id} className={`alarm-list__item ${alarm.enabled ? '' : 'alarm-list__item--off'}`}>
                <div>
                  <p className="alarm-list__time">
                    {alarm.hour.toString().padStart(2, '0')}:{alarm.minute.toString().padStart(2, '0')}
                  </p>
                  <p className="alarm-list__meta">
                    {alarm.label} · {tz?.city ?? alarm.timezone}
                  </p>
                </div>
                <div className="alarm-list__actions">
                  <button className="toggle" data-on={alarm.enabled} onClick={() => onToggle(alarm.id)}>
                    <span className="toggle__thumb" />
                  </button>
                  <button className="icon-btn" onClick={() => onRemove(alarm.id)} aria-label="Delete alarm">
                    ×
                  </button>
                </div>
              </li>
            )
          })}
        </ul>
      </div>
    </div>
  )
}
