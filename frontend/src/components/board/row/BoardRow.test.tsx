import { render, screen } from '@testing-library/react'
import BoardRow from './BoardRow'
import { makeCard, makeBoardRow } from '@/test/factories'

vi.mock('../card/Card', () => ({
  default: ({ card }: { card?: { id: string; name: string } }) =>
    card ? <div data-testid={`card-${card.id}`}>{card.name}</div> : <div data-testid="card-back" />,
}))

vi.mock('@/components/ui/CountBadge', () => ({
  default: ({ value }: { value: number }) => <div data-testid="score-badge">{value}</div>,
}))

const defaultProps = {
  rowLabel: 'Melee',
  rowType: 'MELEE' as const,
  side: 'player' as const,
  interactive: false,
}

describe('BoardRow', () => {
  it('renders all cards from row.cards', () => {
    const cards = [makeCard({ id: 'c1', name: 'A' }), makeCard({ id: 'c2', name: 'B' })]
    const row = makeBoardRow({ cards })
    render(<BoardRow row={row} {...defaultProps} />)

    expect(screen.getByTestId('card-c1')).toBeInTheDocument()
    expect(screen.getByTestId('card-c2')).toBeInTheDocument()
  })

  it('shows score badge with sum of currentPower values', () => {
    const cards = [
      makeCard({ id: 'c1', currentPower: 3, basePower: 3 }),
      makeCard({ id: 'c2', currentPower: 7, basePower: 7 }),
    ]
    const row = makeBoardRow({ cards })
    render(<BoardRow row={row} {...defaultProps} />)

    expect(screen.getByTestId('score-badge')).toHaveTextContent('10')
  })

  it('shows horn icon when hornActive is true', () => {
    const row = makeBoardRow({ hornActive: true })
    const { container } = render(<BoardRow row={row} {...defaultProps} />)
    expect(container.querySelector('.board-row__horn--active')).toBeInTheDocument()
  })

  it('applies weather class based on rowType when weatherActive', () => {
    const row = makeBoardRow({ weatherActive: true })

    const { container: meleeContainer } = render(<BoardRow row={row} {...defaultProps} rowType="MELEE" />)
    expect(meleeContainer.querySelector('.board-row--frost')).toBeInTheDocument()

    const { container: rangedContainer } = render(<BoardRow row={row} {...defaultProps} rowType="RANGED" />)
    expect(rangedContainer.querySelector('.board-row--fog')).toBeInTheDocument()

    const { container: siegeContainer } = render(<BoardRow row={row} {...defaultProps} rowType="SIEGE" />)
    expect(siegeContainer.querySelector('.board-row--rain')).toBeInTheDocument()
  })

  it('adds board-row--target class when isPlacementTarget is true', () => {
    const row = makeBoardRow()
    const { container } = render(<BoardRow row={row} {...defaultProps} isPlacementTarget />)
    expect(container.querySelector('.board-row--target')).toBeInTheDocument()
  })
})
