import { memo } from 'react'

function pad(n) {
  return n.toString().padStart(2, '0')
}

function DigitalClock({ hours, minutes, seconds, use24h }) {
  let displayHour = hours
  let suffix = ''
  if (!use24h) {
    suffix = hours >= 12 ? 'PM' : 'AM'
    displayHour = hours % 12 || 12
  }

  return (
    <div className="digital-clock">
      <span className="digital-clock__time">
        {pad(displayHour)}:{pad(minutes)}
        <span className="digital-clock__seconds">:{pad(seconds)}</span>
      </span>
      {suffix && <span className="digital-clock__suffix">{suffix}</span>}
    </div>
  )
}

export default memo(DigitalClock)
