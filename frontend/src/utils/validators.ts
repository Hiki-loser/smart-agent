export const rules = {
  username: [
    { required: true, message: 'auth.usernameRequired', trigger: 'blur' },
    { min: 4, max: 64, message: 'auth.usernameLength', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'auth.passwordRequired', trigger: 'blur' },
    { min: 6, max: 20, message: 'auth.passwordLength', trigger: 'blur' },
  ],
  nickname: [
    { max: 50, message: 'validation.nicknameLength', trigger: 'blur' },
  ],
  confirmPassword: (passwordRef: () => string | undefined) => [
    {
      required: true,
      message: 'auth.passwordRequired',
      trigger: 'blur',
    },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value !== passwordRef()) {
          callback(new Error('auth.passwordMismatch'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}
