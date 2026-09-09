import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useState } from 'react'
import { IconSwords, IconShield, IconUsers, IconExternalLink, IconBrandGithub, IconBrandLinkedin } from '@tabler/icons-react'
import RulesModal from '@/components/hub/RulesModal'
import { Button } from '@/components/ui/button'
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from '@/components/ui/accordion'
import { AnimatedTestimonials } from '@/components/ui/animated-testimonials'
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from '@/components/ui/carousel'

const factionShowcase = [
  {
    name: 'Reinos do Norte',
    designation: 'Forca e Resiliencia',
    quote:
      'Fortificacoes poderosas e unidades com lacos estreitos. Combine infantaria leal com cercos devastadores para esmagar seus oponentes com forca bruta e taticas de campo.',
    src: '/cards/northern/Tw3_cardart_northernrealms_foltest_gold.webp',
  },
  {
    name: 'Nilfgaard',
    designation: 'Espionagem e Controle',
    quote:
      'O Imperio domina pelo conhecimento. Espie a mao do oponente, roube suas cartas e manipule o campo de batalha. Vencer e uma questao de informacao, nao de forca.',
    src: '/cards/nilfgaard/Tw3_cardart_nilfgaard_letho.webp',
  },
  {
    name: 'Monstros',
    designation: 'Enxame e Devastacao',
    quote:
      'A Cacada Selvagem traz o caos ao campo. Invoque hordas imparaveis, devore seus proprios aliados para ficar mais forte e domine pelo numero absoluto de criaturas.',
    src: '/cards/monster/Tw3_cardart_monsters_eredin_gold.webp',
  },
  {
    name: "Scoia'tael",
    designation: 'Agilidade e Emboscada',
    quote:
      'Guerrilheiros mestres da adaptacao. Unidades ageis que escolhem sua fileira, emboscadas que surpreendem e arqueiros elfos que eliminam ameacas antes que se tornem perigosas.',
    src: '/cards/scoiatael/ST_DOL_BLATHANNA.webp',
  },
  {
    name: 'Neutro',
    designation: 'Poder Independente',
    quote:
      'Herois lendarios que nao juram lealdade a nenhuma bandeira. Geralt, Ciri e Yennefer trazem habilidades unicas que podem virar qualquer partida de cabeca para baixo.',
    src: '/cards/neutral/Tw3_cardart_neutral_geralt.webp',
  },
]

const features = [
  {
    icon: IconSwords,
    title: 'Batalhas em Tres Rounds',
    description:
      'Tabuleiro com tres fileiras por lado: corpo a corpo, distancia e cerco. Venca dois de tres rounds usando tatica, blefe e timing perfeito.',
    image: '/landing/feature-battles.png',
  },
  {
    icon: IconShield,
    title: 'Forja de Baralhos',
    description:
      'Monte baralhos com cinco facoes e escolha seu lider. Reinos do Norte, Nilfgaard, Monstros, Scoia\'tael e Skellige, cada uma com estrategias unicas.',
    image: '/landing/feature-deckbuilding.png',
  },
  {
    icon: IconUsers,
    title: 'Desafie Outros Jogadores',
    description:
      'Entre na fila ranqueada ou convide amigos para duelos privados na taverna. Matchmaking em tempo real com reconexao automatica.',
    image: '/landing/feature-multiplayer.png',
  },
]

