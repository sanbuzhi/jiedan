import request from '@/utils/request'

/**
 * 通用上传文件接口
 */
export const uploadFile = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string>('/common/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}