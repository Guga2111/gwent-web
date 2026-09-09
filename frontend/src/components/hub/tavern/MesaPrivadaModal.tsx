import { useNavigate } from 'react-router-dom'
import { Plus, LogIn } from 'lucide-react'
import { useMesaPrivada } from '@/hooks/useMesaPrivada'
import { Button } from '../../ui/button'
import { Card, CardContent, CardTitle, CardDescription } from '../../ui/card'
import { Input } from '../../ui/input'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from '@/components/ui/select'

interface MesaPrivadaModalProps {
  open: boolean
  onClose: () => void
  onCreateGame: (deckId: string) => Promise<string>
  onJoinGame: (code: string, deckId: string) => Promise<void>
  defaultDeckId: string | null
}

export default function MesaPrivadaModal({ open, onClose, onCreateGame, onJoinGame, defaultDeckId }: MesaPrivadaModalProps) {
  const navigate = useNavigate()

  const {
    decks, selectedDeckId, setSelectedDeckId, createdId, joinCode, setJoinCode,
    showJoinInput, setShowJoinInput, loading, error, handleCreate, handleJoin, handleClose,
  } = useMesaPrivada(open, defaultDeckId, onCreateGame, onJoinGame, onClose)

  return (
    <Dialog open={open} onOpenChange={(v) => !v && handleClose()}>
      <DialogContent className="mq-panel border-none max-w-[560px] px-[42px] py-10">
        {/* Header */}
        <DialogHeader className="text-center mb-[26px]">
          <DialogTitle className="text-[10.5px] tracking-[4px] uppercase font-bold text-gold font-heading">
            Mesa Privada
          </DialogTitle>
          <div className="mt-1.5 text-[23px] font-bold text-gold-light tracking-[1px] font-display">
            Uma Amistosa entre Amigos
          </div>
          <DialogDescription className="sr-only">
            Crie ou entre numa partida privada com um amigo
          </DialogDescription>
        </DialogHeader>

        {/* Deck selector */}
        {decks.length > 0 && (
          <div className="mb-5">
            <div className="text-[10px] tracking-[2px] uppercase font-bold text-gold mb-1.5 font-heading">
              Baralho
            </div>
            <Select
              value={selectedDeckId ?? ''}
              onValueChange={(value) => setSelectedDeckId(value)}
            >
              <SelectTrigger className="font-ui w-full px-2 py-1.5 rounded text-sm text-text-primary bg-bg-darkest border-none outline-none cursor-pointer">
                <SelectValue placeholder="Baralho" />
              </SelectTrigger>
              <SelectContent position="popper" className="bg-bg-dark border-gold/30">
                <SelectGroup>
                  <SelectLabel>Baralhos</SelectLabel>
                  {decks.map((d) => (
                    <SelectItem key={d.id} value={d.id}>
                      {d.name} ({d.cards.reduce((s, c) => s + c.quantity, 0)} cartas)
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>
        )}

        {decks.length === 0 && (
          <p className="text-[13px] text-center text-gold mb-4 font-body">
            Crie um baralho na Forja antes de jogar
          </p>
        )}

        {error && (
          <p className="text-[13px] text-center text-red mb-4">{error}</p>
        )}

        {/* Two side-by-side option cards */}
        <div className="flex gap-4">
          {/* Left card — Criar Mesa */}
          <Card
            className={`flex-1 items-center border-none bg-gradient-to-b from-bg-card to-bg-dark ring-1 ring-inset ring-gold/30 shadow-lg ${createdId ? 'cursor-default' : 'cursor-pointer'}`}
            onClick={!createdId && !loading ? handleCreate : undefined}
          >
            <CardContent className="flex flex-col items-center gap-3 px-3">
              {!createdId ? (
                <>
                  <div className="size-[52px] rounded-full flex items-center justify-center text-bg-darkest bg-gradient-to-br from-gold-light to-gold-dark shadow-md">
                    <Plus size={26} strokeWidth={1.8} />
                  </div>
                  <CardTitle className="text-[15px] text-gold-light tracking-[.5px] font-heading">
                    {loading ? '...' : 'Criar Mesa'}
                  </CardTitle>
                  <CardDescription className="text-[12.5px] text-text-muted text-center leading-[1.35] font-body">
                    Gere um código e convide um amigo pelo link
                  </CardDescription>
                </>
              ) : (
                <>
                  <div className="text-[13px] text-green mb-1 font-body">
                    Partida criada!
                  </div>
                  <code className="block px-4 py-3 rounded-lg bg-bg-darkest text-gold-light font-mono text-[13px] break-all select-all text-center">
                    {createdId}
                  </code>
                  <Button
                    variant="cta"
                    onClick={() => navigate(`/game/${createdId}`)}
                    className="mt-1 px-5 py-2.5 rounded-[7px] text-[13px]"
                  >
                    Entrar na Partida
                  </Button>
                </>
              )}
            </CardContent>
          </Card>

          {/* Right card — Entrar com Código */}
          <Card
            className={`flex-1 items-center border-none bg-gradient-to-b from-bg-card to-bg-dark ring-1 ring-inset ring-gold/30 shadow-lg ${showJoinInput ? 'cursor-default' : 'cursor-pointer'}`}
            onClick={!showJoinInput ? () => setShowJoinInput(true) : undefined}
          >
            <CardContent className="flex flex-col items-center gap-3 px-3">
              {!showJoinInput ? (
                <>
                  <div className="size-[52px] rounded-full flex items-center justify-center text-gold bg-black/30 ring-[1.5px] ring-inset ring-gold/45">
                    <LogIn size={26} strokeWidth={1.8} />
                  </div>
                  <CardTitle className="text-[15px] text-gold-light tracking-[.5px] font-heading whitespace-nowrap">
                    Entrar com Código
                  </CardTitle>
                  <CardDescription className="text-[12.5px] text-text-muted text-center leading-[1.35] font-body">
                    Recebeu um convite? Digite o código aqui
                  </CardDescription>
                </>
              ) : (
                <>
                  <Input
                    type="text"
                    value={joinCode}
                    onChange={(e) => setJoinCode(e.target.value)}
                    placeholder="Cole o código aqui"
                    autoFocus
                    className="border-none text-center text-sm text-text-primary bg-bg-darkest ring-1 ring-inset ring-gold font-ui"
                  />
                  <Button
                    variant="cta"
                    disabled={loading || !joinCode.trim()}
                    onClick={handleJoin}
                    className="px-5 py-2.5 rounded-[7px] text-[13px]"
                  >
                    {loading ? '...' : 'Entrar'}
                  </Button>
                </>
              )}
            </CardContent>
          </Card>
        </div>
      </DialogContent>
    </Dialog>
  )
}
