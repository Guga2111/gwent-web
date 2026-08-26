import { Swords } from 'lucide-react'

interface CentralDividerProps {
  turnRemainingPct?: number | null;
  isMyTurn?: boolean;
  isUrgent?: boolean;
}

export default function CentralDivider({ turnRemainingPct, isMyTurn, isUrgent }: CentralDividerProps) {
  let timerBarClass = 'central-divider__timer-bar';
  if (isUrgent && isMyTurn) {
    timerBarClass += ' central-divider__timer-bar--urgent';
  } else if (isMyTurn) {
    timerBarClass += ' central-divider__timer-bar--my-turn';
  } else {
    timerBarClass += ' central-divider__timer-bar--opponent-turn';
  }

  return (
    <div className="central-divider">
      <div className="central-divider__line" />
      {turnRemainingPct != null && (
        <div
          className={timerBarClass}
          style={{ width: `${turnRemainingPct}%` }}
        />
      )}
      <div className="central-divider__accent central-divider__accent--left" />
      <div className="central-divider__medallion">
        <Swords size={18} strokeWidth={1.5} />
      </div>
      <div className="central-divider__accent central-divider__accent--right" />
    </div>
  )
}
