const Code = {
  SUCCESS: 20000,
  INSERT_OK: 20011,
  DELETE_OK: 20021,
  UPDATE_OK: 20031,
  SELECT_OK: 20041,

  INSERT_ERR: 20010,
  DELETE_ERR: 20020,
  UPDATE_ERR: 20030,
  SELECT_ERR: 20040,

  REGISTER_ERR: 30010,
  LOGIN_TOKEN_ERR: 30020,
  LOGIN_TOKEN_MISSING: 30030,

  SYSTEM_ERR: 50001,
  BUSINESS_ERR: 60001
}

const CodeUtil = {
  isSuccess(code: number) {
    return [
      Code.SUCCESS,
      Code.INSERT_OK,
      Code.DELETE_OK,
      Code.UPDATE_OK,
      Code.SELECT_OK
    ].includes(code)
  },

  isError(code: number) {
    return [
      Code.INSERT_ERR,
      Code.DELETE_ERR,
      Code.UPDATE_ERR,
      Code.SELECT_ERR
    ].includes(code)
  },

  isSystemError(code: number) {
    return code === Code.SYSTEM_ERR
  },

  isBusinessError(code: number) {
    return code === Code.BUSINESS_ERR
  },

  isTokenError(code: number) {
    return [Code.LOGIN_TOKEN_ERR, Code.LOGIN_TOKEN_MISSING].includes(code)
  },

  getMessage(code: number) {
    if (this.isSuccess(code)) return 'Success'
    if (this.isError(code)) return 'CRUD error'
    if (this.isSystemError(code)) return 'System error'
    if (this.isBusinessError(code)) return 'Business error'
    if (this.isTokenError(code)) return 'auth error'
    return 'Unknow error'
  }
}

export default CodeUtil
