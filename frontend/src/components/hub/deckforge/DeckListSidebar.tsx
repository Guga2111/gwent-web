import { useState } from 'react'
import { Plus, Trash2 } from 'lucide-react'
import type { DeckDto, DeckCardEntryDto } from '@/types/deck'
import { getFactionConfig } from '@/utils/factionConfig'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

function totalCount(cards: DeckCardEntryDto[]) {
  return cards.reduce((s, e) => s + e.quantity, 0)
}

function countColorClass(count: number) {
  if (count === 25) return 'text-green'
  if (count >= 22 && count <= 40) return 'text-gold'
  return 'text-text-muted'
}

function SidebarHeader({ onNew }: { onNew: () => void }) {
  return (
    <div className="flex items-center justify-between px-5 py-4 border-b deckforge-sidebar-header">
      <Label className="font-heading font-bold text-xs tracking-[2.5px] uppercase text-gold">
        Meus Baralhos
      </Label>
      <Button
        onClick={onNew}
        variant="cta"
        size="sm"
        className="flex items-center gap-1.5 px-3 py-1 text-sm font-semibold"
      >
        <Plus size={12} strokeWidth={2.5} />
        Novo
      </Button>
    </div>
  )
}

function EmptyState({ onNew }: { onNew: () => void }) {
  return (
    <div className="px-5 py-8 text-center">
      <Label className="font-body text-sm text-text-muted">
        Nenhum baralho ainda
      </Label>
      <Button
        onClick={onNew}
        variant="link"
        className="mt-3 block mx-auto text-sm text-gold"
      >
        Criar agora
      </Button>
    </div>
  )
}

interface DeckListItemProps {
  deck: DeckDto
  onEdit: (d: DeckDto) => void
  onRequestDelete: (d: DeckDto) => void
}

function DeckListItem({ deck, onEdit, onRequestDelete }: DeckListItemProps) {
  const fc = getFactionConfig(deck.faction)
  const count = totalCount(deck.cards)
  const Emblem = fc.Emblem
  return (
    <Button
      variant="ghost"
      onClick={() => onEdit(deck)}
      className="group w-[calc(100%-16px)] h-auto justify-start flex items-center gap-2.5 mx-2 my-1 px-3 py-2.5 rounded-[5px] border-l-[3px] border-solid bg-white/[0.02] hover:bg-white/[0.06] cursor-pointer transition-colors"
      style={{ borderLeftColor: fc.tokens.accent }}
    >
      {Emblem && (
        <div className="flex-shrink-0 w-7 h-7" style={{ color: fc.tokens.primary }}>
          <Emblem />
        </div>
      )}
      <div className="flex-1 min-w-0">
        <span className="block font-heading font-bold text-[15px] text-text-primary truncate">
          {deck.name}
        </span>
        <span className="block text-xs text-text-muted">
          {fc.label} · <span className={countColorClass(count)}>{count}/25</span>
        </span>
      </div>
      <Button
        variant="ghost"
        size="icon"
        onClick={(ev) => { ev.stopPropagation(); onRequestDelete(deck) }}
        className="flex-shrink-0 opacity-0 group-hover:opacity-100 text-text-muted hover:text-red hover:bg-transparent transition-opacity h-7 w-7"
      >
        <Trash2 size={13} />
      </Button>
    </Button>
  )
}

interface DeleteDeckDialogProps {
  deck: DeckDto | null
  onConfirm: (id: string) => void
  onCancel: () => void
}

function DeleteDeckDialog({ deck, onConfirm, onCancel }: DeleteDeckDialogProps) {
  return (
    <AlertDialog open={!!deck} onOpenChange={(open) => { if (!open) onCancel() }}>
      <AlertDialogContent className="border-gold/30 bg-bg-dark text-text-primary">
        <AlertDialogHeader>
          <AlertDialogTitle className="font-heading text-gold-light">
            Apagar baralho
          </AlertDialogTitle>
          <AlertDialogDescription className="text-text-secondary">
            Tem certeza que deseja apagar{' '}
            <span className="font-semibold text-text-primary">{deck?.name}</span>?
            Esta ação é irreversível e irá:
            <ul className="mt-2 list-inside list-disc space-y-1 text-text-muted">
              <li>Remover permanentemente o baralho e todas as suas cartas</li>
              <li>Se este for o baralho ativo, você precisará selecionar outro para jogar</li>
            </ul>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel variant="outline" size="sm">
            Cancelar
          </AlertDialogCancel>
          <AlertDialogAction
            variant="destructive"
            size="sm"
            onClick={() => deck && onConfirm(deck.id)}
          >
            Apagar
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}

interface DeckListSidebarProps {
  decks: DeckDto[]
  onNew: () => void
  onEdit: (d: DeckDto) => void
  onDelete: (id: string) => void
}

export function DeckListSidebar({ decks, onNew, onEdit, onDelete }: DeckListSidebarProps) {
  const [deckToDelete, setDeckToDelete] = useState<DeckDto | null>(null)

  return (
    <div className="w-[280px] flex-shrink-0 flex flex-col border-r deckforge-sidebar">
      <SidebarHeader onNew={onNew} />
      <div className="flex-1 overflow-y-auto">
        {decks.length === 0 && <EmptyState onNew={onNew} />}
        {decks.map((d) => (
          <DeckListItem key={d.id} deck={d} onEdit={onEdit} onRequestDelete={setDeckToDelete} />
        ))}
      </div>
      <DeleteDeckDialog
        deck={deckToDelete}
        onConfirm={(id) => { onDelete(id); setDeckToDelete(null) }}
        onCancel={() => setDeckToDelete(null)}
      />
    </div>
  )
}
