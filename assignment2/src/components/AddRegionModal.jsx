import { useMemo, useState } from 'react'
import { TIMEZONE_CATALOG } from '../utils/timezones'

export default function AddRegionModal({ activeIds, onAdd, onClose }) {
  const [query, setQuery] = useState('')

  const results = useMemo(() => {
    const q = query.trim().toLowerCase()
    return TIMEZONE_CATALOG.filter((tz) => !activeIds.includes(tz.id)).filter(
      (tz) => !q || tz.city.toLowerCase().includes(q) || tz.country.toLowerCase().includes(q)
    )
  }, [query, activeIds])

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal__header">
          <h2>Add a region</h2>
          <button className="icon-btn" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>
        <input
          autoFocus
          className="modal__search"
          placeholder="Search city or country…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <ul className="modal__list">
          {results.length === 0 && <li className="modal__empty">No matching regions.</li>}
          {results.map((tz) => (
            <li key={tz.id}>
              <button
                className="modal__item"
                onClick={() => {
                  onAdd(tz)
                  onClose()
                }}
              >
                <span>{tz.city}</span>
                <span className="modal__item-country">{tz.country}</span>
              </button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
