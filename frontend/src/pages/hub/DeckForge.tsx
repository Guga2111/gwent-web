import { DeckListSidebar } from '@/components/hub/deckforge/DeckListSidebar'
import { DeckEditorHeader } from '@/components/hub/deckforge/DeckEditorHeader'
import { CatalogPanel } from '@/components/hub/deckforge/CatalogPanel'
import { DeckPreviewPanel } from '@/components/hub/deckforge/DeckPreviewPanel'
import { useDeckEditor } from '@/hooks/useDeckEditor'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'

export default function DeckForge() {
  const {
    decks, editor, leaders, nonLeaderCatalog, cardById,
    saving, error, openNew, openEdit, closeEditor,
    handleDelete, handleSave, addCard, removeCard, setEditorField,
  } = useDeckEditor()

  if (!editor) {
    return (
      <div className="absolute inset-0 flex overflow-hidden">
        <DeckListSidebar
          decks={decks}
          onNew={openNew}
          onEdit={openEdit}
          onDelete={handleDelete}
        />
        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <Label className="font-body text-lg text-text-muted mb-4 block">
              Selecione um baralho ou crie um novo
            </Label>
            <Button
              onClick={openNew}
              variant="cta"
              className="px-6 py-3 text-sm"
            >
              Criar Baralho
            </Button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="absolute inset-0 flex flex-col overflow-hidden">
      <DeckEditorHeader
        name={editor.name}
        faction={editor.faction}
        saving={saving}
        error={error}
        onBack={closeEditor}
        onNameChange={(name) => setEditorField({ name })}
        onFactionChange={(faction) => setEditorField({ faction, leaderId: '', cards: [] })}
        onSave={handleSave}
      />
      <div className="flex flex-1 min-h-0">
        <CatalogPanel
          faction={editor.faction}
          catalog={nonLeaderCatalog}
          editorCards={editor.cards}
          onAdd={addCard}
        />
        <DeckPreviewPanel
          leaders={leaders}
          leaderId={editor.leaderId}
          cards={editor.cards}
          cardById={cardById}
          onLeaderChange={(leaderId) => setEditorField({ leaderId })}
          onRemove={removeCard}
        />
      </div>
    </div>
  )
}
