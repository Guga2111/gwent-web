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
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'

interface AbilityEntry {
  icon: LucideIcon
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
    body: 'Vence quem ganhar 2 dos 3 rounds. Cada round vai para quem tiver maior pontuação total nas fileiras.',
  },
  {
    title: 'Tipos de Cartas',
    body: 'Unidade — Corpo-a-corpo, Distância ou Cerco. Herói — imune a efeitos especiais. Especial — clima, corneta, engodo. Líder — habilidade única, usável 1× por partida.',
  },
  {
    title: 'Jogar uma Carta',
    body: 'Clique numa carta da mão para colocá-la na fileira correta. Cartas Ágeis permitem escolher entre Corpo-a-corpo e Distância.',
  },
  {
    title: 'Passar',
    body: 'Ao passar, você não pode mais jogar cartas neste round. O oponente pode continuar jogando até também passar. O vencedor do round é quem tiver maior pontuação.',
  },
  {
    title: 'Clima',
    abilities: [
      { icon: Snowflake, label: 'Gelo', desc: 'Reduz todas as unidades Corpo-a-corpo não-herói para 1 de força.' },
      { icon: CloudFog,  label: 'Nevoeiro', desc: 'Reduz todas as unidades Distância não-herói para 1 de força.' },
      { icon: CloudRain, label: 'Chuva', desc: 'Reduz todas as unidades Cerco não-herói para 1 de força.' },
      { icon: Sun,       label: 'Limpar Tempo', desc: 'Remove todos os efeitos climáticos ativos.' },
    ],
  },
  {
    title: 'Habilidades Especiais',
    abilities: [
      { icon: Eye,        label: 'Espião',          desc: 'Vai para o lado do oponente; você compra 2 cartas.' },
      { icon: HeartPulse, label: 'Médico',           desc: 'Revive uma carta do cemitério.' },
      { icon: Users,      label: 'Convocar',         desc: 'Joga automaticamente todas as cópias da carta do baralho.' },
      { icon: Flame,      label: 'Chamuscar',        desc: 'Destrói a(s) unidade(s) com maior força em campo.' },
      { icon: Link,       label: 'Laço Estreito',    desc: 'Duplica a força de todas as unidades com o mesmo nome na fileira.' },
      { icon: ChevronUp,  label: 'Moral',            desc: 'Adiciona +1 à força de todas as outras unidades da fileira.' },
      { icon: Axe,        label: 'Berserker',        desc: 'Transforma-se numa unidade mais poderosa ao ser enfraquecido.' },
      { icon: RefreshCcw, label: 'Ágil',             desc: 'Pode ser jogada em Corpo-a-corpo ou Distância à sua escolha.' },
    ],
  },
  {
    title: 'Corneta do Comandante',
    abilities: [
      { icon: Megaphone, label: 'Corneta do Comandante', desc: 'Dobra a pontuação de toda a fileira onde é jogada. Cada fileira pode ter apenas uma corneta ativa.' },
    ],
  },
  {
    title: 'Mulligan',
    body: 'No início da partida, você pode trocar até 2 cartas da sua mão pelo topo do baralho. Use para descartar cartas que não se encaixam na sua estratégia.',
  },
]

interface RulesModalProps {
  open: boolean
  onClose: () => void
}

export default function RulesModal({ open, onClose }: RulesModalProps) {
  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center rules-modal-backdrop"
      onClick={onClose}
    >
      <div
        className="relative flex flex-col rules-modal-panel"
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div
          className="flex-shrink-0 flex items-center justify-between px-7 pt-6 pb-4"
          style={{ borderBottom: '1px solid rgba(90,63,28,.2)' }}
        >
          <h2 className="font-heading text-[18px] tracking-[2px] uppercase text-[var(--parchment-heading)]">
            Regras do Gwent
          </h2>
          <button onClick={onClose} className="rules-modal-close">
            Fechar
          </button>
        </div>

        {/* Scrollable content */}
        <div
          className="flex-1 overflow-y-auto px-7 py-5 flex flex-col gap-5"
          style={{ scrollbarWidth: 'thin', scrollbarColor: 'rgba(90,63,28,.35) transparent' }}
        >
          {sections.map((section, i) => (
            <div key={i}>
              {i > 0 && <div className="parchment-separator h-px mb-5" />}
              <h3 className="font-heading text-[12.5px] tracking-[1.5px] uppercase mb-[10px] text-[var(--parchment-heading)]">
                {section.title}
              </h3>

              {section.body && (
                <p className="font-bodytext-[14.5px] leading-[1.65] text-[var(--parchment-text)]">
                  {section.body}
                </p>
              )}

              {section.abilities && (
                <div className="flex flex-col gap-2">
                  {section.abilities.map(({ icon: Icon, label, desc }) => (
                    <div key={label} className="flex items-start gap-3">
                      <div
                        className="flex-shrink-0 w-7 h-7 rounded-full flex items-center justify-center mt-[1px]"
                        style={{
                          background: 'rgba(90,63,28,.18)',
                          border: '1px solid rgba(90,63,28,.35)',
                          color: 'var(--parchment-heading)',
                        }}
                      >
                        <Icon size={13} strokeWidth={1.8} />
                      </div>
                      <p className="font-bodytext-[14px] leading-[1.55] text-[var(--parchment-text)]">
                        <span className="font-headingtext-[11px] tracking-[1px] uppercase text-[var(--parchment-heading)] mr-1">
                          {label}
                        </span>
                        — {desc}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}

          <div className="font-body text-[10.5px]text-right text-[var(--parchment-muted)] mt-1">
            que os dados sejam favoráveis em vossos duelos
          </div>
        </div>
      </div>
    </div>
  )
}
