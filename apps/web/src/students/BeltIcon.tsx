import { useId } from 'react'

import { beltInfo } from './belts'

type Point = readonly [number, number]
type Tail = { corners: string; across: [Point, Point]; at: (t: number) => [Point, Point] }

const TAIL_WIDTH = 5.2
/** Pontas da faixa saindo do nó (início → fim), no viewBox 40×26. */
const LEFT_TAIL = makeTail([17.5, 13.5], [11.5, 25.4])
const RIGHT_TAIL = makeTail([22.5, 13.5], [28.5, 25.4])
/** Trecho final da ponta direita que recebe a ponteira. */
const TIP_START = 0.5
/** Acima de 6 graus (faixas vermelhas) as listras não cabem: o texto informa. */
const MAX_TAPES = 6
/** Distância entre fitas, em fração da ponta; o grupo fica centralizado na ponteira. */
const TAPE_GAP = 0.075
const TIP_BLACK = '#0a0a0b'
const TIP_RED = '#dc2626'
const BAND_PATH = 'M1 7.5 Q20 11 39 7.5 V13 Q20 16.5 1 13 Z'

function makeTail(start: Point, end: Point): Tail {
  const length = Math.hypot(end[0] - start[0], end[1] - start[1])
  const half: Point = [(-(end[1] - start[1]) / length) * (TAIL_WIDTH / 2), ((end[0] - start[0]) / length) * (TAIL_WIDTH / 2)]
  const at = (t: number): [Point, Point] => {
    const center: Point = [start[0] + (end[0] - start[0]) * t, start[1] + (end[1] - start[1]) * t]
    return [[center[0] - half[0], center[1] - half[1]], [center[0] + half[0], center[1] + half[1]]]
  }
  const [a, b] = at(0)
  const [d, c] = at(1)
  return { corners: points(a, b, c, d), across: [a, b], at }
}

function points(...list: Point[]) {
  return list.map(([x, y]) => `${x.toFixed(2)},${y.toFixed(2)}`).join(' ')
}

function tapePositions(degrees: number): number[] {
  if (degrees < 1 || degrees > MAX_TAPES) return []
  const middle = (TIP_START + 1) / 2
  return Array.from({ length: degrees }, (_, i) => middle + (i - (degrees - 1) / 2) * TAPE_GAP)
}

/** Listra central das faixas de duas cores; a direção do gradiente segue a largura da peça. */
function Stripes({ id, main, stripe, from, to }: { id: string; main: string; stripe: string; from: Point; to: Point }) {
  return (
    <linearGradient id={id} gradientUnits="userSpaceOnUse" x1={from[0]} y1={from[1]} x2={to[0]} y2={to[1]}>
      <stop offset="0.33" stopColor={main} />
      <stop offset="0.33" stopColor={stripe} />
      <stop offset="0.67" stopColor={stripe} />
      <stop offset="0.67" stopColor={main} />
    </linearGradient>
  )
}

type Props = { belt: string | null; degrees?: number; size?: number }

/**
 * Faixa de jiu-jitsu amarrada, na cor da faixa (listra central nas de duas cores). A ponteira é
 * preta — vermelha na faixa preta — e leva uma listra branca por grau.
 */
export function BeltIcon({ belt, degrees = 0, size = 30 }: Props) {
  const id = `belt${useId().replace(/[^\w-]/g, '')}`
  const info = beltInfo(belt)
  if (!info) return null

  const [main, stripe = main] = info.colors
  const tip = info.slug === 'preta' ? TIP_RED : TIP_BLACK
  const [tipA, tipB] = RIGHT_TAIL.at(TIP_START)
  const [tipD, tipC] = RIGHT_TAIL.at(1)

  return (
    <svg className="belt-icon" width={size} height={size * 0.65} viewBox="0 0 40 26" aria-hidden focusable="false">
      <defs>
        <Stripes id={`${id}-band`} main={main} stripe={stripe} from={[0, 5.5]} to={[0, 15]} />
        <Stripes id={`${id}-left`} main={main} stripe={stripe} from={LEFT_TAIL.across[0]} to={LEFT_TAIL.across[1]} />
        <Stripes id={`${id}-right`} main={main} stripe={stripe} from={RIGHT_TAIL.across[0]} to={RIGHT_TAIL.across[1]} />
      </defs>
      <polygon className="belt-shape" fill={`url(#${id}-left)`} points={LEFT_TAIL.corners} />
      <polygon className="belt-shape" fill={`url(#${id}-right)`} points={RIGHT_TAIL.corners} />
      <polygon className="belt-shape" fill={tip} points={points(tipA, tipB, tipC, tipD)} />
      {tapePositions(degrees).map((t) => {
        const [[x1, y1], [x2, y2]] = RIGHT_TAIL.at(t)
        return <line key={t} className="belt-tape" x1={x1} y1={y1} x2={x2} y2={y2} />
      })}
      <path className="belt-shape" d={BAND_PATH} fill={`url(#${id}-band)`} />
      <rect className="belt-shape" x="14.5" y="5.5" width="11" height="10" rx="2.5" fill={`url(#${id}-band)`} />
      <line className="belt-crease" x1="14.5" y1="10.5" x2="25.5" y2="10.5" />
    </svg>
  )
}
