import { ChevronLeft, Check } from 'lucide-react'
import type { Faction } from '@/types/deck'
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

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
      <Button
        onClick={onBack}
        variant="outline"
        className="flex items-center gap-1.5 bg-transparent border-none cursor-pointer text-text-muted text-sm"
      >
        <ChevronLeft size={14} />
        Voltar
      </Button>
      <Input
        type="text"
        value={name}
        onChange={(e) => onNameChange(e.target.value)}
        placeholder="Nome do baralho"
        className="font-heading flex-1 bg-transparent border-none outline-none text-lg font-bold text-gold-light"
      />
      <div className="flex gap-1.5">
        {FACTIONS.map((f) => (
          <Button
            key={f.value}
            onClick={() => onFactionChange(f.value)}
            variant={faction === f.value ? 'cta' : 'ghost'}
            size="pill"
            className="font-bold tracking-wide cursor-pointer"
          >
            {f.label.split(' ')[0]}
          </Button>
        ))}
      </div>
      {error && <span className="text-xs text-red">{error}</span>}
      <Button
        onClick={onSave}
        disabled={saving}
        variant="cta"
        className={`flex items-center gap-1.5 px-4 py-2 rounded text-sm ${saving ? 'opacity-60' : ''}`}
      >
        <Check size={14} strokeWidth={2.5} />
        {saving ? 'Salvando…' : 'Salvar'}
      </Button>
    </div>
  )
}
