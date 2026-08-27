import { useEffect, useState } from "react";

const TURN_DURATION_MS = 60_000;

interface TurnCountdown {
  remainingMs: number | null;
  remainingPct: number | null;
  remainingSeconds: number | null;
  isUrgent: boolean;
}

export function useTurnCountdown(turnDeadlineUtc: number | null): TurnCountdown {
  const [remainingMs, setRemainingMs] = useState<number | null>(null);

  useEffect(() => {
    if (turnDeadlineUtc === null) {
      setRemainingMs(null);
      return;
    }

    console.log('[turn-timer] deadline:', turnDeadlineUtc);

    const tick = () => {
      const ms = Math.max(0, Math.min(TURN_DURATION_MS, turnDeadlineUtc - Date.now()));
      setRemainingMs(ms);
    };

    tick();
    const id = setInterval(tick, 100);
    return () => clearInterval(id);
  }, [turnDeadlineUtc]);

  if (remainingMs === null) {
    return { remainingMs: null, remainingPct: null, remainingSeconds: null, isUrgent: false };
  }

  const remainingPct = (remainingMs / TURN_DURATION_MS) * 100;
  const remainingSeconds = Math.ceil(remainingMs / 1000);
  const isUrgent = remainingSeconds <= 10;

  return { remainingMs, remainingPct, remainingSeconds, isUrgent };
}
