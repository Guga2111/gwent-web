interface TurnCountdownProps {
  remainingSeconds: number | null;
  isMyTurn: boolean;
}

export default function TurnCountdown({ remainingSeconds, isMyTurn }: TurnCountdownProps) {
  if (remainingSeconds === null || remainingSeconds > 10 || remainingSeconds <= 0 || !isMyTurn) {
    return null;
  }

  return (
    <div className="turn-countdown-number" key={remainingSeconds}>
      {remainingSeconds}
    </div>
  );
}
