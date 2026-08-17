import { memo } from 'react'

const SIZE = 168
const CENTER = SIZE / 2
const FACE_R = 74

function polarToCartesian(radius, angleDeg) {
  const rad = ((angleDeg - 90) * Math.PI) / 180
  return {
    x: CENTER + radius * Math.cos(rad),
    y: CENTER + radius * Math.sin(rad),
  }
}

function describeArc(radius, startAngle, endAngle) {
  const start = polarToCartesian(radius, endAngle)
  const end = polarToCartesian(radius, startAngle)
  const largeArc = endAngle - startAngle <= 180 ? 0 : 1
  return `M ${start.x} ${start.y} A ${radius} ${radius} 0 ${largeArc} 0 ${end.x} ${end.y}`
}

function AnalogClock({ hours, minutes, seconds, dayProgress, accent }) {
  const hourAngle = ((hours % 12) + minutes / 60) * 30
  const minuteAngle = (minutes + seconds / 60) * 6
  const secondAngle = seconds * 6
  const progressAngle = dayProgress * 360

  const ticks = Array.from({ length: 60 }, (_, i) => {
    const isHour = i % 5 === 0
    const inner = isHour ? FACE_R - 9 : FACE_R - 5
    const outer = FACE_R - 2
    const p1 = polarToCartesian(inner, i * 6)
    const p2 = polarToCartesian(outer, i * 6)
    return (
      <line
        key={i}
        x1={p1.x}
        y1={p1.y}
        x2={p2.x}
        y2={p2.y}
        className={isHour ? 'tick tick--hour' : 'tick tick--minute'}
      />
    )
  })

  const hourTip = polarToCartesian(38, hourAngle)
  const minuteTip = polarToCartesian(54, minuteAngle)
  const secondTip = polarToCartesian(62, secondAngle)

  return (
    <svg
      className="analog-clock"
      viewBox={`0 0 ${SIZE} ${SIZE}`}
      width={SIZE}
      height={SIZE}
      role="img"
      aria-label={`Analog clock showing ${hours}:${minutes.toString().padStart(2, '0')}`}
    >
      {/* Day-progress arc: the dashboard's signature detail. Traces how far
          this region has moved through its day, so you can read "is it
          morning or night there" at a glance without reading numbers. */}
      <circle cx={CENTER} cy={CENTER} r={FACE_R + 10} className="progress-track" />
      <path
        d={describeArc(FACE_R + 10, 0, Math.max(progressAngle, 0.001))}
        className="progress-arc"
        style={{ stroke: accent }}
      />

      <circle cx={CENTER} cy={CENTER} r={FACE_R} className="face" />
      {ticks}

      <line
        x1={CENTER}
        y1={CENTER}
        x2={hourTip.x}
        y2={hourTip.y}
        className="hand hand--hour"
      />
      <line
        x1={CENTER}
        y1={CENTER}
        x2={minuteTip.x}
        y2={minuteTip.y}
        className="hand hand--minute"
      />
      <line
        x1={CENTER}
        y1={CENTER}
        x2={secondTip.x}
        y2={secondTip.y}
        className="hand hand--second"
        style={{ stroke: accent }}
      />
      <circle cx={CENTER} cy={CENTER} r={4} className="hub" style={{ fill: accent }} />
    </svg>
  )
}

export default memo(AnalogClock)
