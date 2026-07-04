import { useState } from 'react'
import { Pencil } from 'lucide-react'
import { createGame, joinGame } from '@/api/game'
import MesaPrivadaModal from '@/components/hub/MesaPrivadaModal'

const deckFan = [
  { rot: -2, x: 66, y: 10, z: 1 },
  { rot: 4, x: 104, y: 8, z: 2 },
  { rot: 9, x: 146, y: 10, z: 3 },
  { rot: 14, x: 190, y: 16, z: 4 },
  { rot: 18, x: 232, y: 24, z: 5 },
]

export default function Taverna() {
  const [modalOpen, setModalOpen] = useState(false)

  async function handleCreateGame(): Promise<string> {
    const { gameId } = await createGame()
    return gameId
  }

  async function handleJoinGame(code: string): Promise<void> {
    await joinGame(code)
  }

  return (
    <div className="absolute inset-0 flex flex-col items-center justify-center text-center gap-[5px]">
      {/* Ambient light */}
      <div
        className="absolute w-[560px] h-[520px] -top-[10px] rounded-full pointer-events-none"
        style={{
          background: 'radial-gradient(circle, rgba(255,196,108,.15), transparent 62%)',
          animation: 'gw-flicker 5s ease-in-out infinite',
        }}
      />

      {/* Flavor text */}
      <div
        className="relative italic text-[17px] text-[var(--text-muted)]"
        style={{ fontFamily: 'var(--font-body)' }}
      >
        Cerveja na mão, cartas na mesa — alivie alguns bolsos esta noite.
      </div>

      {/* Faction shield */}
      <div
        className="relative my-1.5 flex items-center justify-center"
        style={{
          width: 90,
          height: 106,
          background: 'linear-gradient(180deg, var(--gold-light), var(--gold) 58%, var(--gold-dim))',
          clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
          filter: 'drop-shadow(0 6px 14px rgba(0,0,0,.55))',
        }}
      >
        <div
          className="flex items-center justify-center"
          style={{
            width: 76,
            height: 92,
            clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
            background: 'linear-gradient(180deg, var(--blue), #1c3a5c 70%, #13283f)',
          }}
        >
          {/* Sword icon */}
          <div className="relative" style={{ width: 14, height: 52, marginTop: -4 }}>
            <div
              className="absolute left-1/2 top-0 -translate-x-1/2 rounded-sm"
              style={{ width: 5, height: 38, background: 'linear-gradient(180deg, var(--gold-light), var(--gold))' }}
            />
            <div
              className="absolute left-1/2 top-2 -translate-x-1/2 rounded-sm bg-[var(--gold-light)]"
              style={{ width: 22, height: 4 }}
            />
            <div
              className="absolute left-1/2 top-px -translate-x-1/2 rounded-full bg-[var(--gold-light)]"
              style={{ width: 9, height: 9 }}
            />
          </div>
        </div>
      </div>

      {/* Faction label */}
      <div
        className="relative text-[11px] tracking-[3px] uppercase font-bold text-[var(--blue)]"
      >
        Reinos do Norte
      </div>

      {/* Mode banner */}
      <div
        className="relative my-2.5 px-[30px] py-1.5"
        style={{
          background: 'linear-gradient(180deg, var(--gold), var(--gold-dim))',
          clipPath: 'polygon(0 0, 8% 50%, 0 100%, 100% 100%, 92% 50%, 100% 0)',
          filter: 'drop-shadow(0 4px 8px rgba(0,0,0,.5))',
        }}
      >
        <span
          className="font-bold text-[11.5px] tracking-[3px] text-[var(--bg-darkest)]"
          style={{ fontFamily: 'var(--font-heading)' }}
        >
          RANQUEADA · COROAS EM JOGO
        </span>
      </div>

      {/* Primary CTA */}
      <button
        className="relative mt-1.5 px-14 py-5 rounded-[11px] border-none cursor-pointer"
        style={{
          background: 'linear-gradient(180deg, #f7df9a 0%, #e0b65e 44%, var(--gold) 62%, var(--gold) 100%)',
          animation: 'gw-glow 3.4s ease-in-out infinite',
        }}
      >
        {/* Corner diamonds */}
        {[
          { top: 7, left: 7 },
          { top: 7, right: 7 },
          { bottom: 7, left: 7 },
          { bottom: 7, right: 7 },
        ].map((pos, i) => (
          <div
            key={i}
            className="absolute w-2 h-2 bg-[var(--bg-darkest)]"
            style={{
              ...pos,
              transform: 'rotate(45deg)',
              boxShadow: '0 0 0 1px rgba(255,235,190,.4)',
            }}
          />
        ))}
        <div className="flex items-center gap-[15px] text-[var(--bg-darkest)]">
          <svg viewBox="0 0 24 24" className="w-[30px] h-[30px]" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
            <polyline points="14.5 17.5 3 6 3 3 6 3 17.5 14.5" />
            <line x1="13" y1="19" x2="19" y2="13" />
            <line x1="16" y1="16" x2="20" y2="20" />
            <line x1="19" y1="21" x2="21" y2="19" />
            <polyline points="14.5 6.5 18 3 21 3 21 6 17.5 9.5" />
            <line x1="5" y1="14" x2="9" y2="18" />
            <line x1="7" y1="17" x2="4" y2="20" />
            <line x1="3" y1="19" x2="5" y2="21" />
          </svg>
          <span
            className="text-[25px] font-bold tracking-[1.5px]"
            style={{
              fontFamily: 'var(--font-heading)',
              textShadow: '0 1px 0 rgba(255,240,200,.5)',
            }}
          >
            PROCURAR OPONENTE
          </span>
        </div>
      </button>

      {/* Win streak + amistosa link */}
      <div className="relative flex items-center gap-4 mt-3.5">
        <span
          className="italic text-[13.5px] text-[var(--text-muted)]"
          style={{ fontFamily: 'var(--font-body)' }}
        >
          sequência de 4 vitórias
        </span>
        <span className="w-1 h-1 rounded-full bg-[var(--border-gold)]" />
        <span
          className="italic text-[13.5px] text-[var(--text-muted)]"
          style={{ fontFamily: 'var(--font-body)' }}
        >
          320 PR até o Ouro
        </span>
        <span className="w-1 h-1 rounded-full bg-[var(--border-gold)]" />
        <button
          onClick={() => setModalOpen(true)}
          className="bg-transparent border-none cursor-pointer italic text-[13.5px] underline underline-offset-[3px] text-[var(--blue)]"
          style={{ fontFamily: 'var(--font-body)' }}
        >
          trocar tapas numa amistosa
        </button>
      </div>

      {/* Bottom left: Active deck preview */}
      <div className="absolute left-6 bottom-6 text-left">
        <div className="flex items-center gap-[9px] mb-[9px]">
          <span
            className="font-semibold text-[10.5px] tracking-[2.5px] uppercase text-[var(--gold)]"
            style={{ fontFamily: 'var(--font-heading)' }}
          >
            Seu baralho
          </span>
          <span
            className="italic text-[12.5px] text-[var(--text-muted)]"
            style={{ fontFamily: 'var(--font-body)' }}
          >
            Vanguarda do Norte · 25
          </span>
          <button
            className="flex items-center gap-[5px] px-[11px] py-1 rounded-[5px] text-[11px] font-semibold tracking-[.5px] border-none cursor-pointer text-[var(--gold-light)]"
            style={{
              background: 'rgba(240,205,120,.12)',
              boxShadow: 'inset 0 0 0 1px rgba(240,205,120,.4)',
            }}
          >
            <Pencil size={12} strokeWidth={2} />
            Editar
          </button>
        </div>
        <div className="relative h-[140px] w-[330px]">
          {/* Card fan */}
          {deckFan.map((card, i) => (
            <div
              key={i}
              className="absolute flex items-center justify-center rounded-[7px] border-2 border-[var(--gold)]"
              style={{
                bottom: card.y,
                left: card.x,
                width: 82,
                height: 112,
                background: 'linear-gradient(160deg, #24405f, #142536)',
                boxShadow: '0 6px 16px rgba(0,0,0,.5)',
                transform: `rotate(${card.rot}deg)`,
                transformOrigin: 'bottom left',
                zIndex: card.z,
              }}
            >
              <div
                className="w-[13px] h-[13px] rotate-45"
                style={{ border: '1.5px solid rgba(240,205,120,.5)' }}
              />
            </div>
          ))}
          {/* Leader card */}
          <div
            className="absolute left-0 bottom-0 rounded-[8px] p-[3px] z-[6]"
            style={{
              width: 92,
              height: 124,
              background: 'linear-gradient(180deg, var(--gold-light), var(--gold-dark))',
              boxShadow: '0 12px 26px rgba(0,0,0,.6)',
            }}
          >
            <div
              className="relative w-full h-full rounded-[6px] overflow-hidden flex items-start justify-center"
              style={{ background: 'repeating-linear-gradient(45deg, #2a4258 0 6px, #21384b 6px 12px)' }}
            >
              <span className="font-mono text-[7.5px] text-[#8fb0c8] mt-2">arte do líder</span>
              <div
                className="absolute left-0 right-0 bottom-0 px-1.5 py-[5px] text-center"
                style={{ background: 'linear-gradient(180deg, transparent, rgba(10,6,3,.92))' }}
              >
                <div
                  className="font-bold text-[10px] text-[var(--gold-light)] leading-[1.1]"
                  style={{ fontFamily: 'var(--font-heading)' }}
                >
                  Foltest
                </div>
                <div
                  className="italic text-[8px] text-[var(--gold)]"
                  style={{ fontFamily: 'var(--font-body)' }}
                >
                  Rei de Temeria
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom right: Quests */}
      <div
        className="absolute right-6 bottom-[10px] w-[316px] text-left px-[19px] pt-[17px] pb-[14px] rounded-sm text-[var(--parchment-text)]"
        style={{
          transform: 'rotate(-1.4deg)',
          background: 'linear-gradient(155deg, var(--parchment-light), var(--parchment-mid) 55%, var(--parchment-dark))',
          boxShadow: '0 16px 34px rgba(0,0,0,.55), inset 0 0 36px rgba(150,115,60,.32), inset 0 0 0 1px rgba(120,90,45,.25)',
        }}
      >
        {/* Seal */}
        <div
          className="absolute -top-[13px] left-1/2 -translate-x-1/2 w-[34px] h-[34px] flex items-center justify-center"
          style={{
            borderRadius: '48% 52% 51% 49%',
            background: 'radial-gradient(circle at 38% 32%, #b8403a, #6e1414)',
            boxShadow: '0 3px 7px rgba(0,0,0,.5), inset 0 -3px 5px rgba(0,0,0,.35)',
          }}
        >
          <div
            className="w-[10px] h-[10px] rotate-45"
            style={{ boxShadow: 'inset 0 0 0 1.5px rgba(255,210,200,.45)' }}
          />
        </div>

        <div
          className="font-bold text-[12.5px] tracking-[1.5px] uppercase text-center text-[var(--parchment-heading)] mt-1 mb-[11px]"
          style={{ fontFamily: 'var(--font-heading)' }}
        >
          Encomendas do Taverneiro
        </div>

        <div className="flex flex-col gap-[10px]">
          {[
            { text: 'Vença 3 duelos ranqueados', progress: 'I / III', pct: '33%', reward: '50' },
            { text: 'Baixe 10 unidades na mesa', progress: 'VI / X', pct: '60%', reward: '25' },
            { text: 'Despache um jogador de Monstros', progress: '0 / I', pct: '0%', reward: '100' },
          ].map((quest, i) => (
            <div key={i}>
              {i > 0 && <div className="h-px mb-[10px]" style={{ background: 'rgba(90,63,28,.18)' }} />}
              <div className="flex justify-between items-baseline">
                <span className="text-sm text-[var(--parchment-text)]" style={{ fontFamily: 'var(--font-body)' }}>
                  {quest.text}
                </span>
                <span
                  className="font-bold text-[11.5px] text-[var(--parchment-accent)]"
                  style={{ fontFamily: 'var(--font-heading)' }}
                >
                  {quest.progress}
                </span>
              </div>
              <div className="flex items-center gap-[7px] mt-1">
                <div
                  className="flex-1 h-[5px] rounded-[3px] overflow-hidden"
                  style={{ background: 'rgba(90,63,28,.22)' }}
                >
                  <div
                    className="h-full"
                    style={{ width: quest.pct, background: 'linear-gradient(90deg, #9a6a1c, var(--gold))' }}
                  />
                </div>
                <span className="text-[10.5px] font-bold text-[var(--parchment-accent)]">
                  &#x2B26; {quest.reward}
                </span>
              </div>
            </div>
          ))}
        </div>

        <div
          className="italic text-[10.5px] text-right text-[var(--parchment-muted)] mt-2.5"
          style={{ fontFamily: 'var(--font-body)' }}
        >
          novas encomendas ao raiar do dia · 06:42
        </div>
      </div>

      <MesaPrivadaModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onCreateGame={handleCreateGame}
        onJoinGame={handleJoinGame}
      />
    </div>
  )
}
