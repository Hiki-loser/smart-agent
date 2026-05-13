import { v4 as uuidv4 } from 'uuid'
import { DEVICE_ID_STORAGE_KEY } from '@/config/app.config'

export function useDeviceId(): string {
  let id = localStorage.getItem(DEVICE_ID_STORAGE_KEY)
  if (!id) {
    id = uuidv4()
    localStorage.setItem(DEVICE_ID_STORAGE_KEY, id)
  }
  return id
}
