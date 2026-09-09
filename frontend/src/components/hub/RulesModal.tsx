import type { ReactNode } from 'react'
import {
  Eye,
  HeartPulse,
  Users,
  Flame,
  Link,
  ChevronUp,
  Axe,
  Megaphone,
  Snowflake,
  CloudFog,
  CloudRain,
  Sun,
  RefreshCcw,
  Sword,
  BowArrow,
} from 'lucide-react'
import { SiegeIcon } from '@/components/board/card/RowIcon'
import { HorizontalSeparator } from '@/components/ui/horizontal-separator'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

interface AbilityEntry {
  icon: (props: { size: number; strokeWidth: number }) => ReactNode
  label: string
  desc: string
}

interface Section {
  title: string
  body?: string
  abilities?: AbilityEntry[]
}

const sections: Section[] = [
  {
    title: 'Objetivo',
    body: 'Vence quem ganhar 2 dos 3 rounds. Cada round é de quem tiver a maior pontuação somada nas fileiras.',
  },
  {
    title: 'Tipos de Cartas',
    body: 'Cada carta pertence a um tipo: Unidade (ocupa uma fileira), Herói (imune a efeitos especiais), Especial (clima, corneta ou engodo) ou Líder (habilidade única, uma vez por partida).',
  },
  {
    title: 'Fileiras de Combate',
    abilities: [
      { icon: Sword,     label: 'Corpo-a-corpo', desc: 'Unidades de combate próximo. Sofrem o efeito de Geada.' },
      { icon: BowArrow,  label: 'Distância',     desc: 'Unidades de ataque à distância. Sofrem o efeito de Névoa.' },
      { icon: SiegeIcon, label: 'Cerco',         desc: 'Unidades de armamento pesado. Sofrem o efeito de Chuva.' },
    ],
  },
  {
    title: 'Jogar uma Carta',
    body: 'Clique numa carta da mão para colocá-la na fileira correspondente. Cartas Ágeis permitem escolher entre Corpo-a-corpo e Distância.',
  },
  {
    title: 'Passar',
    body: 'Quem passa não pode mais jogar cartas neste round. O oponente pode continuar até também passar. Ganha o round quem tiver a pontuação mais alta.',
  },
  {
    title: 'Clima',
    abilities: [
      { icon: Snowflake, label: 'Gelo',         desc: 'Reduz todas as unidades Corpo-a-corpo (exceto heróis) para 1 de força.' },
      { icon: CloudFog,  label: 'Nevoeiro',      desc: 'Reduz todas as unidades Distância (exceto heróis) para 1 de força.' },
      { icon: CloudRain, label: 'Chuva',         desc: 'Reduz todas as unidades Cerco (exceto heróis) para 1 de força.' },
      { icon: Sun,       label: 'Limpar Tempo',  desc: 'Cancela todos os efeitos climáticos ativos no campo.' },
    ],
  },
  {
    title: 'Habilidades Especiais',
    abilities: [
      { icon: Eye,        label: 'Espião',          desc: 'Entra no campo do oponente. Em troca, você compra 2 cartas do baralho.' },
      { icon: HeartPulse, label: 'Médico',           desc: 'Traz de volta uma carta do seu cemitério para o campo.' },
      { icon: Users,      label: 'Convocar',         desc: 'Puxa todas as cópias desta carta do baralho direto para o campo.' },
      { icon: Flame,      label: 'Chamuscar',        desc: 'Elimina a unidade com maior força no campo. Em caso de empate, todas são destruídas.' },
      { icon: Link,       label: 'Laço Estreito',    desc: 'Dobra a força de todas as unidades com o mesmo nome na fileira.' },
      { icon: ChevronUp,  label: 'Moral',            desc: 'Concede +1 de força a todas as outras unidades da mesma fileira.' },
      { icon: Axe,        label: 'Berserker',        desc: 'Quando enfraquecido por efeitos, se transforma numa versão mais poderosa.' },
      { icon: RefreshCcw, label: 'Ágil',             desc: 'Pode ser colocada na fileira de Corpo-a-corpo ou Distância, à sua escolha.' },
    ],
  },
  {
    title: 'Corneta do Comandante',
    abilities: [
      { icon: Megaphone, label: 'Corneta do Comandante', desc: 'Dobra a força total da fileira onde é colocada. Cada fileira comporta apenas uma corneta por vez.' },
    ],
  },
  {
    title: 'Mulligan',
    body: 'No início de cada round, você pode trocar até 2 cartas da mão pelo topo do baralho. Útil para descartar cartas que não combinam com sua estratégia.',
  },
]

interface RulesModalProps {
  open: boolean
  onClose: () => void
}

export default function RulesModal({ open, onClose }: RulesModalProps) {
  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) onClose() }}>
      <DialogContent
        className="rules-modal-panel flex flex-col p-0 border-none w-[min(780px,92vw)] max-w-none sm:max-w-none max-h-[88vh]"
        showCloseButton={false}
      >
        {/* Header */}
        <DialogHeader className="flex-shrink-0 flex-row items-center justify-between px-7 pt-6 pb-4 border-b border-parchment-heading/20 gap-0">
          <DialogTitle className="font-heading text-[22px] tracking-[2px] uppercase text-parchment-heading">
            Regras do Gwent
          </DialogTitle>
          <Button onClick={onClose} variant="parchment" className="px-[18px] py-1.5">
            Fechar
          </Button>
        </DialogHeader>

        {/* Scrollable content */}
        <div
          className="flex-1 overflow-y-auto px-7 py-5 flex flex-col gap-5 [scrollbar-width:thin] [scrollbar-color:rgba(90,63,28,.35)_transparent]"
        >
          {sections.map((section, i) => (
            <div key={i}>
              {i > 0 && <HorizontalSeparator variant="parchment" className="mb-5" />}
              <h3 className="font-heading text-[15px] tracking-[1.5px] uppercase mb-[10px] text-parchment-heading">
                {section.title}
              </h3>

              {section.body && (
                <p className="font-body text-base leading-[1.65] text-parchment-text">
                  {section.body}
                </p>
              )}

              {section.abilities && (
                <div className="flex flex-col gap-2">
                  {section.abilities.map(({ icon: Icon, label, desc }) => (
                    <div key={label} className="flex items-start gap-3">
                      <div
                        className="flex-shrink-0 w-7 h-7 rounded-full flex items-center justify-center mt-[1px] bg-parchment-heading/18 border border-parchment-heading/35 text-parchment-heading"
                      >
                        <Icon size={13} strokeWidth={1.8} />
                      </div>
                      <p className="font-body text-[15px] leading-[1.55] text-parchment-text">
                        <span className="font-heading text-[12.5px] tracking-[1px] uppercase text-parchment-heading mr-1">
                          {label}
                        </span>
                        {desc}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}

          <div className="font-body text-xs text-right text-parchment-muted mt-1">
            que os dados sejam favoráveis em vossos duelos
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
