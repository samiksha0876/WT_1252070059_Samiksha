// Curated list of representative regions. Uses IANA timezone identifiers so
// Intl.DateTimeFormat handles DST and offset math correctly — no hand-rolled
// UTC offset arithmetic, which is where most world-clock bugs come from.
export const TIMEZONE_CATALOG = [
  { id: 'America/New_York', city: 'New York', country: 'USA' },
  { id: 'America/Los_Angeles', city: 'Los Angeles', country: 'USA' },
  { id: 'America/Chicago', city: 'Chicago', country: 'USA' },
  { id: 'America/Sao_Paulo', city: 'São Paulo', country: 'Brazil' },
  { id: 'Europe/London', city: 'London', country: 'UK' },
  { id: 'Europe/Paris', city: 'Paris', country: 'France' },
  { id: 'Europe/Berlin', city: 'Berlin', country: 'Germany' },
  { id: 'Europe/Moscow', city: 'Moscow', country: 'Russia' },
  { id: 'Africa/Cairo', city: 'Cairo', country: 'Egypt' },
  { id: 'Africa/Johannesburg', city: 'Johannesburg', country: 'South Africa' },
  { id: 'Asia/Dubai', city: 'Dubai', country: 'UAE' },
  { id: 'Asia/Kolkata', city: 'Mumbai', country: 'India' },
  { id: 'Asia/Shanghai', city: 'Shanghai', country: 'China' },
  { id: 'Asia/Tokyo', city: 'Tokyo', country: 'Japan' },
  { id: 'Asia/Singapore', city: 'Singapore', country: 'Singapore' },
  { id: 'Australia/Sydney', city: 'Sydney', country: 'Australia' },
  { id: 'Pacific/Auckland', city: 'Auckland', country: 'New Zealand' },
]

export function getDefaultRegions() {
  return ['America/New_York', 'Europe/London', 'Asia/Tokyo', 'Asia/Kolkata']
}

// Returns { hours, minutes, seconds, dateLabel, offsetLabel } for a given
// IANA timezone at the given instant, using Intl so DST is always correct.
export function getZonedTimeParts(date, timeZone) {
  const dtf = new Intl.DateTimeFormat('en-US', {
    timeZone,
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    weekday: 'short',
    day: '2-digit',
    month: 'short',
  })
  const parts = Object.fromEntries(dtf.formatToParts(date).map((p) => [p.type, p.value]))

  const hours = parseInt(parts.hour, 10) % 24
  const minutes = parseInt(parts.minute, 10)
  const seconds = parseInt(parts.second, 10)

  const offsetFormatter = new Intl.DateTimeFormat('en-US', {
    timeZone,
    timeZoneName: 'shortOffset',
  })
  const offsetPart = offsetFormatter.formatToParts(date).find((p) => p.type === 'timeZoneName')
  const offsetLabel = offsetPart ? offsetPart.value.replace('GMT', 'UTC') : ''

  return {
    hours,
    minutes,
    seconds,
    dateLabel: `${parts.weekday}, ${parts.month} ${parts.day}`,
    offsetLabel,
  }
}

// 0 = midnight, 1 = solar noon. Used to drive the day/night gradient strip
// and card ambience — the dashboard's signature detail.
export function getDayProgress({ hours, minutes, seconds }) {
  return (hours * 3600 + minutes * 60 + seconds) / 86400
}

export function isDaytime(progress) {
  // Rough daylight window; not astronomically precise, but reads correctly
  // for a dashboard-at-a-glance use case.
  return progress > 0.25 && progress < 0.79
}
