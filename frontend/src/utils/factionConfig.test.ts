import { getFactionConfig } from './factionConfig'
import type { Faction } from '@/types/deck'

describe('getFactionConfig', () => {
  const factions: { key: Faction; label: string }[] = [
    { key: 'NORTHERN_REALMS', label: 'Reinos do Norte' },
    { key: 'NILFGAARD', label: 'Nilfgaard' },
    { key: 'MONSTER', label: 'Monstros' },
    { key: 'SCOIATAEL', label: "Scoiata'el" },
    { key: 'SKELLIGE', label: 'Skellige' },
  ]

  it.each(factions)('$key returns correct label and has Emblem function', ({ key, label }) => {
    const config = getFactionConfig(key)
    expect(config.label).toBe(label)
    expect(config.tokens).toBeDefined()
    expect(config.tokens.primary).toBeTruthy()
    expect(typeof config.Emblem).toBe('function')
  })

  it('NEUTRAL has label Neutro and null Emblem', () => {
    const config = getFactionConfig('NEUTRAL')
    expect(config.label).toBe('Neutro')
    expect(config.Emblem).toBeNull()
  })

  it('null returns NEUTRAL config', () => {
    const config = getFactionConfig(null)
    expect(config.label).toBe('Neutro')
    expect(config.Emblem).toBeNull()
  })

  it('unknown faction returns NEUTRAL config', () => {
    const config = getFactionConfig('UNKNOWN' as Faction)
    expect(config.label).toBe('Neutro')
  })
})
