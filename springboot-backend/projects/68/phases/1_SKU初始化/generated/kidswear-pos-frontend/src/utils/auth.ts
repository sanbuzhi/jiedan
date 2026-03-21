const TOKEN_KEY = 'kidswear_pos_token'

export const getToken = () => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token: string) => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = () => {
  localStorage.removeItem(TOKEN_KEY)
}