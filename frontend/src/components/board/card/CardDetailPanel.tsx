import type { CardDto } from '@/types/game'
import { factionTokens } from './CardBack'
import CardArtImage from './CardArtImage'
import PowerGem from './PowerGem'
import RowIcon from './RowIcon'
import AbilityIcon from './AbilityIcon'

interface CardDetailAction {
  label: string
  onClick: () => void
  disabled?: boolean
}

interface CardDetailPanelProps {
  card: CardDto
  onClose: () => void
  action?: CardDetailAction
}

const abilityDescriptions: Record<string, string> = {
  SPY: 'Jogada no lado inimigo. Compre 2 cartas do seu baralho.',
  TIGHT_BOND: 'Dobra a forca quando ao lado de cartas com o mesmo nome.',
  MORALE_BOOST: 'Adiciona +1 de forca a todas as unidades na mesma fileira.',
  MEDIC: 'Ao ser jogada, ressuscite uma carta do cemiterio.',
  MUSTER: 'Convoca todas as cartas com o mesmo nome do baralho.',
  SCORCH: 'Destroi a(s) carta(s) com maior forca no campo.',
  DUMMY: 'Substitui uma carta no campo, devolvendo-a a mao.',
  COMMANDERS_HORN: 'Dobra a forca de todas as unidades na fileira.',
  BERSERKER: 'Transforma-se quando afetado por clima.',
  AGILE: 'Pode ser jogada na fileira Corpo a Corpo ou Distancia.',
  FROST: 'Reduz todas as unidades nas fileiras Corpo a Corpo a 1 de forca.',
  FOG: 'Reduz todas as unidades nas fileiras Distancia a 1 de forca.',
  RAIN: 'Reduz todas as unidades nas fileiras Cerco a 1 de forca.',
  CLEAR_WEATHER: 'Remove todos os efeitos climaticos do campo.',
}

const abilityNames: Record<string, string> = {
  SPY: 'Espiao',
  TIGHT_BOND: 'Vinculo',
  MORALE_BOOST: 'Moral',
  MEDIC: 'Medico',
  MUSTER: 'Convocacao',
  SCORCH: 'Chamuscar',
  DUMMY: 'Isca',
  COMMANDERS_HORN: 'Corneta',
  BERSERKER: 'Berserker',
  AGILE: 'Agil',
  FROST: 'Geada',
  FOG: 'Nevoa',
  RAIN: 'Chuva',
  CLEAR_WEATHER: 'Limpar Clima',
}

const cardTypeNames: Record<string, string> = {
  UNIT: 'Unidade',
  HERO: 'Heroi',
  WEATHER: 'Clima',
  SPECIAL: 'Especial',
  LEADER: 'Lider',
}

const leaderAbilityNames: Record<string, string> = {
  SIEGE_MASTER: 'Mestre de Cerco',
  SON_OF_MEDELL: 'Filho de Medell',
  KING_OF_TEMERIA: 'Rei da Temeria',
  LORD_COMMANDER: 'Lorde Comandante',
  STEEL_FORGED: 'Forjado em Aco',
  EMPEROR_OF_NILFGAARD: 'Imperador de Nilfgaard',
  INVADER_OF_THE_NORTH: 'Invasor do Norte',
  RELENTLESS: 'Implacavel',
  WHITE_FLAME: 'Chama Branca',
  IMPERIAL_MAJESTY: 'Majestade Imperial',
  BRINGER_OF_DEATH: 'Portador da Morte',
  COMMANDER_OF_THE_RED_RIDERS: 'Comandante dos Cavaleiros Vermelhos',
  DESTROYER_OF_WORLDS: 'Destruidor de Mundos',
  KING_OF_THE_WILD_HUNT: 'Rei da Caçada Selvagem',
  TREACHEROUS: 'Traicoeiro',
  QUEEN_OF_DOL_BLATHANNA: 'Rainha de Dol Blathanna',
  DAISY_OF_THE_VALLEY: 'Margarida do Vale',
  PUREBLOOD_ELF: 'Elfo Puro-Sangue',
  THE_BEATIFUL: 'A Bela',
  HOPE_OF_THE_AEN_SEIDHE: 'Esperanca dos Aen Seidhe',
  KING_BRAN: 'Rei Bran',
  CLAN_AN_CRAITE: 'Cla an Craite',
}

