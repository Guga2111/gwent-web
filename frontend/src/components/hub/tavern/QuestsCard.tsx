import { Label } from '@/components/ui/label'
import { Progress } from '@/components/ui/progress'
import { HorizontalSeparator } from '@/components/ui/horizontal-separator'

interface Quest {
  text: string
  progress: string
  value: number
  reward: string
}

const quests: Quest[] = [
  { text: 'Vença 3 partidas amistosas', progress: 'I / III', value: 33, reward: '50' },
  { text: 'Coloque 10 unidades na mesa', progress: 'VI / X', value: 60, reward: '25' },
  { text: 'Derrote um jogador que use Monstros', progress: '0 / I', value: 0, reward: '100' },
]

export default function QuestsCard() {
  return (
    <div className="taverna-quests-card absolute right-6 bottom-[10px] w-[316px] text-left px-[19px] pt-[17px] pb-[14px] rounded-sm text-parchment-text">
      {/* Seal */}
      <div className="taverna-quest-seal absolute -top-[13px] left-1/2 -translate-x-1/2 w-[34px] h-[34px] flex items-center justify-center">
        <div className="taverna-quest-seal__icon w-[10px] h-[10px] rotate-45" />
      </div>

      <Label className="block font-heading font-bold text-[12.5px] tracking-[1.5px] uppercase text-center text-parchment-heading mt-1 mb-[11px]">
        Encomendas do Taverneiro
      </Label>

      <div className="flex flex-col gap-[10px]">
        {quests.map((quest, i) => (
          <div key={i}>
            {i > 0 && <HorizontalSeparator variant="parchment" className="mb-[10px]" />}
            <div className="flex justify-between items-baseline">
              <span className="font-body text-sm text-parchment-text">
                {quest.text}
              </span>
              <Label className="font-heading font-bold text-[11.5px] text-parchment-accent">
                {quest.progress}
              </Label>
            </div>
            <div className="flex items-center gap-[7px] mt-1">
              <Progress
                value={quest.value}
                className="flex-1 h-[5px] rounded-[3px] bg-[rgba(90,63,28,0.22)]"
                indicatorClassName="bg-gradient-to-r from-[#9a6a1c] to-gold"
              />
              <Label className="text-[10.5px] font-bold text-parchment-accent">
                &#x2B26; {quest.reward}
              </Label>
            </div>
          </div>
        ))}
      </div>

      <Label className="block font-body text-[12px] text-center text-parchment-muted mt-2.5">
        Novas entregas em · 06:42
      </Label>
    </div>
  )
}
