import { createContext, useContext, useEffect, useRef, useState } from 'react'

// Single ticking source of truth. Every clock reads from here instead of
// running its own setInterval — this is what keeps a dashboard with a
// dozen+ clocks from turning into a re-render storm.
const TimeContext = createContext(null)

export function TimeProvider({ children }) {
  const [now, setNow] = useState(() => new Date())
  const frameRef = useRef()
  const lastSecondRef = useRef(-1)

  useEffect(() => {
    const tick = () => {
      const d = new Date()
      // Only push a new Date object into state once per second — the
      // analog hands still animate smoothly via CSS transforms computed
      // from `now`, but we don't force a re-render every frame.
      if (d.getSeconds() !== lastSecondRef.current) {
        lastSecondRef.current = d.getSeconds()
        setNow(d)
      }
      frameRef.current = requestAnimationFrame(tick)
    }
    frameRef.current = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(frameRef.current)
  }, [])

  return <TimeContext.Provider value={now}>{children}</TimeContext.Provider>
}

export function useNow() {
  const ctx = useContext(TimeContext)
  if (!ctx) throw new Error('useNow must be used within a TimeProvider')
  return ctx
}
