interface RulesModalProps {
  open: boolean
  onClose: () => void
}

const sections = [
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
    body: 'Gelo (Corpo-a-corpo), Nevoeiro (Distância) e Chuva (Cerco) reduzem todas as unidades não-herói daquela fileira para 1 de força. Uma carta Limpar Tempo remove o efeito.',
  },
  {
    title: 'Habilidades Especiais',
    body: 'SPY — vai para o lado do oponente; você compra 2 cartas. MEDIC — revive uma carta do cemitério. MUSTER — joga todas as cópias da carta do baralho. SCORCH — destrói a(s) unidade(s) com maior força. TIGHT BOND — duplica a força de unidades com o mesmo nome na fileira.',
  },
  {
    title: 'Corneta do Comandante',
    body: 'Dobra a pontuação de toda a fileira onde é jogada. Cada fileira pode ter apenas uma corneta ativa.',
  },
  {
    title: 'Mulligan',
    body: 'No início da partida, você pode trocar até 2 cartas da sua mão pelo topo do baralho. Use para descartar cartas que não se encaixam na sua estratégia.',
  },
]

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
              {i > 0 && (
                <div className="parchment-separator h-px mb-5" />
              )}
              <h3 className="font-heading text-[12.5px] tracking-[1.5px] uppercase mb-[7px] text-[var(--parchment-heading)]">
                {section.title}
              </h3>
              <p className="font-body italic text-[14.5px] leading-[1.65] text-[var(--parchment-text)]">
                {section.body}
              </p>
            </div>
          ))}

          <div className="font-body text-[10.5px] italic text-right text-[var(--parchment-muted)] mt-1">
            que os dados sejam favoráveis em vossos duelos
          </div>
        </div>
      </div>
    </div>
  )
}
