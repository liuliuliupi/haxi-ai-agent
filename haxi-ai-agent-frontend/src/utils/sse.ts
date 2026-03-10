export class SSEManager {
  private controller: AbortController | null = null
  private onMessage: (data: string) => void
  private onError: (error: Error) => void
  private onOpen: () => void
  private onComplete: () => void

  constructor(
    onMessage: (data: string) => void,
    onError: (error: Error) => void = () => {},
    onOpen: () => void = () => {},
    onComplete: () => void = () => {}
  ) {
    this.onMessage = onMessage
    this.onError = onError
    this.onOpen = onOpen
    this.onComplete = onComplete
  }

  async connect(url: string, params: Record<string, string> = {}) {
    const queryString = new URLSearchParams(params).toString()
    const fullUrl = queryString ? `${url}?${queryString}` : url

    this.controller = new AbortController()

    try {
      const response = await fetch(fullUrl, {
        method: 'GET',
        headers: {
          'Accept': 'text/event-stream',
          'Cache-Control': 'no-cache'
        },
        signal: this.controller.signal
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      if (!response.body) {
        throw new Error('Response body is null')
      }

      this.onOpen()

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()

        if (done) {
          if (buffer.trim()) {
            const parsed = this.parseSSEData(buffer)
            if (parsed) {
              this.onMessage(parsed)
            }
          }
          break
        }

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.trim() === '' || line.startsWith(':')) continue
          
          const parsed = this.parseSSEData(line)
          if (parsed) {
            this.onMessage(parsed)
          }
        }
      }

      this.onComplete()
    } catch (error) {
      if ((error as Error).name !== 'AbortError') {
        this.onError(error as Error)
      }
    }

    return this
  }

  private parseSSEData(line: string): string | null {
    if (line.startsWith('data:')) {
      const data = line.slice(5).trim()
      if (data === '[DONE]') {
        return null
      }
      return data
    }
    return null
  }

  close() {
    if (this.controller) {
      this.controller.abort()
      this.controller = null
    }
  }
}

export function createSSEConnection(
  url: string,
  params: Record<string, string>,
  onMessage: (data: string) => void,
  onError?: (error: Error) => void,
  onComplete?: () => void
): SSEManager {
  const manager = new SSEManager(onMessage, onError, () => {}, onComplete || (() => {}))
  manager.connect(url, params)
  return manager
}
