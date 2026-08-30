import { ChevronLeft, Check } from 'lucide-react'
import type { Faction } from '@/types/deck'

export const FACTIONS: { value: Faction; label: string }[] = [
  { value: 'NORTHERN_REALMS', label: 'Reinos do Norte' },
  { value: 'NILFGAARD', label: 'Nilfgaard' },
  { value: 'MONSTER', label: 'Monstros' },
  { value: 'SCOIATAEL', label: 'Scoia\'tael' },
]

interface DeckEditorHeaderProps {
  name: string
  faction: Faction
  saving: boolean
  error: string
  onBack: () => void
  onNameChange: (name: string) => void
  onFactionChange: (f: Faction) => void
  onSave: () => void
}

export function DeckEditorHeader({
  name, faction, saving, error, onBack, onNameChange, onFactionChange, onSave,
}: DeckEditorHeaderProps) {
  return (
    <div className="flex items-center gap-4 px-5 py-3 border-b flex-shrink-0 deckforge-editor-header">
      <button
        onClick={onBack}
        className="flex items-center gap-1.5 bg-transparent border-none cursor-pointer text-[var(--text-muted)] text-[12px]"
      >
        <ChevronLeft size={14} />
        Voltar
      </button>
      <input
        type="text"
        value={name}
        onChange={(e) => onNameChange(e.target.value)}
        placeholder="Nome do baralho"
        className="font-heading flex-1 bg-transparent border-none outline-none text-[16px] font-bold text-[var(--gold-light)]"
      />
      <div className="flex gap-1.5">
        {FACTIONS.map((f) => (
          <button
            key={f.value}
            onClick={() => onFactionChange(f.value)}
            className={`px-2.5 py-1 rounded text-[10px] font-bold tracking-wide border-none cursor-pointer ${faction === f.value ? 'deckforge-faction-pill--active' : 'deckforge-faction-pill'}`}
          >
            {f.label.split(' ')[0]}
          </button>
        ))}
      </div>
      {error && <span className="text-[11px] text-[var(--red)]">{error}</span>}
      <button
        onClick={onSave}
        disabled={saving}
        className={`flex items-center gap-1.5 px-4 py-2 rounded border-none cursor-pointer font-bold text-[12px] text-[var(--bg-darkest)] btn-gold ${saving ? 'opacity-60' : ''}`}
      >
        <Check size={14} strokeWidth={2.5} />
        {saving ? 'Salvando…' : 'Salvar'}
      </button>
    </div>
  )
}
