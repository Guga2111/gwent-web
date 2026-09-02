import Card from "../card/Card";
import CountBadge from "@/components/ui/CountBadge";

interface GraveyardStackProps {
  count: number;
}

export default function GraveyardStack({ count }: GraveyardStackProps) {
  return (
    <div className="flex flex-col items-center gap-1">
      <div className="relative w-(--card-w) h-(--card-h)">
        {/* Depth layers */}
        {count > 2 && (
          <div className="stack-depth-layer" style={{ top: 3, left: 3 }} />
        )}
        {count > 1 && (
          <div className="stack-depth-layer" style={{ top: 1.5, left: 1.5 }} />
        )}
        {/* Main card */}
        <div className="relative z-10">
          <Card />
        </div>
        {/* Count badge */}
        <div className="absolute -top-1.5 -right-1.5 z-30">
          <CountBadge value={count} size={27} fontSize={14} />
        </div>
      </div>
      <div className="text-[9px] text-text-muted font-heading">Graveyard</div>
    </div>
  );
}
