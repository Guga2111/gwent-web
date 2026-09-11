import { render, screen } from '@testing-library/react'
import PlayerPanel from './PlayerPanel'
import { makePlayerState, makeOpponentState, makeCard } from '@/test/factories'

vi.mock('@/components/ui/CountBadge', () => ({
  default: ({ value }: { value: number }) => <div data-testid="score-badge">{value}</div>,
}))

vi.mock('@/icons/faction-emblems', () => ({
  NorthernRealmsEmblem: () => <svg data-testid="emblem-northern" />,
  NilfgaardEmblem: () => <svg data-testid="emblem-nilfgaard" />,
  MonstersEmblem: () => <svg data-testid="emblem-monsters" />,
  ScoiataelEmblem: () => <svg data-testid="emblem-scoiatael" />,
  SkelligeEmblem: () => <svg data-testid="emblem-skellige" />,
}))

describe('PlayerPanel', () => {
  it('shows username (email split at @)', () => {
    const player = makePlayerState({ playerId: 'geralt@rivia.com' })
    render(<PlayerPanel player={player} isActive={false} side="bottom" />)
    expect(screen.getByText('geralt')).toBeInTheDocument()
  })

  it('shows faction name', () => {
    const player = makePlayerState({
      leader: makeCard({ faction: 'NORTHERN_REALMS', cardType: 'LEADER', basePower: null, currentPower: null }),
    })
    render(<PlayerPanel player={player} isActive={false} side="bottom" />)
    expect(screen.getByText('Reinos do Norte')).toBeInTheDocument()
  })

  it('shows PASSOU when passed is true', () => {
    const player = makePlayerState({ passed: true })
    render(<PlayerPanel player={player} isActive={false} side="bottom" />)
    expect(screen.getByText('PASSOU')).toBeInTheDocument()
  })

  it('does not show PASSOU when passed is false', () => {
    const player = makePlayerState({ passed: false })
    render(<PlayerPanel player={player} isActive={false} side="bottom" />)
    expect(screen.queryByText('PASSOU')).not.toBeInTheDocument()
  })

  it('renders correct life gems (alive vs lost)', () => {
    const player = makePlayerState({ lives: 1 })
    const { container } = render(<PlayerPanel player={player} isActive={false} side="bottom" />)
    const alive = container.querySelectorAll('.player-panel__gem--alive')
    const lost = container.querySelectorAll('.player-panel__gem--lost')
    expect(alive).toHaveLength(1)
    expect(lost).toHaveLength(1)
  })

  it('shows hand count from hand.length for PlayerStateDto', () => {
    const hand = [makeCard({ id: 'h1' }), makeCard({ id: 'h2' }), makeCard({ id: 'h3' })]
    const player = makePlayerState({ hand })
    render(<PlayerPanel player={player} isActive={false} side="bottom" />)
    expect(screen.getByText('3')).toBeInTheDocument()
  })

  it('shows hand count from handSize for OpponentStateDto', () => {
    const opponent = makeOpponentState({ handSize: 7 })
    render(<PlayerPanel player={opponent} isActive={false} side="top" />)
    expect(screen.getByText('7')).toBeInTheDocument()
  })

  it('adds player-panel--active class when isActive is true', () => {
    const player = makePlayerState()
    const { container } = render(<PlayerPanel player={player} isActive={true} side="bottom" />)
    expect(container.querySelector('.player-panel--active')).toBeInTheDocument()
  })

  it('does not add player-panel--active class when isActive is false', () => {
    const player = makePlayerState()
    const { container } = render(<PlayerPanel player={player} isActive={false} side="bottom" />)
    expect(container.querySelector('.player-panel--active')).not.toBeInTheDocument()
  })
})
