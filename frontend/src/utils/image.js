import { tGlobal } from '@/i18n/index.js'

const MAX_ICON_BYTES = 100 * 1024

function blobToImage(blob) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(blob)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve(img)
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error(tGlobal('iconUpload.readFailed')))
    }
    img.src = url
  })
}

function canvasToBlob(canvas, mimeType, quality) {
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error(tGlobal('iconUpload.compressFailed')))
        return
      }
      resolve(blob)
    }, mimeType, quality)
  })
}

function drawImage(img, width, height) {
  const canvas = document.createElement('canvas')
  canvas.width = Math.max(1, width)
  canvas.height = Math.max(1, height)
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
  return canvas
}

export async function compressImageToMaxSize(blob, maxBytes = MAX_ICON_BYTES, options = {}) {
  const { maxWidth = 512, mimeType = 'image/jpeg' } = options
  const img = await blobToImage(blob)
  let width = img.naturalWidth
  let height = img.naturalHeight
  if (width > maxWidth || height > maxWidth) {
    const scale = maxWidth / Math.max(width, height)
    width = Math.round(width * scale)
    height = Math.round(height * scale)
  }

  let quality = 0.92
  let result = await canvasToBlob(drawImage(img, width, height), mimeType, quality)
  while (result.size > maxBytes && (quality > 0.4 || Math.max(width, height) > 96)) {
    if (quality > 0.4) {
      quality = Math.max(0.4, +(quality - 0.08).toFixed(2))
    } else {
      width = Math.max(96, Math.round(width * 0.85))
      height = Math.max(96, Math.round(height * 0.85))
    }
    result = await canvasToBlob(drawImage(img, width, height), mimeType, quality)
  }
  if (result.size > maxBytes) {
    throw new Error(tGlobal('iconUpload.cannotCompress'))
  }
  return result
}

export async function urlToObjectUrl(url) {
  const response = await fetch(url, { mode: 'cors' })
  if (!response.ok) {
    throw new Error(tGlobal('iconUpload.fetchFailed'))
  }
  const blob = await response.blob()
  return URL.createObjectURL(blob)
}

export { MAX_ICON_BYTES }
