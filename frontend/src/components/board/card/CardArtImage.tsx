import { useState } from 'react'
import { getCardArtUrl } from './cardArt'

interface Props {
  cardId: string
  faction: string
}

export default function CardArtImage({ cardId, faction }: Props) {
  const [failedId, setFailedId] = useState<string | null>(null)
  if (failedId === cardId) return null

  return (
    <img
      src={getCardArtUrl(cardId, faction)}
      alt=""
      onError={() => setFailedId(cardId)}
      draggable={false}
      className="card-art-img"
    />
  )
}
