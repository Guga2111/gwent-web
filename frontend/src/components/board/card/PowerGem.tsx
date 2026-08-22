interface PowerGemProps {
  basePower: number
  currentPower: number
  isHero: boolean
  size?: 'sm' | 'lg'
}

export default function PowerGem({ basePower, currentPower, isHero, size = 'sm' }: PowerGemProps) {
  const isBuffed = currentPower > basePower
  const isDebuffed = currentPower < basePower
  const prefix = size === 'lg' ? 'card-detail-power-gem' : 'card-power-gem'

  const className = [
    prefix,
    isHero && `${prefix}--hero`,
    isBuffed && `${prefix}--buffed`,
    isDebuffed && `${prefix}--debuffed`,
  ]
    .filter(Boolean)
    .join(' ')

  return <div className={className}>{currentPower}</div>
}
