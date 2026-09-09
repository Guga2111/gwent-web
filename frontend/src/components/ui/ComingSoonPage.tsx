interface ComingSoonPageProps {
  title: string
}

export default function ComingSoonPage({ title }: ComingSoonPageProps) {
  return (
    <div className="absolute inset-0 flex items-center justify-center">
      <div
        className="px-[60px] py-10 rounded-[9px] text-center"
        style={{
          background: 'linear-gradient(155deg, var(--parchment-light), var(--parchment-mid) 55%, var(--parchment-dark))',
          boxShadow: '0 16px 34px rgba(0,0,0,.55), inset 0 0 36px rgba(150,115,60,.32)',
        }}
      >
        <div
          className="text-[26px] font-bold text-parchment-heading mb-2 font-display"
        >
          {title}
        </div>
        <div
          className="text-[15px] text-parchment-muted font-body"
        >
          Em breve
        </div>
      </div>
    </div>
  )
}
