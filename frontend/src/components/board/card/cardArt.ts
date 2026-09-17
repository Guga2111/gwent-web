const factionFolder: Record<string, string> = {
  NORTHERN_REALMS: 'northern',
  NILFGAARD: 'nilfgaard',
  MONSTER: 'monster',
  SCOIATAEL: 'scoiatael',
  SKELLIGE: 'skellige',
  NEUTRAL: 'neutral',
}

/** Strip instance suffix (_1, _2, etc.) and _TRANSFORMED from runtime card IDs */
function catalogId(instanceId: string): string {
  return instanceId.replace(/_TRANSFORMED$/, '').replace(/_\d+$/, '')
}

export function getCardArtUrl(id: string, faction: string): string {
  const folder = factionFolder[faction] ?? 'neutral'
  return `/cards/${folder}/${catalogId(id)}.webp`
}
