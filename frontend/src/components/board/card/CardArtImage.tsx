import { useState } from 'react'
import { getCardArtUrl } from './cardArt'

interface Props {
  cardId: string
  faction: string
}

export default function CardArtImage({ cardId, faction }: Props) {
  const [failed, setFailed] = useState(false)
  if (failed) return null

  return (
    <img
      src={getCardArtUrl(cardId, faction)}
      alt=""
      onError={() => setFailed(true)}
      draggable={false}
      className="card-art-img"
    />
  )
}
