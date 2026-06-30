import { useState } from 'react'
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

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        textAlign: 'center',
        gap: 5,
      }}
    >
      {/* Ambient light */}
      <div
        style={{
          position: 'absolute',
          width: 560,
          height: 520,
          top: -10,
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(255,196,108,.15), transparent 62%)',
          animation: 'gw-flicker 5s ease-in-out infinite',
          pointerEvents: 'none',
        }}
      />

      {/* Flavor text */}
      <div
        style={{
          fontFamily: 'var(--font-body)',
          fontStyle: 'italic',
          fontSize: 17,
          color: 'var(--text-muted)',
          position: 'relative',
        }}
      >
        Cerveja na mão, cartas na mesa — alivie alguns bolsos esta noite.
      </div>

      {/* Faction shield */}
      <div
        style={{
          position: 'relative',
          width: 90,
          height: 106,
          margin: '6px 0 2px',
          background: 'linear-gradient(180deg, var(--gold-light), var(--gold) 58%, var(--gold-dim))',
          clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          filter: 'drop-shadow(0 6px 14px rgba(0,0,0,.55))',
        }}
      >
        <div
          style={{
            width: 76,
            height: 92,
            clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
            background: 'linear-gradient(180deg, var(--blue), #1c3a5c 70%, #13283f)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          {/* Sword icon */}
          <div style={{ position: 'relative', width: 14, height: 52, marginTop: -4 }}>
            <div
              style={{
                position: 'absolute',
                left: '50%',
                top: 0,
                transform: 'translateX(-50%)',
                width: 5,
                height: 38,
                background: 'linear-gradient(180deg, var(--gold-light), var(--gold))',
                borderRadius: 2,
              }}
            />
            <div
              style={{
                position: 'absolute',
                left: '50%',
                top: 8,
                transform: 'translateX(-50%)',
                width: 22,
                height: 4,
                background: 'var(--gold-light)',
                borderRadius: 2,
              }}
            />
            <div
              style={{
                position: 'absolute',
                left: '50%',
                top: 1,
                transform: 'translateX(-50%)',
                width: 9,
                height: 9,
                borderRadius: '50%',
                background: 'var(--gold-light)',
              }}
            />
          </div>
        </div>
      </div>

      {/* Faction label */}
      <div
        style={{
          fontSize: 11,
          letterSpacing: 3,
          textTransform: 'uppercase',
          color: 'var(--blue)',
          fontWeight: 700,
          position: 'relative',
        }}
      >
        Reinos do Norte
      </div>

      {/* Mode banner */}
      <div
        style={{
          margin: '10px 0 4px',
          padding: '6px 30px',
          background: 'linear-gradient(180deg, var(--gold), var(--gold-dim))',
          clipPath: 'polygon(0 0, 8% 50%, 0 100%, 100% 100%, 92% 50%, 100% 0)',
          filter: 'drop-shadow(0 4px 8px rgba(0,0,0,.5))',
          position: 'relative',
        }}
      >
        <span
          style={{
            fontFamily: 'var(--font-heading)',
            fontWeight: 700,
            fontSize: '11.5px',
            letterSpacing: 3,
            color: 'var(--bg-darkest)',
          }}
        >
          RANQUEADA · COROAS EM JOGO
        </span>
      </div>

      {/* Primary CTA */}
      <button
        style={{
          position: 'relative',
          marginTop: 6,
          padding: '20px 56px',
          borderRadius: 11,
          border: 'none',
          cursor: 'pointer',
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
            style={{
              position: 'absolute',
              ...pos,
              width: 8,
              height: 8,
              transform: 'rotate(45deg)',
              background: 'var(--bg-darkest)',
              boxShadow: '0 0 0 1px rgba(255,235,190,.4)',
            }}
          />
        ))}
        <div style={{ display: 'flex', alignItems: 'center', gap: 15, color: 'var(--bg-darkest)' }}>
          <svg viewBox="0 0 24 24" style={{ width: 30, height: 30, fill: 'none', stroke: 'currentColor', strokeWidth: 1.8, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
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
            style={{
              fontFamily: 'var(--font-heading)',
              fontSize: 25,
              fontWeight: 700,
              letterSpacing: '1.5px',
              textShadow: '0 1px 0 rgba(255,240,200,.5)',
            }}
          >
            PROCURAR OPONENTE
          </span>
        </div>
      </button>

      {/* Win streak + amistosa link */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 16,
          marginTop: 14,
          position: 'relative',
        }}
      >
        <span
          style={{
            fontFamily: 'var(--font-body)',
            fontStyle: 'italic',
            fontSize: '13.5px',
            color: 'var(--text-muted)',
          }}
        >
          sequência de 4 vitórias
        </span>
        <span
          style={{
            width: 4,
            height: 4,
            borderRadius: '50%',
            background: 'var(--border-gold)',
          }}
        />
        <span
          style={{
            fontFamily: 'var(--font-body)',
            fontStyle: 'italic',
            fontSize: '13.5px',
            color: 'var(--text-muted)',
          }}
        >
          320 PR até o Ouro
        </span>
        <span
          style={{
            width: 4,
            height: 4,
            borderRadius: '50%',
            background: 'var(--border-gold)',
          }}
        />
        <button
          onClick={() => setModalOpen(true)}
          style={{
            background: 'transparent',
            border: 'none',
            cursor: 'pointer',
            color: 'var(--blue)',
            fontFamily: 'var(--font-body)',
            fontStyle: 'italic',
            fontSize: '13.5px',
            textDecoration: 'underline',
            textUnderlineOffset: 3,
          }}
        >
          trocar tapas numa amistosa
        </button>
      </div>

      {/* Bottom left: Active deck preview */}
      <div style={{ position: 'absolute', left: 24, bottom: 24, textAlign: 'left' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 9, marginBottom: 9 }}>
          <span
            style={{
              fontFamily: 'var(--font-heading)',
              fontWeight: 600,
              fontSize: '10.5px',
              letterSpacing: '2.5px',
              textTransform: 'uppercase',
              color: 'var(--gold)',
            }}
          >
            Seu baralho
          </span>
          <span
            style={{
              fontFamily: 'var(--font-body)',
              fontStyle: 'italic',
              fontSize: '12.5px',
              color: 'var(--text-muted)',
            }}
          >
            Vanguarda do Norte · 25
          </span>
          <button
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 5,
              padding: '4px 11px',
              borderRadius: 5,
              background: 'rgba(240,205,120,.12)',
              boxShadow: 'inset 0 0 0 1px rgba(240,205,120,.4)',
              color: 'var(--gold-light)',
              cursor: 'pointer',
              fontSize: 11,
              fontWeight: 600,
              letterSpacing: '.5px',
              border: 'none',
            }}
          >
            <svg viewBox="0 0 24 24" style={{ width: 12, height: 12, fill: 'none', stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
              <path d="M21.17 6.81a1 1 0 0 0-3.98-3.99L3.84 16.17a2 2 0 0 0-.5.83l-1.32 4.35a.5.5 0 0 0 .62.62l4.35-1.32a2 2 0 0 0 .83-.5z" />
              <path d="m15 5 4 4" />
            </svg>
            Editar
          </button>
        </div>
        <div style={{ position: 'relative', height: 140, width: 330 }}>
          {/* Card fan */}
          {deckFan.map((card, i) => (
            <div
              key={i}
              style={{
                position: 'absolute',
                bottom: card.y,
                left: card.x,
                width: 82,
                height: 112,
                borderRadius: 7,
                background: 'linear-gradient(160deg, #24405f, #142536)',
                boxShadow: '0 6px 16px rgba(0,0,0,.5)',
                border: '2px solid var(--gold)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                transform: `rotate(${card.rot}deg)`,
                transformOrigin: 'bottom left',
                zIndex: card.z,
              }}
            >
              <div
                style={{
                  width: 13,
                  height: 13,
                  transform: 'rotate(45deg)',
                  border: '1.5px solid rgba(240,205,120,.5)',
                }}
              />
            </div>
          ))}
          {/* Leader card */}
          <div
            style={{
              position: 'absolute',
              left: 0,
              bottom: 0,
              width: 92,
              height: 124,
              borderRadius: 8,
              background: 'linear-gradient(180deg, var(--gold-light), var(--gold-dark))',
              padding: 3,
              boxShadow: '0 12px 26px rgba(0,0,0,.6)',
              zIndex: 6,
            }}
          >
            <div
              style={{
                position: 'relative',
                width: '100%',
                height: '100%',
                borderRadius: 6,
                overflow: 'hidden',
                background: 'repeating-linear-gradient(45deg, #2a4258 0 6px, #21384b 6px 12px)',
                display: 'flex',
                alignItems: 'flex-start',
                justifyContent: 'center',
              }}
            >
              <span
                style={{
                  fontFamily: 'monospace',
                  fontSize: '7.5px',
                  color: '#8fb0c8',
                  marginTop: 8,
                }}
              >
                arte do líder
              </span>
              <div
                style={{
                  position: 'absolute',
                  left: 0,
                  right: 0,
                  bottom: 0,
                  padding: '5px 6px',
                  background: 'linear-gradient(180deg, transparent, rgba(10,6,3,.92))',
                  textAlign: 'center',
                }}
              >
                <div
                  style={{
                    fontFamily: 'var(--font-heading)',
                    fontWeight: 700,
                    fontSize: 10,
                    color: 'var(--gold-light)',
                    lineHeight: 1.1,
                  }}
                >
                  Foltest
                </div>
                <div
                  style={{
                    fontFamily: 'var(--font-body)',
                    fontStyle: 'italic',
                    fontSize: 8,
                    color: 'var(--gold)',
                  }}
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
        style={{
          position: 'absolute',
          right: 24,
          bottom: 10,
          width: 316,
          transform: 'rotate(-1.4deg)',
          padding: '17px 19px 14px',
          borderRadius: 3,
          background: 'linear-gradient(155deg, #e9d6a6, #dcc795 55%, #cbb279)',
          boxShadow:
            '0 16px 34px rgba(0,0,0,.55), inset 0 0 36px rgba(150,115,60,.32), inset 0 0 0 1px rgba(120,90,45,.25)',
          color: '#3d2b14',
          textAlign: 'left',
        }}
      >
        {/* Seal */}
        <div
          style={{
            position: 'absolute',
            top: -13,
            left: '50%',
            transform: 'translateX(-50%)',
            width: 34,
            height: 34,
            borderRadius: '48% 52% 51% 49%',
            background: 'radial-gradient(circle at 38% 32%, #b8403a, #6e1414)',
            boxShadow: '0 3px 7px rgba(0,0,0,.5), inset 0 -3px 5px rgba(0,0,0,.35)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <div
            style={{
              width: 10,
              height: 10,
              transform: 'rotate(45deg)',
              boxShadow: 'inset 0 0 0 1.5px rgba(255,210,200,.45)',
            }}
          />
        </div>

        <div
          style={{
            fontFamily: 'var(--font-heading)',
            fontWeight: 700,
            fontSize: '12.5px',
            letterSpacing: '1.5px',
            textTransform: 'uppercase',
            textAlign: 'center',
            color: '#5a3f1c',
            margin: '4px 0 11px',
          }}
        >
          Encomendas do Taverneiro
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {[
            { text: 'Vença 3 duelos ranqueados', progress: 'I / III', pct: '33%', reward: '50' },
            { text: 'Baixe 10 unidades na mesa', progress: 'VI / X', pct: '60%', reward: '25' },
            { text: 'Despache um jogador de Monstros', progress: '0 / I', pct: '0%', reward: '100' },
          ].map((quest, i) => (
            <div key={i}>
              {i > 0 && <div style={{ height: 1, background: 'rgba(90,63,28,.18)', marginBottom: 10 }} />}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                <span style={{ fontFamily: 'var(--font-body)', fontSize: 14, color: '#3d2b14' }}>
                  {quest.text}
                </span>
                <span
                  style={{
                    fontFamily: 'var(--font-heading)',
                    fontWeight: 700,
                    fontSize: '11.5px',
                    color: '#7a5620',
                  }}
                >
                  {quest.progress}
                </span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 7, marginTop: 4 }}>
                <div
                  style={{
                    flex: 1,
                    height: 5,
                    borderRadius: 3,
                    background: 'rgba(90,63,28,.22)',
                    overflow: 'hidden',
                  }}
                >
                  <div
                    style={{
                      width: quest.pct,
                      height: '100%',
                      background: 'linear-gradient(90deg, #9a6a1c, var(--gold))',
                    }}
                  />
                </div>
                <span style={{ fontSize: '10.5px', fontWeight: 700, color: '#8a6526' }}>
                  &#x2B26; {quest.reward}
                </span>
              </div>
            </div>
          ))}
        </div>

        <div
          style={{
            fontFamily: 'var(--font-body)',
            fontStyle: 'italic',
            fontSize: '10.5px',
            textAlign: 'right',
            color: '#7a5e34',
            marginTop: 10,
          }}
        >
          novas encomendas ao raiar do dia · 06:42
        </div>
      </div>

      <MesaPrivadaModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  )
}