export default function Landing() {
  const [rulesOpen, setRulesOpen] = useState(false)

  return (
    <div className="min-h-screen bg-bg-darkest text-text-primary">

      {/* Hero Section */}
      <section className="relative h-screen flex items-center justify-center overflow-hidden">
        <div
          className="absolute inset-0 bg-cover bg-center opacity-40"
          style={{ backgroundImage: "url('/the_witcher_3_gwent.webp')" }}
        />
        <div className="absolute inset-0 bg-gradient-to-b from-bg-darkest/70 via-transparent to-bg-darkest" />
        <div className="relative z-10 text-center flex flex-col items-center gap-6 px-4">
          <img src="/gwent-logo.svg" alt="Gwent" className="h-28 w-28" />
          <h1 className="font-display text-5xl md:text-6xl text-gold-light tracking-wide">
            GWENT ONLINE
          </h1>
          <p className="font-body italic text-text-secondary text-lg max-w-md">
            O lendario jogo de cartas do Continente, agora nas tavernas da web.
          </p>
          <div className="flex gap-4 mt-4">
            <Button variant="cta" size="lg" asChild>
              <Link to="/login">JOGAR AGORA</Link>
            </Button>
            <Button
              variant="outline"
              size="lg"
              onClick={() =>
                document
                  .getElementById('features')
                  ?.scrollIntoView({ behavior: 'smooth' })
              }
            >
              COMO JOGAR
            </Button>
          </div>
        </div>
        <motion.div
          className="absolute bottom-8 z-10"
          animate={{ y: [0, 8, 0] }}
          transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
        >
          <svg
            className="w-6 h-6 text-gold/60"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
          </svg>
        </motion.div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-24 px-6">
        <div className="max-w-5xl mx-auto">
          <h2 className="font-display text-3xl text-gold-light text-center mb-16">
            Forjado nas Tavernas do Continente
          </h2>
          <Carousel className="mx-auto max-w-4xl">
            <CarouselContent>
              {features.map((feature) => (
                <CarouselItem key={feature.title}>
                  <div className="relative aspect-video rounded-lg overflow-hidden">
                    <img
                      src={feature.image}
                      alt={feature.title}
                      className="w-full h-full object-cover"
                    />
                    <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-bg-darkest/90 via-bg-darkest/50 to-transparent p-6 pt-16">
                      <div className="flex items-center justify-center gap-2 mb-2">
                        <feature.icon className="h-6 w-6 text-gold" />
                        <h3 className="font-heading text-gold-light text-lg">
                          {feature.title}
                        </h3>
                      </div>
                      <p className="font-body text-text-secondary text-sm text-center max-w-md mx-auto">
                        {feature.description}
                      </p>
                    </div>
                  </div>
                </CarouselItem>
              ))}
            </CarouselContent>
            <CarouselPrevious className="border-gold text-gold hover:bg-gold/10 hover:text-gold-light" />
            <CarouselNext className="border-gold text-gold hover:bg-gold/10 hover:text-gold-light" />
          </Carousel>
        </div>
      </section>

      {/* Faction Showcase */}
      <section className="py-16 px-6">
        <h2 className="font-display text-3xl text-gold-light text-center mb-4">
          Cinco Facoes, Infinitas Estrategias
        </h2>
        <AnimatedTestimonials testimonials={factionShowcase} autoplay />
      </section>

      {/* FAQ Section */}
      <section className="py-24 px-6">
        <div className="max-w-3xl mx-auto">
          <h2 className="font-display text-3xl text-gold-light text-center mb-12">
            Perguntas Frequentes
          </h2>
          <Accordion type="single" collapsible className="space-y-2">
            <AccordionItem value="what-is" className="border-border-subtle">
              <AccordionTrigger className="font-heading text-gold-light hover:text-gold hover:no-underline text-left">
                O que e Gwent Online?
              </AccordionTrigger>
              <AccordionContent className="font-body text-text-secondary">
                Gwent Online e uma versao web do lendario jogo de cartas do universo de The Witcher.
                Dois jogadores se enfrentam em duelos estrategicos usando baralhos compostos por
                unidades, herois e cartas especiais, disputando a supremacia no campo de batalha.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="free" className="border-border-subtle">
              <AccordionTrigger className="font-heading text-gold-light hover:text-gold hover:no-underline text-left">
                Gwent Online e gratuito?
              </AccordionTrigger>
              <AccordionContent className="font-body text-text-secondary">
                Sim! Gwent Online e totalmente gratuito para jogar. Basta criar uma conta e voce
                ja pode montar seus baralhos e desafiar outros jogadores sem nenhum custo.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="how" className="border-border-subtle">
              <AccordionTrigger className="font-heading text-gold-light hover:text-gold hover:no-underline text-left">
                Como funciona uma partida?
              </AccordionTrigger>
              <AccordionContent className="font-body text-text-secondary">
                Cada partida e disputada em melhor de tres rounds. Em cada round, jogadores
                alternam turnos colocando cartas em tres fileiras de combate — corpo a corpo,
                distancia e cerco. O jogador com maior forca total ao final do round vence.
                Saber quando passar e tao importante quanto saber jogar.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="factions" className="border-border-subtle">
              <AccordionTrigger className="font-heading text-gold-light hover:text-gold hover:no-underline text-left">
                Quantas facoes existem?
              </AccordionTrigger>
              <AccordionContent className="font-body text-text-secondary">
                Existem cinco facoes jogaveis: Reinos do Norte (forca bruta), Nilfgaard
                (espionagem e controle), Monstros (enxame e devastacao), Scoia'tael (agilidade
                e emboscada) e cartas Neutras que podem ser usadas em qualquer baralho.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="friends" className="border-border-subtle">
              <AccordionTrigger className="font-heading text-gold-light hover:text-gold hover:no-underline text-left">
                Posso jogar com amigos?
              </AccordionTrigger>
              <AccordionContent className="font-body text-text-secondary">
                Claro! Alem da fila ranqueada, voce pode criar partidas privadas e convidar
                amigos para duelos amistosos diretamente na taverna. Basta compartilhar o
                codigo da sala.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="witcher" className="border-border-subtle">
              <AccordionTrigger className="font-heading text-gold-light hover:text-gold hover:no-underline text-left">
                Preciso conhecer The Witcher para jogar?
              </AccordionTrigger>
              <AccordionContent className="font-body text-text-secondary">
                Nao! Embora o jogo seja inspirado no universo de The Witcher, nenhum
                conhecimento previo e necessario. As regras sao simples de aprender e a
                diversao e garantida para todos, fas ou nao da serie.
              </AccordionContent>
            </AccordionItem>

            <AccordionItem value="credits" className="border-border-subtle">
              <AccordionTrigger className="font-heading text-gold-light hover:text-gold hover:no-underline text-left">
                Quem criou o Gwent original?
              </AccordionTrigger>
              <AccordionContent className="font-body text-text-secondary">
                Gwent foi criado pela CD Projekt Red como minigame em The Witcher 3: Wild Hunt.
                Todas as artes de cartas, conceitos e propriedade intelectual pertencem a CD Projekt Red.
                Este projeto e um tributo feito por fas, sem fins lucrativos e sem afiliacao oficial
                com a CD Projekt Red ou suas subsidiarias.
              </AccordionContent>
            </AccordionItem>
          </Accordion>
        </div>
      </section>

      {/* CTA Footer */}
      <section className="relative py-32 px-6 overflow-hidden">
        <div className="absolute inset-0 from-bg-darkest via-bg-darkest/80 to-bg-darkest" />

        <motion.div
          className="relative z-10 flex flex-col items-center text-center max-w-2xl mx-auto"
        >
          {/* Decorative separator */}
          <div className="flex items-center gap-4 mb-8">
            <div className="h-px w-16 from-transparent to-gold/60" />
            <IconSwords className="h-6 w-6 text-gold" />
            <div className="h-px w-16 from-transparent to-gold/60" />
          </div>

          <h2 className="font-display text-4xl md:text-5xl text-gold-light mb-4 tracking-wide">
            Sua Mesa Aguarda
          </h2>

          <p className="font-body text-text-secondary text-lg mb-10 max-w-md">
            As cartas estao embaralhadas, a taverna esta cheia. So falta voce.
          </p>

          <Button
            variant="cta"
            size="lg"
            asChild
            className="font-heading text-base tracking-wider px-10 py-3 h-auto"
          >
            <Link to="/login">CRIAR CONTA</Link>
          </Button>

          <p className="mt-6 text-text-muted font-body text-sm">
            Ja tem uma conta?{' '}
            <Link to="/login" className="text-gold hover:text-gold-light underline">
              Entrar
            </Link>
          </p>
        </motion.div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border-subtle bg-bg-dark">
        <div className="max-w-6xl mx-auto px-6 py-12">
          {/* Top Row */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-10">
            {/* Brand */}
            <div className="md:col-span-1">
              <div className="flex items-center gap-2 mb-3">
                <img src="/gwent-logo.svg" alt="Gwent" className="h-8 w-8" />
                <span className="font-display text-gold-light text-lg">GWENT ONLINE</span>
              </div>
              <p className="font-body italic text-text-secondary text-sm leading-relaxed">
                O lendario jogo de cartas do universo de The Witcher, agora nas tavernas da web.
              </p>
            </div>

            {/* Link Columns */}
            <div>
              <h4 className="font-heading text-gold-light text-sm uppercase tracking-wider mb-4">Jogo</h4>
              <ul className="space-y-2">
                <li><Link to="/login" className="font-body text-text-secondary hover:text-gold text-sm">Jogar</Link></li>
                <li>
                  <button
                    onClick={() => setRulesOpen(true)}
                    className="font-body text-text-secondary hover:text-gold text-sm"
                  >
                    Regras
                  </button>
                </li>
                <li>
                  <button
                    onClick={() => document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })}
                    className="font-body text-text-secondary hover:text-gold text-sm"
                  >
                    Fações
                  </button>
                </li>
              </ul>
            </div>

            <div>
              <h4 className="font-heading text-gold-light text-sm uppercase tracking-wider mb-4">Comunidade</h4>
              <ul className="space-y-2">
                <li>
                  <a href="https://github.com/Guga2111/gwent-web" target="_blank" rel="noopener noreferrer" className="font-body text-text-secondary hover:text-gold text-sm inline-flex items-center gap-1">
                    GitHub <IconExternalLink className="h-3 w-3" />
                  </a>
                </li>
                <li>
                  <a href="https://www.linkedin.com/in/luisgosampaio/" target="_blank" rel="noopener noreferrer" className="font-body text-text-secondary hover:text-gold text-sm inline-flex items-center gap-1">
                    LinkedIn <IconExternalLink className="h-3 w-3" />
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <h4 className="font-heading text-gold-light text-sm uppercase tracking-wider mb-4">Legal</h4>
              <ul className="space-y-2">
                <li><a href="#" className="font-body text-text-secondary hover:text-gold text-sm">Termos de Uso</a></li>
                <li><a href="#" className="font-body text-text-secondary hover:text-gold text-sm">Privacidade</a></li>
              </ul>
            </div>
          </div>

          {/* Bottom Row */}
          <div className="border-t border-border-subtle mt-10 pt-6 flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="text-text-muted font-body text-xs text-center md:text-left">
              <p>&copy; 2026 Gwent Online. Projeto de fas sem fins lucrativos.</p>
              <p>Gwent e The Witcher sao marcas registradas da CD Projekt Red.</p>
            </div>
            <div className="flex items-center gap-4">
              <a href="https://github.com/Guga2111/gwent-web" target="_blank" rel="noopener noreferrer" className="text-text-muted hover:text-gold">
                <IconBrandGithub className="h-5 w-5" />
              </a>
              <a href="https://www.linkedin.com/in/luisgosampaio/" target="_blank" rel="noopener noreferrer" className="text-text-muted hover:text-gold">
                <IconBrandLinkedin className="h-5 w-5" />
              </a>
            </div>
          </div>
        </div>
      </footer>

      <RulesModal open={rulesOpen} onClose={() => setRulesOpen(false)} />
    </div>
  )
}
