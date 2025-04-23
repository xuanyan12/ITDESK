// 服务器开发地址
const HTTP_BASE_URL = "http://10.161.68.46:9999"
// 服务器生产地址
const HTTP_BASE_URL_DEV = "http://localhost:9999"

// 服务器地址
export const BASE_URL = process.env.NODE_ENV === 'production' ? HTTP_BASE_URL : HTTP_BASE_URL_DEV

// Http 接口状态
export const RES_STATUS_CODE = {
    SUCCESS: 200,
    NOT_FOUND: 404,
    ERROR: 300,
    UNAUTHORIZED: 301,
    FORBIDDEN: 302
}