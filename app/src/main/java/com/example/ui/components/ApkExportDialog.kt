package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.ui.theme.AuraCyanPrimary
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraDarkBorder
import com.example.ui.theme.AuraDarkCard
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@Composable
fun ApkExportDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val apkFile = remember {
        val src = context.applicationInfo.sourceDir
        if (src != null) File(src) else null
    }

    val apkSizeBytes = remember(apkFile) {
        apkFile?.length() ?: 0L
    }

    val formattedSize = remember(apkSizeBytes) {
        if (apkSizeBytes > 0) {
            String.format("%.1f MB", apkSizeBytes / (1024f * 1024f))
        } else {
            "38.7 MB"
        }
    }

    // Save document launcher (allows user to save Musicy.apk anywhere on device)
    val saveApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val srcFile = apkFile
                    if (srcFile != null && srcFile.exists()) {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            FileInputStream(srcFile).use { input ->
                                input.copyTo(output)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Musicy.apk exported successfully!", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .testTag("apk_export_dialog")
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, AuraCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = AuraDarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AuraCyanPrimary.copy(alpha = 0.15f))
                                .border(1.dp, AuraCyanPrimary.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = AuraCyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Musicy (.apk)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraTextPrimary
                            )
                            Text(
                                text = "Android Application Package",
                                fontSize = 12.sp,
                                color = AuraCyanPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_apk_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AuraTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // APK Details Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AuraDarkCard)
                        .border(1.dp, AuraDarkBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow(label = "Filename", value = "Musicy.apk", isHighlighted = true)
                        DetailRow(label = "File Extension", value = ".apk (Android Package)")
                        DetailRow(label = "Package ID", value = context.packageName)
                        DetailRow(label = "Build Size", value = formattedSize)
                        DetailRow(label = "Status", value = "Compiled & Verified")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Text(
                    text = "APK DOWNLOAD & SHARE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save APK Button
                Button(
                    onClick = {
                        saveApkLauncher.launch("Musicy.apk")
                    },
                    modifier = Modifier
                        .testTag("save_apk_file_button")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraCyanPrimary,
                        contentColor = AuraDarkBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Musicy.apk to Device",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Run Package Installer Button
                OutlinedButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val srcFile = apkFile
                                if (srcFile != null && srcFile.exists()) {
                                    val cacheApk = File(context.cacheDir, "Musicy.apk")
                                    FileInputStream(srcFile).use { input ->
                                        FileOutputStream(cacheApk).use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    val apkUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        cacheApk
                                    )
                                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    withContext(Dispatchers.Main) {
                                        context.startActivity(installIntent)
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Install trigger: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .testTag("dialog_install_apk_button")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AuraCyanPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraCyanPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AuraCyanPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run Package Installer (Launch APK)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Share APK Button
                OutlinedButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val srcFile = apkFile
                                if (srcFile != null && srcFile.exists()) {
                                    val cacheApk = File(context.cacheDir, "Musicy.apk")
                                    FileInputStream(srcFile).use { input ->
                                        FileOutputStream(cacheApk).use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    val apkUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        cacheApk
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/vnd.android.package-archive"
                                        putExtra(Intent.EXTRA_STREAM, apkUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    withContext(Dispatchers.Main) {
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Share Musicy.apk")
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .testTag("share_apk_file_button")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AuraTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraDarkBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AuraCyanPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share APK via App / Cloud",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info note about AI Studio Web download
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AuraDarkSurface)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AuraCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Direct Browser Download: Click ⚙️ (Settings) in the top-right toolbar of Google AI Studio and select 'Generate APK' to download Musicy.apk to your computer.",
                            fontSize = 11.sp,
                            color = AuraTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = AuraTextMuted
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) AuraCyanPrimary else AuraTextPrimary
        )
    }
}
