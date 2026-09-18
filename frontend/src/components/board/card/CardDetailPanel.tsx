import type { CardDto } from '@/types/game'
import { getFactionConfig } from '@/utils/factionConfig'
import CardArtImage from './CardArtImage'
import PowerGem from './PowerGem'
import RowIcon from './RowIcon'
import AbilityIcon from './AbilityIcon'
import { HorizontalSeparator } from '@/components/ui/horizontal-separator'

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
  TIGHT_BOND: 'Dobra a força quando ao lado de cartas com o mesmo nome.',
  MORALE_BOOST: 'Adiciona +1 de força à todas as unidades na mesma fileira.',
  MEDIC: 'Ao ser jogada, ressuscite uma carta do cemitério.',
  MUSTER: 'Convoca todas as cartas com o mesmo nome do baralho.',
  SCORCH: 'Destroi a(s) carta(s) com maior força no campo.',
  DUMMY: 'Substitui uma carta no campo, devolvendo-a a mão.',
  COMMANDERS_HORN: 'Dobra a força de todas as unidades na fileira.',
  BERSERKER: 'Transforma-se quando afetado por clima.',
  MARDROEME: 'Transforma Berserkers na fileira alvo em formas mais poderosas.',
  AGILE: 'Pode ser jogada na fileira Corpo a Corpo ou Distância.',
  FROST: 'Reduz todas as unidades nas fileiras Corpo a Corpo à 1 de forca.',
  FOG: 'Reduz todas as unidades nas fileiras Distância à 1 de forca.',
  RAIN: 'Reduz todas as unidades nas fileiras Cerco à 1 de forca.',
  CLEAR_WEATHER: 'Remove todos os efeitos climaticos do campo.',
}

const abilityNames: Record<string, string> = {
  SPY: 'Espião',
  TIGHT_BOND: 'Vínculo',
  MORALE_BOOST: 'Moral',
  MEDIC: 'Médico',
  MUSTER: 'Convocação',
  SCORCH: 'Chamuscar',
  DUMMY: 'Isca',
  COMMANDERS_HORN: 'Corneta',
  BERSERKER: 'Berserker',
  MARDROEME: 'Mardroeme',
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
  LEADER: 'Líder',
}

const leaderAbilityNames: Record<string, string> = {
  SIEGE_MASTER: 'Mestre de Cerco',
  SON_OF_MEDELL: 'Filho de Medell',
  KING_OF_TEMERIA: 'Rei da Teméria',
  LORD_COMMANDER: 'Lorde Comandante',
  STEEL_FORGED: 'Forjado em Aço',
  EMPEROR_OF_NILFGAARD: 'Imperador de Nilfgaard',
  INVADER_OF_THE_NORTH: 'Invasor do Norte',
  RELENTLESS: 'Implacável',
  WHITE_FLAME: 'Chama Branca',
  IMPERIAL_MAJESTY: 'Majestade Imperial',
  BRINGER_OF_DEATH: 'Portador da Morte',
  COMMANDER_OF_THE_RED_RIDERS: 'Comandante dos Cavaleiros Vermelhos',
  DESTROYER_OF_WORLDS: 'Destruidor de Mundos',
  KING_OF_THE_WILD_HUNT: 'Rei da Caçada Selvagem',
  TREACHEROUS: 'Traiçoeiro',
  QUEEN_OF_DOL_BLATHANNA: 'Rainha de Dol Blathanna',
  DAISY_OF_THE_VALLEY: 'Margarida do Vale',
  PUREBLOOD_ELF: 'Elfo Puro-Sangue',
  THE_BEATIFUL: 'A Bela',
  HOPE_OF_THE_AEN_SEIDHE: 'Esperança dos Aen Seidhe',
  KING_BRAN: 'Rei Bran',
  CLAN_AN_CRAITE: 'Clã an Craite',
}

