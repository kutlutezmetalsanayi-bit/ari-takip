package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.util.ImageUtils
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer
import kotlinx.coroutines.launch
import java.io.File

/**
 * Creates a temporary file uri for camera capture using FileProvider
 */
fun createTempCameraUri(context: Context): Uri {
  val cacheDir = context.cacheDir
  val tempFile = File.createTempFile("ari_kamera_", ".jpg", cacheDir).apply {
    createNewFile()
    deleteOnExit()
  }
  val authority = "${context.packageName}.fileprovider"
  return FileProvider.getUriForFile(context, authority, tempFile)
}

/**
 * Bottom sheet to choose between Camera and Gallery
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceBottomSheet(
  onDismiss: () -> Unit,
  onCameraSelected: () -> Unit,
  onGallerySelected: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .testTag("photo_source_bottom_sheet")
    ) {
      Text(
        text = "📷 Fotoğraf Kaynağı Seçin",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(16.dp))

      // 1. Camera Option
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HoneyAmberContainer),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onDismiss()
            onCameraSelected()
          }
          .testTag("select_camera_btn")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(HoneyGoldDark),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White)
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = "📷 Kamera ile Çek",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = HoneyOnAmberContainer
            )
            Text(
              text = "Anında kovan, kontrol veya besleme fotoğrafı çekin",
              fontSize = 12.sp,
              color = HoneyOnAmberContainer.copy(alpha = 0.8f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 2. Gallery Option
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onDismiss()
            onGallerySelected()
          }
          .testTag("select_gallery_btn")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(HoneyGoldPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White)
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = "🖼️ Galeriden Seç",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Cihazınızdaki mevcut fotoğraflardan ekleyin",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

/**
 * Camera Photo Preview Dialog (V1.3.2 Section 2 & 7)
 * Allows the beekeeper to review the captured photo before saving:
 * - ✓ Kullan (Use)
 * - ↻ Tekrar Çek (Retake)
 * - 🗑️ Vazgeç (Discard)
 */
@Composable
fun CapturedPhotoPreviewDialog(
  photoUri: Uri,
  onUsePhoto: () -> Unit,
  onRetakePhoto: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(8.dp),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(20.dp))
        .testTag("photo_preview_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "📷 Fotoğraf Önizleme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Kapat")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image Preview
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black)
        ) {
          AsyncImage(
            model = photoUri,
            contentDescription = "Çekilen Fotoğraf Önizleme",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions: Kullan, Tekrar Çek, Vazgeç
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Discard / Vazgeç
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("preview_discard_btn")
          ) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Vazgeç", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Retake / Tekrar Çek
          OutlinedButton(
            onClick = onRetakePhoto,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1.1f)
              .height(48.dp)
              .testTag("preview_retake_btn")
          ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Tekrar Çek", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Use / Kullan
          Button(
            onClick = onUsePhoto,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldDark),
            modifier = Modifier
              .weight(1.1f)
              .height(48.dp)
              .testTag("preview_use_btn")
          ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Kullan", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
          }
        }
      }
    }
  }
}

/**
 * Reusable Photo Capture & Gallery management widget with full permission & compression integration
 */
