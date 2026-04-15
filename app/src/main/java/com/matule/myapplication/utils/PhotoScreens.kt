package com.matule.myapplication.utils

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matule.myapplication.data.PlanetDatabase
import com.matule.myapplication.data.ObservationRepository
import com.matule.myapplication.models.ObservationPhoto
import com.matule.myapplication.models.UserObservation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ObservationDetailScreenContent(
    observation: UserObservation,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val repository = LocalObservationRepository.current
    val calculator = LocalAstroCalculator.current
    var photos by remember(observation.id) { mutableStateOf(repository.getPhotosForObservation(observation.id)) }
    var previewPhoto by remember { mutableStateOf<ObservationPhoto?>(null) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var photoDescription by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val matchedPlanet = remember(observation.objectName) {
        PlanetDatabase.getPlanetByName(observation.objectName)
    }
    val astronomySnapshot = remember(
        observation.id,
        observation.observationDate,
        matchedPlanet?.latinName
    ) {
        matchedPlanet?.let { calculator.calculatePlanetPosition(it.latinName, observation.observationDate) }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pendingPhotoUri = uri
        photoDescription = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(
            title = observation.objectName,
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { photoPicker.launch("image/*") }) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = tr("Добавить фотографию", "Add photo"),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = tr("Редактировать", "Edit"),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(tr("Основная информация", "Main information"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailLine(tr("Тип объекта", "Object type"), observation.objectType ?: tr("Не указан", "Not specified"))
                        DetailLine(tr("Дата", "Date"), formatDateTime(observation.observationDate, "dd.MM.yyyy"))
                        DetailLine(tr("Место", "Location"), observation.location ?: tr("Не указано", "Not specified"))
                        DetailLine(tr("Телескоп", "Telescope"), observation.telescopeUsed ?: tr("Не указан", "Not specified"))
                        DetailLine(tr("Погода", "Weather"), observation.weatherConditions ?: tr("Не указана", "Not specified"))
                        DetailLine(tr("Качество", "Quality"), "${observation.seeingRating}/5")
                        DetailLine(tr("Статус", "Status"), getStatusTextLocalized(observation.status))
                    }
                }
            }
            observation.personalNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(tr("Заметки", "Notes"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(notes, modifier = Modifier.padding(top = 8.dp), fontStyle = FontStyle.Italic)
                        }
                    }
                }
            }
            astronomySnapshot?.let { position ->
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                tr("Астрономические данные", "Astronomy snapshot"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                tr("Параметры на дату наблюдения", "Values for the observation date"),
                                modifier = Modifier.padding(top = 6.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            DetailLine("X", formatOptionalDouble(position.heliocentricXAu, 7))
                            DetailLine("Y", formatOptionalDouble(position.heliocentricYAu, 7))
                            DetailLine("Z", formatOptionalDouble(position.heliocentricZAu, 7))
                            if (position.hasLocalSkyPosition) {
                                DetailLine(tr("Высота", "Altitude"), formatAngleValue(position.altitude))
                                DetailLine(tr("Азимут", "Azimuth"), formatAngleValue(position.azimuth))
                                DetailLine(
                                    tr("Расстояние от Земли", "Distance from Earth"),
                                    formatAuValue(position.distanceFromEarthAu)
                                )
                                position.constellationCode?.let { code ->
                                    DetailLine(
                                        tr("Созвездие", "Constellation"),
                                        buildString {
                                            append(code)
                                            position.constellationName?.let { name ->
                                                append(" (")
                                                append(name)
                                                append(")")
                                            }
                                        }
                                    )
                                }
                                position.magnitude?.let {
                                    DetailLine(tr("Блеск", "Magnitude"), formatOptionalDouble(it))
                                }
                            } else {
                                Text(
                                    tr(
                                        "Для Земли локальные небесные координаты не рассчитываются.",
                                        "Local sky coordinates are not calculated for Earth."
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("Фотографии", "Photos"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            TextButton(onClick = { photoPicker.launch("image/*") }) {
                                Text(tr("Добавить", "Add"))
                            }
                        }
                        if (photos.isEmpty()) {
                            Text(tr("Фотографии пока не добавлены.", "No photos added yet."))
                        } else {
                            photos.forEach { photo ->
                                PhotoCardContent(
                                    photo = photo,
                                    repository = repository,
                                    onPreview = { previewPhoto = photo },
                                    onDelete = {
                                        repository.deletePhoto(photo.id)
                                        photos = repository.getPhotosForObservation(observation.id)
                                        if (previewPhoto?.id == photo.id) previewPhoto = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(tr("Удалить наблюдение", "Delete observation"))
                }
            }
        }
    }

    if (pendingPhotoUri != null) {
        AlertDialog(
            onDismissRequest = { pendingPhotoUri = null },
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
                        pendingPhotoUri?.let { uri ->
                            repository.addPhotoFromUri(observation.id, uri, photoDescription.takeIf { it.isNotBlank() })
                            photos = repository.getPhotosForObservation(observation.id)
                        }
                        pendingPhotoUri = null
                    }
                ) {
                    Text(tr("Сохранить", "Save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPhotoUri = null }) {
                    Text(tr("Отмена", "Cancel"))
                }
            }
        )
    }

    previewPhoto?.let {
        PhotoPreviewDialog(photo = it, repository = repository, onDismiss = { previewPhoto = null })
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(tr("Удалить наблюдение", "Delete observation")) },
            text = { Text(tr("Вы уверены, что хотите удалить это наблюдение?", "Are you sure you want to delete this observation?")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
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

@Composable
fun PhotosScreenContent(onBackClick: () -> Unit) {
    val repository = LocalObservationRepository.current
    var photos by remember { mutableStateOf(repository.getAllPhotos()) }
    var previewPhoto by remember { mutableStateOf<ObservationPhoto?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenTopBar(title = tr("Мои снимки", "My photos"), onBackClick = onBackClick)

        if (photos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(tr("У вас пока нет загруженных фотографий.", "You have no uploaded photos yet."))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    PhotoCardContent(
                        photo = photo,
                        repository = repository,
                        onPreview = { previewPhoto = photo },
                        onDelete = {
                            repository.deletePhoto(photo.id)
                            photos = repository.getAllPhotos()
                            if (previewPhoto?.id == photo.id) previewPhoto = null
                        }
                    )
                }
            }
        }
    }

    previewPhoto?.let {
        PhotoPreviewDialog(photo = it, repository = repository, onDismiss = { previewPhoto = null })
    }
}

@Composable
private fun PhotoCardContent(
    photo: ObservationPhoto,
    repository: ObservationRepository,
    onPreview: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val bitmap = rememberPhotoBitmap(
        repository = repository,
        filePath = photo.filePath,
        loadThumbnail = true
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(photo.fileName, fontWeight = FontWeight.Medium)
            Text(formatDateTime(photo.uploadDate, "dd.MM.yyyy HH:mm"), modifier = Modifier.padding(top = 4.dp))
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = photo.fileName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 10.dp)
                        .clickable(onClick = onPreview)
                )
            }
            photo.description?.let {
                Text(it, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 10.dp))
            }
            Text(tr("Размер", "Size") + ": ${repository.formatFileSize(photo.fileSize)}", modifier = Modifier.padding(top = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onPreview) { Text(tr("Открыть", "Open")) }
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text(tr("Удалить", "Delete"), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(tr("Удалить фотографию", "Delete photo")) },
            text = { Text(tr("Фотография будет удалена без возможности восстановления.", "This photo will be deleted permanently.")) },
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

@Composable
private fun PhotoPreviewDialog(
    photo: ObservationPhoto,
    repository: ObservationRepository,
    onDismiss: () -> Unit
) {
    val bitmap = rememberPhotoBitmap(
        repository = repository,
        filePath = photo.filePath,
        loadThumbnail = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(photo.fileName) },
        text = {
            Column {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = photo.fileName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                photo.description?.let { Text(it, modifier = Modifier.padding(top = 12.dp)) }
                Text(tr("Размер", "Size") + ": ${repository.formatFileSize(photo.fileSize)}", modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Закрыть", "Close"))
            }
        }
    )
}

@Composable
private fun rememberPhotoBitmap(
    repository: ObservationRepository,
    filePath: String,
    loadThumbnail: Boolean
): Bitmap? {
    val bitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = repository,
        key2 = filePath,
        key3 = loadThumbnail
    ) {
        value = withContext(Dispatchers.IO) {
            if (loadThumbnail) {
                repository.loadPhotoThumbnail(filePath)
            } else {
                repository.loadPhoto(filePath)
            }
        }
    }

    return bitmap
}
