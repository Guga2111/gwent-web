import { Swords } from 'lucide-react'

export default function CentralDivider() {
  return (
    <div className="central-divider">
      <div className="central-divider__line" />
      <div className="central-divider__accent central-divider__accent--left" />
      <div className="central-divider__medallion">
        <Swords size={18} strokeWidth={1.5} />
      </div>
      <div className="central-divider__accent central-divider__accent--right" />
    </div>
  )
}