@Composable
fun PhotoCaptureSection(
  title: String = "📷 Fotoğraflar",
  photoPaths: List<String>,
  onPhotosUpdated: (List<String>) -> Unit,
  allowMultiple: Boolean = true,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var showSourceSheet by remember { mutableStateOf(false) }
  var isCompressing by remember { mutableStateOf(false) }
  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
  var showPreviewDialog by remember { mutableStateOf(false) }
  var showPermissionDeniedDialog by remember { mutableStateOf(false) }
  var showNoCameraHardwareDialog by remember { mutableStateOf(false) }

  // Multiple Gallery Launcher
  val galleryMultipleLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      coroutineScope.launch {
        isCompressing = true
        val newPaths = uris.mapNotNull { ImageUtils.compressAndSaveImage(context, it) }
        val updated = if (allowMultiple) (photoPaths + newPaths).distinct() else newPaths.take(1)
        onPhotosUpdated(updated)
        isCompressing = false
      }
    }
  }

  // Single Gallery Launcher
  val gallerySingleLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      coroutineScope.launch {
        isCompressing = true
        val savedPath = ImageUtils.compressAndSaveImage(context, uri)
        if (savedPath != null) {
          val updated = if (allowMultiple) (photoPaths + savedPath).distinct() else listOf(savedPath)
          onPhotosUpdated(updated)
        }
        isCompressing = false
      }
    }
  }

  // Camera TakePicture Launcher
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
  ) { success: Boolean ->
    if (success && pendingCameraUri != null) {
      // Show Preview dialog first
      showPreviewDialog = true
    }
  }

  // Start Camera Action helper
  fun startCameraCapture() {
    val pm = context.packageManager
    val hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) ||
      pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)

    if (!hasCamera) {
      showNoCameraHardwareDialog = true
      return
    }

    try {
      val uri = createTempCameraUri(context)
      pendingCameraUri = uri
      takePictureLauncher.launch(uri)
    } catch (e: Exception) {
      e.printStackTrace()
      // Fallback safely to gallery without crashing
      if (allowMultiple) {
        galleryMultipleLauncher.launch("image/*")
      } else {
        gallerySingleLauncher.launch("image/*")
      }
    }
  }

  // Camera Permission Launcher
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      startCameraCapture()
    } else {
      showPermissionDeniedDialog = true
    }
  }

  fun launchCameraFlow() {
    val hasCameraPerm = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    if (hasCameraPerm) {
      startCameraCapture()
    } else {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  fun launchGalleryFlow() {
    if (allowMultiple) {
      galleryMultipleLauncher.launch("image/*")
    } else {
      gallerySingleLauncher.launch("image/*")
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      OutlinedButton(
        onClick = { showSourceSheet = true },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("add_photo_btn")
      ) {
        Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (photoPaths.isEmpty()) "Fotoğraf Ekle" else "Fotoğraf Çek/Seç", fontSize = 12.sp)
      }
    }

    if (isCompressing) {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Fotoğraf optimize ediliyor (1200px, EXIF düzeltme)...",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (photoPaths.isNotEmpty()) {
      Spacer(modifier = Modifier.height(10.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        itemsIndexed(photoPaths) { index, path ->
          Box(
            modifier = Modifier
              .size(100.dp)
              .clip(RoundedCornerShape(12.dp))
          ) {
            AsyncImage(
              model = path,
              contentDescription = "Fotoğraf $index",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

            // Delete button
            IconButton(
              onClick = {
                val updated = photoPaths.toMutableList().also { it.removeAt(index) }
                onPhotosUpdated(updated)
              },
              modifier = Modifier
                .size(26.dp)
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
            ) {
              Icon(Icons.Filled.Close, contentDescription = "Kaldır", tint = Color.White, modifier = Modifier.size(14.dp))
            }
          }
        }
      }
    }
  }

  // 1. Source Bottom Sheet
  if (showSourceSheet) {
    PhotoSourceBottomSheet(
      onDismiss = { showSourceSheet = false },
      onCameraSelected = { launchCameraFlow() },
      onGallerySelected = { launchGalleryFlow() }
    )
  }

  // 2. Camera Preview Dialog (Use / Retake / Discard)
  if (showPreviewDialog && pendingCameraUri != null) {
    CapturedPhotoPreviewDialog(
      photoUri = pendingCameraUri!!,
      onUsePhoto = {
        showPreviewDialog = false
        val uri = pendingCameraUri
        if (uri != null) {
          coroutineScope.launch {
            isCompressing = true
            val savedPath = ImageUtils.compressAndSaveImage(context, uri)
            if (savedPath != null) {
              val updated = if (allowMultiple) (photoPaths + savedPath).distinct() else listOf(savedPath)
              onPhotosUpdated(updated)
            }
            isCompressing = false
            pendingCameraUri = null
          }
        }
      },
      onRetakePhoto = {
        showPreviewDialog = false
        startCameraCapture()
      },
      onDismiss = {
        showPreviewDialog = false
        pendingCameraUri = null
      }
    )
  }

  // 3. Permission Denied Dialog
  if (showPermissionDeniedDialog) {
    AlertDialog(
      onDismissRequest = { showPermissionDeniedDialog = false },
      title = { Text("📷 Kamera İzni Gerekli", fontWeight = FontWeight.Bold) },
      text = {
        Text("Kamera izni verilmedi. Fotoğraf eklemek için kamera iznini etkinleştirebilir veya galeriden fotoğraf seçebilirsiniz.")
      },
      confirmButton = {
        Button(
          onClick = {
            showPermissionDeniedDialog = false
            launchGalleryFlow()
          },
          colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldDark)
        ) {
          Text("Galeriden Seç")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPermissionDeniedDialog = false }) {
          Text("Vazgeç")
        }
      }
    )
  }

  // 4. No Camera Hardware Dialog
  if (showNoCameraHardwareDialog) {
    AlertDialog(
      onDismissRequest = { showNoCameraHardwareDialog = false },
      title = { Text("📷 Kamera Bulunamadı", fontWeight = FontWeight.Bold) },
      text = {
        Text("Bu cihazda kamera kullanılamıyor. Fotoğraflarınızı galeriden seçerek ekleyebilirsiniz.")
      },
      confirmButton = {
        Button(
          onClick = {
            showNoCameraHardwareDialog = false
            launchGalleryFlow()
          },
          colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldDark)
        ) {
          Text("Galeriden Seç")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNoCameraHardwareDialog = false }) {
          Text("Kapat")
        }
      }
    )
  }
}
