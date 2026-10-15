import { useSyncExternalStore } from 'react'

/** Evento só do Chromium, ainda fora do lib.dom: guarda o pedido nativo de instalação para usar no clique. */
type InstallPromptEvent = Event & { prompt: () => Promise<void> }

/**
 * prompt: o navegador instala com um toque (Chrome, Edge, Samsung Internet).
 * ios / android: sem pedido nativo (Safari, navegador de outro app, antes do Chrome liberar); o botão
 * mostra o passo a passo pelo menu do navegador.
 * hidden: já instalado, aberto como app ou computador sem instalação.
 */
export type InstallMode = 'prompt' | 'ios' | 'android' | 'hidden'
export type GuidePlatform = Extract<InstallMode, 'ios' | 'android'>

const STANDALONE = '(display-mode: standalone)'

let deferred: InstallPromptEvent | null = null
let installed = false
const listeners = new Set<() => void>()

function notify() {
  for (const listener of listeners) listener()
}

/** Registrar antes do primeiro render: o Chrome pode disparar o evento antes de qualquer tela montar. */
export function listenForInstallPrompt() {
  window.addEventListener('beforeinstallprompt', (event) => {
    event.preventDefault()
    deferred = event as InstallPromptEvent
    notify()
  })
  window.addEventListener('appinstalled', () => {
    deferred = null
    installed = true
    notify()
  })
}

function isStandalone() {
  const iosStandalone = (navigator as Navigator & { standalone?: boolean }).standalone === true
  return iosStandalone || window.matchMedia(STANDALONE).matches
}

/** O iPadOS se apresenta como Mac; a tela de toque o diferencia. */
function isIos() {
  const ua = navigator.userAgent
  return /iPhone|iPad|iPod/.test(ua) || (/Macintosh/.test(ua) && navigator.maxTouchPoints > 1)
}

function currentMode(): InstallMode {
  if (installed || isStandalone()) return 'hidden'
  if (deferred) return 'prompt'
  if (isIos()) return 'ios'
  if (/Android/i.test(navigator.userAgent)) return 'android'
  return 'hidden'
}

function subscribe(onChange: () => void) {
  listeners.add(onChange)
  return () => {
    listeners.delete(onChange)
  }
}

export function useInstallMode() {
  return useSyncExternalStore(subscribe, currentMode, (): InstallMode => 'hidden')
}

/** O evento só pode abrir o pedido uma vez; se a pessoa recusar, o Chrome dispara outro mais tarde. */
export async function promptInstall() {
  const event = deferred
  if (!event) return
  deferred = null
  notify()
  await event.prompt()
}