const leaderAbilityDescriptions: Record<string, string> = {
  SIEGE_MASTER: 'Dobra a forca de todas as unidades na fileira de Cerco.',
  SON_OF_MEDELL: 'Destroi a fileira de Distancia inimiga se tiver 10+ de forca.',
  KING_OF_TEMERIA: 'Escolha uma carta de Nevoa do seu baralho e jogue-a instantaneamente.',
  LORD_COMMANDER: 'Remove todos os efeitos climaticos do campo.',
  STEEL_FORGED: 'Destroi a fileira de Cerco inimiga se tiver 10+ de forca.',
  EMPEROR_OF_NILFGAARD: 'Revela 3 cartas aleatorias da mao do oponente.',
  INVADER_OF_THE_NORTH: 'Ressuscita uma carta do seu cemiterio.',
  RELENTLESS: 'Escolha uma carta do cemiterio do oponente e adicione-a a sua mao.',
  WHITE_FLAME: 'Cancela a habilidade do lider adversario.',
  IMPERIAL_MAJESTY: 'Escolha uma carta de Chuva do seu baralho e jogue-a instantaneamente.',
  BRINGER_OF_DEATH: 'Restaura uma carta do seu cemiterio para sua mao.',
  COMMANDER_OF_THE_RED_RIDERS: 'Dobra a forca de todas as unidades na fileira Corpo a Corpo.',
  DESTROYER_OF_WORLDS: 'Descarte 2 cartas da mao e compre 1 carta do baralho.',
  KING_OF_THE_WILD_HUNT: 'Escolha qualquer carta de clima do seu baralho e jogue-a instantaneamente.',
  TREACHEROUS: 'Dobra a forca de todas as cartas Espiao no campo.',
  QUEEN_OF_DOL_BLATHANNA: 'Destroi a fileira Corpo a Corpo inimiga se tiver 10+ de forca.',
  DAISY_OF_THE_VALLEY: 'Compre uma carta extra no inicio da batalha.',
  PUREBLOOD_ELF: 'Escolha uma carta de Geada do seu baralho e jogue-a instantaneamente.',
  THE_BEATIFUL: 'Dobra a forca das cartas de Distancia (se nao tiver Corneta).',
  HOPE_OF_THE_AEN_SEIDHE: 'Move unidades ageis para a fileira que maximiza sua forca.',
  KING_BRAN: 'Embaralha o cemiterio de volta ao baralho.',
  CLAN_AN_CRAITE: 'Embaralha o cemiterio de volta ao baralho.',
}

export default function CardDetailPanel({ card, action }: CardDetailPanelProps) {
  const isHero = card.cardType === 'HERO'
  const isUnit = card.cardType === 'UNIT' || isHero
  const hasPower = isUnit && card.basePower != null

  const tokens = factionTokens[card.faction]
  const panelVars = tokens
    ? { '--cdp-primary': tokens.primary, '--cdp-secondary': tokens.secondary } as React.CSSProperties
    : undefined

  const panelClass = [
    'card-detail-panel',
    isHero && 'card-detail-panel--hero',
  ]
    .filter(Boolean)
    .join(' ')

  const isLeader = !!card.leaderAbility
  const ability = card.ability && card.ability !== 'NONE' ? card.ability : null
  const description = isLeader
    ? leaderAbilityDescriptions[card.leaderAbility!]
    : ability ? abilityDescriptions[ability] : null

  return (
    <div className={panelClass} style={panelVars}>
      <div className="card-detail-art">
        <CardArtImage cardId={card.id} faction={card.faction} />
        {hasPower && (
          <PowerGem
            basePower={card.basePower!}
            currentPower={card.currentPower ?? card.basePower!}
            isHero={isHero}
            size="lg"
          />
        )}

        {isUnit && card.rowType && (
          <div className="card-detail-row-container">
            <RowIcon rowType={card.rowType} size="lg" />
            {card.ability === 'AGILE' && card.rowType === 'MELEE' && (
              <div className="card-detail-row-secondary">
                <RowIcon rowType="RANGED" size="lg" />
              </div>
            )}
          </div>
        )}

        {ability && !isLeader && (
          <div className="absolute top-2 right-2 z-[3] flex flex-col items-center gap-1">
            <AbilityIcon ability={ability} size="lg" />
            <span className="card-detail-ability-label">
              {abilityNames[ability]}
            </span>
          </div>
        )}

        {isLeader && (
          <div className="absolute top-2 right-2 z-[3] flex flex-col items-center gap-1">
            <span className="card-detail-ability-label">
              {leaderAbilityNames[card.leaderAbility!]}
            </span>
          </div>
        )}
      </div>

      <div className="card-detail-info">
        <span className="font-heading text-sm leading-[1.2] text-gold-light text-center">{card.name}</span>
        <span className="font-ui text-[10px] uppercase tracking-[1px] text-text-muted">{cardTypeNames[card.cardType]}</span>
        {description && (
          <>
            <div className="h-px w-4/5 bg-gold-dim opacity-50 self-center" />
            <p className="font-body text-[11px] text-text-secondary text-center leading-[1.4] m-0">{description}</p>
          </>
        )}
        {action && (
          <button
            className="card-detail-action-btn"
            onClick={action.onClick}
            disabled={action.disabled}
          >
            {action.label}
          </button>
        )}
      </div>
    </div>
  )
}
