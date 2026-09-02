import { DeckListSidebar } from '@/components/hub/deckforge/DeckListSidebar'
import { DeckEditorHeader } from '@/components/hub/deckforge/DeckEditorHeader'
import { CatalogPanel } from '@/components/hub/deckforge/CatalogPanel'
import { DeckPreviewPanel } from '@/components/hub/deckforge/DeckPreviewPanel'
import { useDeckEditor } from '@/hooks/useDeckEditor'

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
            <p className="font-body text-[17px]text-[var(--text-muted)] mb-4">
              Selecione um baralho ou crie um novo
            </p>
            <button
              onClick={openNew}
              className="px-6 py-3 rounded-[8px] font-bold text-[13px] text-[var(--bg-darkest)] border-none cursor-pointer btn-gold"
            >
              Criar Baralho
            </button>
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
