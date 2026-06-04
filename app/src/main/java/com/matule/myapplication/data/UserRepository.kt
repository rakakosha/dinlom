package com.matule.myapplication.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.matule.myapplication.models.AppUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Date
import java.util.UUID

class UserRepository private constructor(
    private val userDao: UserDao
) {
    suspend fun registerUser(fullName: String, email: String, password: String): RegistrationResult {
        return withContext(Dispatchers.IO) {
            val normalizedName = fullName.trim()
            val normalizedEmail = email.trim().lowercase()

            if (normalizedName.length < MIN_NAME_LENGTH) {
                return@withContext RegistrationResult.Error("Введите имя не короче $MIN_NAME_LENGTH символов")
            }
            if (!EMAIL_REGEX.matches(normalizedEmail)) {
                return@withContext RegistrationResult.Error("Введите корректный email")
            }
            if (password.length < MIN_PASSWORD_LENGTH) {
                return@withContext RegistrationResult.Error("Пароль должен быть не короче $MIN_PASSWORD_LENGTH символов")
            }
            if (userDao.isEmailTaken(normalizedEmail)) {
                return@withContext RegistrationResult.Error("Пользователь с таким email уже зарегистрирован")
            }

            val salt = UUID.randomUUID().toString()
            val now = Date()
            val user = AppUser(
                fullName = normalizedName,
                email = normalizedEmail,
                passwordHash = hashPassword(password, salt),
                passwordSalt = salt,
                createdAt = now
            )
            try {
                val id = userDao.insertUser(user).toInt()
                RegistrationResult.Success(user.copy(id = id))
            } catch (_: SQLiteConstraintException) {
                RegistrationResult.Error("Пользователь с таким email уже зарегистрирован")
            }
        }
    }

    suspend fun getAllUsers(): List<AppUser> {
        return withContext(Dispatchers.IO) {
            userDao.getAllUsers()
        }
    }

    private fun hashPassword(password: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    sealed class RegistrationResult {
        data class Success(val user: AppUser) : RegistrationResult()
        data class Error(val message: String) : RegistrationResult()
    }

    companion object {
        private const val MIN_NAME_LENGTH = 2
        private const val MIN_PASSWORD_LENGTH = 6
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(context: Context): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository(
                    AstronomyGuideDatabase.getInstance(context).userDao()
                ).also { instance = it }
            }
        }
    }
}
