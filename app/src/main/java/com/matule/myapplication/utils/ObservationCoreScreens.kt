package com.matule.myapplication.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matule.myapplication.models.UserObservation
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationsScreenContent(
    onBackClick: () -> Unit,
    onObservationClick: (UserObservation) -> Unit,
    onAddObservationClick: () -> Unit,
    onEditObservationClick: (UserObservation) -> Unit
) {
    val repository = LocalObservationRepository.current
    var refreshKey by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf("all") }
    var pendingPhotoObservation by remember { mutableStateOf<UserObservation?>(null) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var photoDescription by remember { mutableStateOf("") }
    val observations = remember(refreshKey) { repository.getAllObservations() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            pendingPhotoObservation = null
            pendingPhotoUri = null
        } else {
            pendingPhotoUri = uri
            photoDescription = ""
        }
    }

    val filteredObservations = when (selectedFilter) {
        "planned" -> observations.filter { it.status == "planned" }
        "completed" -> observations.filter { it.status == "completed" }
        "cancelled" -> observations.filter { it.status == "cancelled" }
        else -> observations
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = tr("Мои наблюдения", "My observations"),
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = onAddObservationClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = tr("Добавить", "Add"),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        ScrollableTabRow(
            selectedTabIndex = when (selectedFilter) {
                "all" -> 0
                "planned" -> 1
                "completed" -> 2
                "cancelled" -> 3
                else -> 0
            }
        ) {
            listOf(
                tr("Все", "All"),
                tr("Запланировано", "Planned"),
                tr("Выполнено", "Completed"),
                tr("Отменено", "Cancelled")
            ).forEachIndexed { index, title ->
                Tab(
                    selected = when (selectedFilter) {
                        "all" -> index == 0
                        "planned" -> index == 1
                        "completed" -> index == 2
                        "cancelled" -> index == 3
                        else -> false
                    },
                    onClick = {
                        selectedFilter = when (index) {
                            1 -> "planned"
                            2 -> "completed"
                            3 -> "cancelled"
                            else -> "all"
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        if (filteredObservations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(tr("Пока нет наблюдений", "No observations yet"))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredObservations, key = { it.id }) { observation ->
                    ObservationCardContent(
                        observation = observation,
                        onClick = { onObservationClick(repository.getObservationById(observation.id) ?: observation) },
                        onEdit = { onEditObservationClick(observation) },
                        onAddPhoto = {
                            pendingPhotoObservation = observation
                            photoPicker.launch("image/*")
                        },
                        onDelete = {
                            repository.deleteObservation(observation.id)
                            refreshKey++
                        }
                    )
                }
            }
        }
    }

    if (pendingPhotoObservation != null && pendingPhotoUri != null) {
        AlertDialog(
            onDismissRequest = {
                pendingPhotoObservation = null
                pendingPhotoUri = null
            },
            title = { Text(tr("Описание фотографии", "Photo description")) },
            text = {
                OutlinedTextField(
                    value = photoDescription,
                    onValueChange = { photoDescription = it },
                    label = { Text(tr("Описание", "Description")) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val observation = pendingPhotoObservation
                        val photoUri = pendingPhotoUri
                        if (observation != null && photoUri != null) {
                            repository.addPhotoFromUri(
                                observationId = observation.id,
                                uri = photoUri,
                                description = photoDescription.takeIf { it.isNotBlank() }
                            )
                            refreshKey++
                        }
                        pendingPhotoObservation = null
                        pendingPhotoUri = null
                    }
                ) {
                    Text(tr("Сохранить", "Save"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingPhotoObservation = null
                        pendingPhotoUri = null
                    }
                ) {
                    Text(tr("Отмена", "Cancel"))
                }
            }
        )
    }
}

@Composable
private fun ObservationCardContent(
    observation: UserObservation,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onAddPhoto: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(observation.objectName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    observation.objectType?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = tr("Редактировать", "Edit"))
                    }
                    IconButton(onClick = onAddPhoto, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = tr("Добавить фото", "Add photo"))
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = tr("Удалить", "Delete"))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            DetailLine(tr("Дата", "Date"), formatDateTime(observation.observationDate, "dd.MM.yyyy"))
            observation.location?.let { DetailLine(tr("Место", "Location"), it) }
            observation.telescopeUsed?.let { DetailLine(tr("Телескоп", "Telescope"), it) }
            DetailLine(tr("Качество", "Quality"), "${observation.seeingRating}/5")

            if (observation.photos.isNotEmpty()) {
                Text(
                    tr("Фотографий", "Photos") + ": ${observation.photos.size}",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            observation.personalNotes?.takeIf { it.isNotBlank() }?.let {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    it,
                    fontStyle = FontStyle.Italic,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(tr("Удалить наблюдение", "Delete observation")) },
            text = { Text(tr("Вы уверены, что хотите удалить это наблюдение?", "Are you sure you want to delete this observation?")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(tr("Удалить", "Delete"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(tr("Отмена", "Cancel"))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationEditorScreenContent(
    initialObservation: UserObservation?,
    onBackClick: () -> Unit,
    onSaveClick: (UserObservation) -> Unit
) {
    var objectName by remember(initialObservation) { mutableStateOf(initialObservation?.objectName.orEmpty()) }
    var objectType by remember(initialObservation) { mutableStateOf(initialObservation?.objectType.orEmpty()) }
    var observationDate by remember(initialObservation) { mutableStateOf(initialObservation?.observationDate ?: Date()) }
    var location by remember(initialObservation) { mutableStateOf(initialObservation?.location.orEmpty()) }
    var telescopeUsed by remember(initialObservation) { mutableStateOf(initialObservation?.telescopeUsed.orEmpty()) }
    var weatherConditions by remember(initialObservation) { mutableStateOf(initialObservation?.weatherConditions.orEmpty()) }
    var seeingRating by remember(initialObservation) { mutableStateOf(initialObservation?.seeingRating ?: 3) }
    var personalNotes by remember(initialObservation) { mutableStateOf(initialObservation?.personalNotes.orEmpty()) }
    var status by remember(initialObservation) { mutableStateOf(initialObservation?.status ?: "planned") }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = if (initialObservation == null) tr("Новое наблюдение", "New observation")
            else tr("Редактирование наблюдения", "Edit observation"),
            onBackClick = onBackClick,
            actions = {
                TextButton(
                    onClick = {
                        if (objectName.isBlank()) return@TextButton
                        onSaveClick(
                            UserObservation(
                                id = initialObservation?.id ?: 0,
                                userName = initialObservation?.userName ?: "Астроном",
                                objectName = objectName,
                                objectType = objectType.takeIf { it.isNotBlank() },
                                observationDate = observationDate,
                                location = location.takeIf { it.isNotBlank() },
                                telescopeUsed = telescopeUsed.takeIf { it.isNotBlank() },
                                weatherConditions = weatherConditions.takeIf { it.isNotBlank() },
                                seeingRating = seeingRating.coerceIn(1, 5),
                                personalNotes = personalNotes.takeIf { it.isNotBlank() },
                                createdAt = initialObservation?.createdAt ?: Date(),
                                status = status,
                                actualObservationDate = initialObservation?.actualObservationDate,
                                actualObservationNotes = initialObservation?.actualObservationNotes,
                                photos = initialObservation?.photos ?: emptyList()
                            )
                        )
                    }
                ) {
                    Text(tr("Сохранить", "Save"), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = objectName,
                    onValueChange = { objectName = it },
                    label = { Text(tr("Название объекта", "Object name")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = objectType,
                    onValueChange = { objectType = it },
                    label = { Text(tr("Тип объекта", "Object type")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formatDateTime(observationDate, "dd.MM.yyyy"),
                        onValueChange = {},
                        enabled = false,
                        label = { Text(tr("Дата наблюдения", "Observation date")) },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = tr("Выбрать дату", "Pick date"))
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(tr("Место наблюдения", "Location")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = telescopeUsed,
                    onValueChange = { telescopeUsed = it },
                    label = { Text(tr("Телескоп или оборудование", "Telescope or equipment")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = weatherConditions,
                    onValueChange = { weatherConditions = it },
                    label = { Text(tr("Погодные условия", "Weather conditions")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(tr("Качество наблюдения", "Observation quality") + ": $seeingRating/5")
                Slider(
                    value = seeingRating.toFloat(),
                    onValueChange = { seeingRating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
            item {
                OutlinedTextField(
                    value = personalNotes,
                    onValueChange = { personalNotes = it },
                    label = { Text(tr("Заметки", "Notes")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == "planned",
                        onClick = { status = "planned" },
                        label = { Text(tr("Запланировано", "Planned")) }
                    )
                    FilterChip(
                        selected = status == "completed",
                        onClick = { status = "completed" },
                        label = { Text(tr("Выполнено", "Completed")) }
                    )
                    FilterChip(
                        selected = status == "cancelled",
                        onClick = { status = "cancelled" },
                        label = { Text(tr("Отменено", "Cancelled")) }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { millis ->
                millis?.let { observationDate = Date(it) }
                showDatePicker = false
            }
        )
    }
}

fun getStatusTextLocalized(status: String): String {
    return when (status) {
        "planned" -> "Запланировано"
        "completed" -> "Выполнено"
        "cancelled" -> "Отменено"
        else -> status
    }
}
