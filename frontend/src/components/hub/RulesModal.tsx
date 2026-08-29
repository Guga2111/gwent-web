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
      className="fixed inset-0 z-50 flex items-center justify-center"
      style={{ background: 'rgba(0,0,0,.72)' }}
      onClick={onClose}
    >
      <div
        className="relative flex flex-col"
        style={{
          width: 'min(640px, 92vw)',
          maxHeight: '88vh',
          borderRadius: 4,
          background: 'linear-gradient(155deg, var(--parchment-light), var(--parchment-mid) 55%, var(--parchment-dark))',
          boxShadow: '0 24px 60px rgba(0,0,0,.7), inset 0 0 48px rgba(150,115,60,.28), inset 0 0 0 1px rgba(120,90,45,.3)',
        }}
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div
          className="flex-shrink-0 flex items-center justify-between px-7 pt-6 pb-4"
          style={{ borderBottom: '1px solid rgba(90,63,28,.2)' }}
        >
          <h2
            className="text-[18px] tracking-[2px] uppercase text-[var(--parchment-heading)]"
            style={{ fontFamily: 'var(--font-heading)' }}
          >
            Regras do Gwent
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'linear-gradient(180deg, var(--gold), var(--gold-dark))',
              border: 'none',
              borderRadius: 3,
              padding: '6px 18px',
              color: 'var(--bg-darkest)',
              fontFamily: 'var(--font-heading)',
              fontSize: 11,
              fontWeight: 700,
              letterSpacing: '1.5px',
              textTransform: 'uppercase',
              cursor: 'pointer',
              boxShadow: '0 2px 6px rgba(0,0,0,.35)',
            }}
          >
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
                <div className="h-px mb-5" style={{ background: 'rgba(90,63,28,.18)' }} />
              )}
              <h3
                className="text-[12.5px] tracking-[1.5px] uppercase mb-[7px] text-[var(--parchment-heading)]"
                style={{ fontFamily: 'var(--font-heading)', fontWeight: 600 }}
              >
                {section.title}
              </h3>
              <p
                className="text-[14.5px] leading-[1.65] text-[var(--parchment-text)]"
                style={{ fontFamily: 'var(--font-body)', fontStyle: 'italic' }}
              >
                {section.body}
              </p>
            </div>
          ))}

          <div
            className="text-[10.5px] italic text-right text-[var(--parchment-muted)] mt-1"
            style={{ fontFamily: 'var(--font-body)' }}
          >
            que os dados sejam favoráveis em vossos duelos
          </div>
        </div>
      </div>
    </div>
  )
}
