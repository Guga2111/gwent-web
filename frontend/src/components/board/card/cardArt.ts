const factionFolder: Record<string, string> = {
  NORTHERN_REALMS: 'northern',
  NILFGAARD: 'nilfgaard',
  MONSTER: 'monster',
  SCOIATAEL: 'scoiatael',
  SKELLIGE: 'skellige',
  NEUTRAL: 'neutral',
}

/** Strip instance suffix (_1, _2, etc.) but keep _TRANSFORMED for bear art */
function catalogId(instanceId: string): string {
  const isTransformed = instanceId.endsWith('_TRANSFORMED')
  const base = instanceId.replace(/_TRANSFORMED$/, '').replace(/_\d+$/, '')
  return isTransformed ? `${base}_TRANSFORMED` : base
}

export function getCardArtUrl(id: string, faction: string): string {
  const folder = factionFolder[faction] ?? 'neutral'
  return `/cards/${folder}/${catalogId(id)}.webp`
}