const leaderAbilityDescriptions: Record<string, string> = {
  SIEGE_MASTER: 'Dobra a força de todas as unidades na fileira de Cerco.',
  SON_OF_MEDELL: 'Destroi a fileira de Distância inimiga se tiver 10+ de força.',
  KING_OF_TEMERIA: 'Escolha uma carta de Névoa do seu baralho e jogue-a instantaneamente.',
  LORD_COMMANDER: 'Remove todos os efeitos climáticos do campo.',
  STEEL_FORGED: 'Destroi a fileira de Cerco inimiga se tiver 10+ de força.',
  EMPEROR_OF_NILFGAARD: 'Revela 3 cartas aleatórias da mão do oponente.',
  INVADER_OF_THE_NORTH: 'Ressuscita uma carta do seu cemitério.',
  RELENTLESS: 'Escolha uma carta do cemitério do oponente e adicione-a a sua mão.',
  WHITE_FLAME: 'Cancela a habilidade do líder adversário.',
  IMPERIAL_MAJESTY: 'Escolha uma carta de Chuva do seu baralho e jogue-a instantaneamente.',
  BRINGER_OF_DEATH: 'Restaura uma carta do seu cemitério para sua mão.',
  COMMANDER_OF_THE_RED_RIDERS: 'Dobra a força de todas as unidades na fileira Corpo a Corpo.',
  DESTROYER_OF_WORLDS: 'Descarte 2 cartas da mão e compre 1 carta do baralho.',
  KING_OF_THE_WILD_HUNT: 'Escolha qualquer carta de clima do seu baralho e jogue-a instantaneamente.',
  TREACHEROUS: 'Dobra a força de todas as cartas Espião no campo.',
  QUEEN_OF_DOL_BLATHANNA: 'Destroi a fileira Corpo a Corpo inimiga se tiver 10+ de força.',
  DAISY_OF_THE_VALLEY: 'Compre uma carta extra no início da batalha.',
  PUREBLOOD_ELF: 'Escolha uma carta de Geada do seu baralho e jogue-a instantaneamente.',
  THE_BEATIFUL: 'Dobra a força das cartas de Distância (se não tiver Corneta).',
  HOPE_OF_THE_AEN_SEIDHE: 'Move unidades ageis para a fileira que maximiza sua força.',
  KING_BRAN: 'Unidades perdem apenas metade da força em condições de máu tempo.',
  CLAN_AN_CRAITE: 'Embaralha o cemiterio de volta ao baralho.',
}

export default function CardDetailPanel({ card, action }: CardDetailPanelProps) {
  const isHero = card.cardType === 'HERO'
  const isUnit = card.cardType === 'UNIT' || isHero
  const hasPower = isUnit && card.basePower != null

  const { tokens } = getFactionConfig(card.faction)
  const panelVars = tokens
    ? { '--cdp-primary': tokens.primary, '--cdp-secondary': tokens.secondary } as React.CSSProperties
    : undefined

  const panelClass = [
    'card-detail-panel flex flex-col overflow-hidden',
    isHero && 'card-detail-panel--hero',
  ]
    .filter(Boolean)
    .join(' ')

  const isLeader = !!card.leaderAbility
  const ability = card.ability && card.ability !== 'NONE' ? card.ability : null
  const secondAbility = card.secondAbility && card.secondAbility !== 'NONE' ? card.secondAbility : null
  const description = isLeader
    ? leaderAbilityDescriptions[card.leaderAbility!]
    : ability ? abilityDescriptions[ability] : null
  const secondDescription = secondAbility ? abilityDescriptions[secondAbility] : null

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
            {(card.ability === 'AGILE' || card.secondAbility === 'AGILE') && card.rowType === 'MELEE' && (
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
            {secondAbility && (
              <>
                <AbilityIcon ability={secondAbility} size="lg" />
                <span className="card-detail-ability-label">
                  {abilityNames[secondAbility]}
                </span>
              </>
            )}
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

      <div className="card-detail-info flex flex-col gap-1 items-center flex-1 overflow-y-auto min-h-0">
        <span className="font-heading text-sm leading-[1.2] text-gold-light text-center">{card.name}</span>
        <span className="font-ui text-[10px] uppercase tracking-[1px] text-text-muted">{cardTypeNames[card.cardType]}</span>
        {description && (
          <>
            <HorizontalSeparator variant="gold" className="w-4/5 self-center" />
            <p className="font-body text-[11px] text-text-secondary text-center leading-[1.4] m-0">{description}</p>
          </>
        )}
        {secondDescription && (
          <p className="font-body text-[11px] text-text-secondary text-center leading-[1.4] m-0">{secondDescription}</p>
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
