package com.matule.myapplication.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matule.myapplication.data.UserRepository
import com.matule.myapplication.models.AppUser
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RegistrationScreenContent(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<AppUser>>(emptyList()) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(repository) {
        users = repository.getAllUsers()
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = tr("Регистрация", "Registration"),
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = tr("Создание пользователя", "Create user"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(
                                "Сейчас это локальная Room-демонстрация. Для готовой PostgreSQL-базы подключите backend API по инструкции в docs/postgresql-integration.md.",
                                "This is a local Room demo. For an existing PostgreSQL database, connect a backend API as described in docs/postgresql-integration.md."
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(tr("ФИО", "Full name")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(tr("Пароль", "Password")) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = passwordRepeat,
                            onValueChange = { passwordRepeat = it },
                            label = { Text(tr("Повтор пароля", "Repeat password")) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        message?.let { text ->
                            Text(
                                text = text,
                                color = if (isSuccessMessage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                enabled = !isLoading,
                                onClick = {
                                    if (password != passwordRepeat) {
                                        message = "Пароли не совпадают"
                                        isSuccessMessage = false
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        isLoading = true
                                        when (val result = repository.registerUser(fullName, email, password)) {
                                            is UserRepository.RegistrationResult.Success -> {
                                                users = repository.getAllUsers()
                                                fullName = ""
                                                email = ""
                                                password = ""
                                                passwordRepeat = ""
                                                message = "Пользователь ${result.user.fullName} зарегистрирован"
                                                isSuccessMessage = true
                                            }

                                            is UserRepository.RegistrationResult.Error -> {
                                                message = result.message
                                                isSuccessMessage = false
                                            }
                                        }
                                        isLoading = false
                                    }
                                }
                            ) {
                                Text(if (isLoading) tr("Подождите...", "Please wait...") else tr("Зарегистрировать", "Register"))
                            }
                            TextButton(
                                onClick = {
                                    fullName = ""
                                    email = ""
                                    password = ""
                                    passwordRepeat = ""
                                    message = null
                                }
                            ) {
                                Text(tr("Очистить", "Clear"))
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = tr("Пользователи в базе: ${users.size}", "Users in database: ${users.size}"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoading) {
                item {
                    Text(
                        text = tr("Загрузка пользователей...", "Loading users..."),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else if (users.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = tr(
                                "Пока пользователей нет. Заполните форму выше, чтобы добавить первую запись в базу.",
                                "There are no users yet. Fill in the form above to add the first database record."
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(users, key = { it.id }) { user ->
                    UserCard(user = user)
                }
            }
        }
    }
}

@Composable
private fun UserCard(user: AppUser) {
    val formatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = user.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.email,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tr(
                    "Дата регистрации: ${formatter.format(user.createdAt)}",
                    "Registered: ${formatter.format(user.createdAt)}"
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontSize = 13.sp
            )
        }
    }
}
