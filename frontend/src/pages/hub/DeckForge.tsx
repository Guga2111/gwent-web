import { useState, useEffect } from 'react'
import { Plus, Trash2, X, Check, ChevronLeft } from 'lucide-react'
import { getUserDecks, createDeck, updateDeck, deleteDeck } from '@/api/deck'
import { getCardsByFaction } from '@/api/catalog'
import type { DeckDto, CatalogCardDto, Faction, DeckCardEntryDto, SaveDeckRequest } from '@/types/deck'

const FACTIONS: { value: Faction; label: string }[] = [
  { value: 'NORTHERN_REALMS', label: 'Reinos do Norte' },
  { value: 'NILFGAARD', label: 'Nilfgaard' },
  { value: 'MONSTER', label: 'Monstros' },
  { value: 'SCOIATAEL', label: 'Scoia\'tael' },
  { value: 'SKELLIGE', label: 'Skellige' },
]

function totalCount(cards: DeckCardEntryDto[]) {
  return cards.reduce((s, e) => s + e.quantity, 0)
}

interface EditorState {
  id: string | null
  name: string
  faction: Faction
  leaderId: string
  cards: DeckCardEntryDto[]
}

function emptyEditor(): EditorState {
  return { id: null, name: '', faction: 'NORTHERN_REALMS', leaderId: '', cards: [] }
}

function deckToEditor(d: DeckDto): EditorState {
  return { id: d.id, name: d.name, faction: d.faction, leaderId: d.leaderId, cards: d.cards }
}

