import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Card from './Card'
import { makeCard } from '@/test/factories'

vi.mock('./CardArtImage', () => ({ default: () => <div data-testid="card-art" /> }))

describe('Card', () => {
  it('renders CardBack when no card prop', () => {
    const { container } = render(<Card />)
    expect(container.querySelector('.card-back')).toBeInTheDocument()
  })

  it('renders PowerGem and RowIcon for UNIT card', () => {
    const card = makeCard({ basePower: 5, currentPower: 5, cardType: 'UNIT', rowType: 'MELEE' })
    const { container } = render(<Card card={card} />)
    expect(container.querySelector('.card-power-gem')).toBeInTheDocument()
    expect(container.querySelector('.card-row-icon')).toBeInTheDocument()
  })

  it('adds card-face--hero class for HERO card', () => {
    const card = makeCard({ cardType: 'HERO' })
    const { container } = render(<Card card={card} />)
    expect(container.querySelector('.card-face--hero')).toBeInTheDocument()
  })

  it('does not render PowerGem for WEATHER card', () => {
    const card = makeCard({ cardType: 'WEATHER', basePower: null, currentPower: null, rowType: null })
    const { container } = render(<Card card={card} />)
    expect(container.querySelector('.card-power-gem')).not.toBeInTheDocument()
  })

  it('adds interactive class and fires click handler when interactive', async () => {
    const user = userEvent.setup()
    const handleClick = vi.fn()
    const card = makeCard()
    const { container } = render(<Card card={card} onClick={handleClick} interactive />)

    expect(container.querySelector('.card-face--interactive')).toBeInTheDocument()
    await user.click(container.querySelector('.card-face')!)
    expect(handleClick).toHaveBeenCalledOnce()
  })

  it('does not fire click handler when not interactive', async () => {
    const user = userEvent.setup()
    const handleClick = vi.fn()
    const card = makeCard()
    const { container } = render(<Card card={card} onClick={handleClick} interactive={false} />)

    expect(container.querySelector('.card-face--interactive')).not.toBeInTheDocument()
    await user.click(container.querySelector('.card-face')!)
    expect(handleClick).not.toHaveBeenCalled()
  })
})
