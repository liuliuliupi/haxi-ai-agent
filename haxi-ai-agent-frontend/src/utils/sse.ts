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
          // 处理剩余的 buffer
          if (buffer) {
            const lines = buffer.split(/\r?\n/)
            for (let i = 0; i < lines.length; i++) {
              const line = lines[i]
              if (line.startsWith('data:')) {
                const data = line.slice(5)
                if (data !== '[DONE]') {
                  // 检查下一行是否也是 data: 开头，如果是则添加换行符
                  const nextLine = lines[i + 1]
                  const hasMoreData = nextLine && nextLine.startsWith('data:')
                  this.onMessage(data + (hasMoreData ? '\n' : ''))
                }
              }
            }
          }
          break
        }

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split(/\r?\n/)
        // 保留最后一行（可能不完整）
        buffer = lines.pop() || ''

        for (let i = 0; i < lines.length; i++) {
          const line = lines[i]
          if (line.startsWith('data:')) {
            const data = line.slice(5)
            if (data !== '[DONE]') {
              // 检查下一行是否也是 data: 开头，如果是则添加换行符
              const nextLine = lines[i + 1]
              const hasMoreData = nextLine && nextLine.startsWith('data:')
              this.onMessage(data + (hasMoreData ? '\n' : ''))
            }
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