export default function DeckForge() {
  const [decks, setDecks] = useState<DeckDto[]>([])
  const [editor, setEditor] = useState<EditorState | null>(null)
  const [catalog, setCatalog] = useState<CatalogCardDto[]>([])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    getUserDecks().then(setDecks).catch(() => {})
  }, [])

  useEffect(() => {
    if (!editor) return
    getCardsByFaction(editor.faction, true).then(setCatalog).catch(() => {})
  }, [editor?.faction])

  function openNew() {
    setEditor(emptyEditor())
    setError('')
  }

  function openEdit(d: DeckDto) {
    setEditor(deckToEditor(d))
    setError('')
  }

  function closeEditor() {
    setEditor(null)
    setError('')
  }

  async function handleDelete(id: string) {
    await deleteDeck(id)
    setDecks((prev) => prev.filter((d) => d.id !== id))
  }

  async function handleSave() {
    if (!editor) return
    setError('')

    const count = totalCount(editor.cards)
    if (!editor.name.trim()) { setError('O baralho precisa de um nome'); return }
    if (!editor.leaderId) { setError('Selecione um líder'); return }
    if (count < 22 || count > 40) { setError(`Total de cartas: ${count} (mín 22, máx 40)`); return }

    const request: SaveDeckRequest = {
      name: editor.name,
      faction: editor.faction,
      leaderId: editor.leaderId,
      cards: editor.cards,
    }

    setSaving(true)
    try {
      if (editor.id) {
        const updated = await updateDeck(editor.id, request)
        setDecks((prev) => prev.map((d) => (d.id === updated.id ? updated : d)))
      } else {
        const created = await createDeck(request)
        setDecks((prev) => [...prev, created])
      }
      closeEditor()
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'Falha ao salvar baralho')
    } finally {
      setSaving(false)
    }
  }

  function addCard(card: CatalogCardDto) {
    if (!editor) return
    const existing = editor.cards.find((e) => e.cardId === card.id)
    const maxCopies = card.ability === 'HERO' ? 1 : card.deckCopies
    if (existing) {
      if (existing.quantity >= maxCopies) return
      setEditor({ ...editor, cards: editor.cards.map((e) => e.cardId === card.id ? { ...e, quantity: e.quantity + 1 } : e) })
    } else {
      setEditor({ ...editor, cards: [...editor.cards, { cardId: card.id, quantity: 1 }] })
    }
  }

  function removeCard(cardId: string) {
    if (!editor) return
    const existing = editor.cards.find((e) => e.cardId === cardId)
    if (!existing) return
    if (existing.quantity > 1) {
      setEditor({ ...editor, cards: editor.cards.map((e) => e.cardId === cardId ? { ...e, quantity: e.quantity - 1 } : e) })
    } else {
      setEditor({ ...editor, cards: editor.cards.filter((e) => e.cardId !== cardId) })
    }
  }

  const leaders = catalog.filter((c) => c.cardType === 'LEADER')
  const nonLeaderCatalog = catalog.filter((c) => c.cardType !== 'LEADER')

  const cardById = Object.fromEntries(catalog.map((c) => [c.id, c]))

  const count = editor ? totalCount(editor.cards) : 0

  if (!editor) {
    return (
      <div className="absolute inset-0 flex overflow-hidden">
        {/* Sidebar */}
        <div
          className="w-[280px] flex-shrink-0 flex flex-col border-r"
          style={{ borderColor: 'rgba(240,205,120,.2)', background: 'rgba(0,0,0,.3)' }}
        >
          <div className="flex items-center justify-between px-5 py-4 border-b" style={{ borderColor: 'rgba(240,205,120,.2)' }}>
            <span
              className="font-bold text-[11px] tracking-[2.5px] uppercase text-[var(--gold)]"
              style={{ fontFamily: 'var(--font-heading)' }}
            >
              Meus Baralhos
            </span>
            <button
              onClick={openNew}
              className="flex items-center gap-1.5 px-3 py-1 rounded border-none cursor-pointer text-[11px] font-semibold text-[var(--bg-darkest)]"
              style={{ background: 'linear-gradient(180deg, var(--gold-light), var(--gold))' }}
            >
              <Plus size={12} strokeWidth={2.5} />
              Novo
            </button>
          </div>
          <div className="flex-1 overflow-y-auto">
            {decks.length === 0 && (
              <div className="px-5 py-8 text-center">
                <p className="italic text-[13px] text-[var(--text-muted)]" style={{ fontFamily: 'var(--font-body)' }}>
                  Nenhum baralho ainda
                </p>
                <button
                  onClick={openNew}
                  className="mt-3 text-[12px] underline text-[var(--gold)] bg-transparent border-none cursor-pointer"
                >
                  Criar agora
                </button>
              </div>
            )}
            {decks.map((d) => (
              <div
                key={d.id}
                className="flex items-center gap-2 px-4 py-3 border-b cursor-pointer"
                style={{ borderColor: 'rgba(240,205,120,.1)' }}
                onClick={() => openEdit(d)}
              >
                <div className="flex-1 min-w-0">
                  <div className="font-semibold text-[13px] text-[var(--text-primary)] truncate">{d.name}</div>
                  <div className="text-[11px] text-[var(--text-muted)]">
                    {totalCount(d.cards)} cartas
                  </div>
                </div>
                <button
                  onClick={(ev) => { ev.stopPropagation(); handleDelete(d.id) }}
                  className="flex-shrink-0 bg-transparent border-none cursor-pointer text-[var(--text-muted)] hover:text-[var(--red)]"
                >
                  <Trash2 size={13} />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Empty state */}
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <p
              className="text-[17px] italic text-[var(--text-muted)] mb-4"
              style={{ fontFamily: 'var(--font-body)' }}
            >
              Selecione um baralho ou crie um novo
            </p>
            <button
              onClick={openNew}
              className="px-6 py-3 rounded-[8px] font-bold text-[13px] text-[var(--bg-darkest)] border-none cursor-pointer"
              style={{ background: 'linear-gradient(180deg, var(--gold-light), var(--gold))' }}
            >
              Criar Baralho
            </button>
          </div>
        </div>
      </div>
    )
  }

  // Editor mode
  return (
    <div className="absolute inset-0 flex flex-col overflow-hidden">
      {/* Editor header */}
      <div
        className="flex items-center gap-4 px-5 py-3 border-b flex-shrink-0"
        style={{ borderColor: 'rgba(240,205,120,.2)', background: 'rgba(0,0,0,.4)' }}
      >
        <button
          onClick={closeEditor}
          className="flex items-center gap-1.5 bg-transparent border-none cursor-pointer text-[var(--text-muted)] text-[12px]"
        >
          <ChevronLeft size={14} />
          Voltar
        </button>
        <input
          type="text"
          value={editor.name}
          onChange={(e) => setEditor({ ...editor, name: e.target.value })}
          placeholder="Nome do baralho"
          className="flex-1 bg-transparent border-none outline-none text-[16px] font-bold text-[var(--gold-light)]"
          style={{ fontFamily: 'var(--font-heading)' }}
        />
        {/* Faction pills */}
        <div className="flex gap-1.5">
          {FACTIONS.map((f) => (
            <button
              key={f.value}
              onClick={() => setEditor({ ...editor, faction: f.value, leaderId: '', cards: [] })}
              className="px-2.5 py-1 rounded text-[10px] font-bold tracking-wide border-none cursor-pointer"
              style={{
                background: editor.faction === f.value ? 'var(--gold)' : 'rgba(240,205,120,.1)',
                color: editor.faction === f.value ? 'var(--bg-darkest)' : 'var(--gold)',
                boxShadow: editor.faction === f.value ? 'none' : 'inset 0 0 0 1px rgba(240,205,120,.3)',
              }}
            >
              {f.label.split(' ')[0]}
            </button>
          ))}
        </div>
        {error && <span className="text-[11px] text-[var(--red)]">{error}</span>}
        <button
          onClick={handleSave}
          disabled={saving}
          className="flex items-center gap-1.5 px-4 py-2 rounded border-none cursor-pointer font-bold text-[12px] text-[var(--bg-darkest)]"
          style={{ background: 'linear-gradient(180deg, var(--gold-light), var(--gold))', opacity: saving ? 0.6 : 1 }}
        >
          <Check size={14} strokeWidth={2.5} />
          {saving ? 'Salvando…' : 'Salvar'}
        </button>
      </div>

      {/* Editor body */}
      <div className="flex flex-1 min-h-0">
        {/* Catalog panel */}
        <div className="flex-1 flex flex-col min-w-0 border-r" style={{ borderColor: 'rgba(240,205,120,.15)' }}>
          <div
            className="px-4 py-2 text-[10px] font-bold tracking-[2px] uppercase text-[var(--gold)] border-b flex-shrink-0"
            style={{ borderColor: 'rgba(240,205,120,.15)', background: 'rgba(0,0,0,.2)' }}
          >
            Catálogo · {FACTIONS.find((f) => f.value === editor.faction)?.label ?? editor.faction}
          </div>
          <div className="flex-1 overflow-y-auto p-3">
            {nonLeaderCatalog.map((card) => {
              const entry = editor.cards.find((e) => e.cardId === card.id)
              const qty = entry?.quantity ?? 0
              const maxCopies = card.cardType === 'HERO' ? 1 : card.deckCopies
              const atMax = qty >= maxCopies
              return (
                <div
                  key={card.id}
                  className="flex items-center gap-3 px-3 py-2 rounded-[6px] mb-1.5 cursor-pointer"
                  style={{
                    background: qty > 0 ? 'rgba(240,205,120,.08)' : 'rgba(255,255,255,.03)',
                    boxShadow: qty > 0 ? 'inset 0 0 0 1px rgba(240,205,120,.25)' : 'none',
                    opacity: atMax ? 0.5 : 1,
                  }}
                  onClick={() => !atMax && addCard(card)}
                >
                  <div
                    className="w-6 h-6 rounded flex items-center justify-center text-[11px] font-bold flex-shrink-0"
                    style={{ background: 'rgba(0,0,0,.4)', color: 'var(--gold)' }}
                  >
                    {card.basePower ?? '—'}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="text-[12px] font-semibold text-[var(--text-primary)] truncate">{card.name}</div>
                    <div className="text-[10px] text-[var(--text-muted)]">
                      {card.cardType} {card.rowType ? `· ${card.rowType}` : ''} {card.ability ? `· ${card.ability}` : ''}
                    </div>
                  </div>
                  <div className="flex-shrink-0 text-[11px] text-[var(--text-muted)]">
                    {qty > 0 && <span className="text-[var(--gold)] font-bold">{qty}/</span>}{maxCopies}
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        {/* Deck preview panel */}
        <div
          className="w-[320px] flex-shrink-0 flex flex-col"
          style={{ background: 'rgba(0,0,0,.25)' }}
        >
          {/* Leader */}
          <div className="px-4 py-3 border-b" style={{ borderColor: 'rgba(240,205,120,.15)' }}>
            <div className="text-[10px] font-bold tracking-[2px] uppercase text-[var(--gold)] mb-2">Líder</div>
            <select
              value={editor.leaderId}
              onChange={(e) => setEditor({ ...editor, leaderId: e.target.value })}
              className="w-full px-2 py-1.5 rounded text-[12px] text-[var(--text-primary)] bg-[var(--bg-darkest)] border-none outline-none cursor-pointer"
              style={{ boxShadow: 'inset 0 0 0 1px var(--border-gold)', fontFamily: 'var(--font-ui)' }}
            >
              <option value="">Selecione um líder</option>
              {leaders.map((l) => (
                <option key={l.id} value={l.id}>{l.name}</option>
              ))}
            </select>
          </div>

          {/* Card count badge */}
          <div className="px-4 py-2 border-b flex items-center gap-2" style={{ borderColor: 'rgba(240,205,120,.15)' }}>
            <span className="text-[10px] font-bold tracking-[2px] uppercase text-[var(--gold)]">Cartas</span>
            <span
              className="text-[13px] font-bold"
              style={{ color: count < 22 ? 'var(--red)' : count > 40 ? 'var(--red)' : 'var(--green)' }}
            >
              {count}
            </span>
            <span className="text-[11px] text-[var(--text-muted)]">/ 22–40</span>
          </div>

          {/* Card list */}
          <div className="flex-1 overflow-y-auto p-3">
            {editor.cards.length === 0 && (
              <p className="text-center text-[12px] italic text-[var(--text-muted)] mt-4" style={{ fontFamily: 'var(--font-body)' }}>
                Clique nas cartas do catálogo para adicioná-las
              </p>
            )}
            {editor.cards.map((entry) => {
              const card = cardById[entry.cardId]
              return (
                <div key={entry.cardId} className="flex items-center gap-2 py-1.5 border-b" style={{ borderColor: 'rgba(255,255,255,.05)' }}>
                  <span className="text-[11px] font-bold text-[var(--gold)] w-5 text-center">{entry.quantity}×</span>
                  <span className="flex-1 text-[12px] text-[var(--text-primary)] truncate">{card?.name ?? entry.cardId}</span>
                  <button
                    onClick={() => removeCard(entry.cardId)}
                    className="bg-transparent border-none cursor-pointer text-[var(--text-muted)] flex-shrink-0"
                  >
                    <X size={12} />
                  </button>
                </div>
              )
            })}
          </div>
        </div>
      </div>
    </div>
  )
}