package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private const val DIRECT_APK_DOWNLOAD_URL = "https://ais-dev-n2bt2ws2yd3gh3ssduvjmx-353236373955.europe-west2.run.app/Musicy.apk"

@Composable
fun DownloadScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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
            "38 MB"
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
                            Toast.makeText(context, "Musicy.apk saved successfully! Check your Downloads folder.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "APK file source not accessible directly; please use the browser download link.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Save error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .testTag("download_screen")
            .fillMaxSize()
            .background(AuraDarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("download_hero_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
            border = BorderStroke(1.dp, AuraCyanPrimary.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AuraCyanPrimary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(AuraCyanPrimary, Color(0xFF007A99))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = "Android App Package",
                            modifier = Modifier.size(42.dp),
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Musicy (.apk)",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AuraCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Compiled APK Ready ($formattedSize)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraCyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Download and install the standalone Musicy Android application on your smartphone or tablet.",
                        fontSize = 12.sp,
                        color = AuraTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main Action Buttons
        Text(
            text = "DOWNLOAD OPTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AuraTextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Primary Save APK to Device Button
        Button(
            onClick = {
                saveApkLauncher.launch("Musicy.apk")
            },
            modifier = Modifier
                .testTag("download_save_apk_button")
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuraCyanPrimary,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Download Musicy.apk ($formattedSize)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Install APK on this Device Button
        Button(
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
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "APK not available for direct launch.", Toast.LENGTH_SHORT).show()
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
                .testTag("download_install_apk_button")
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuraCyanPrimary.copy(alpha = 0.2f),
                contentColor = AuraCyanPrimary
            ),
            border = BorderStroke(1.dp, AuraCyanPrimary)
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
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Share APK Button
        Button(
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
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "APK file not ready for sharing.", Toast.LENGTH_SHORT).show()
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
                .testTag("download_share_apk_button")
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AuraDarkCard,
                contentColor = AuraCyanPrimary
            ),
            border = BorderStroke(1.dp, AuraCyanPrimary.copy(alpha = 0.6f))
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = AuraCyanPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share APK via Cloud / Bluetooth / Email",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Package Details Card
        Text(
            text = "PACKAGE SPECIFICATIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AuraTextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
            border = BorderStroke(1.dp, AuraDarkBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecRow(label = "Application Name", value = "Musicy")
                SpecRow(label = "Filename", value = "Musicy.apk", isHighlighted = true)
                SpecRow(label = "Format / Extension", value = ".apk (Android Package)")
                SpecRow(label = "Build Size", value = formattedSize)
                SpecRow(label = "Target Architecture", value = "Universal (ARM64 / x86_64)")
                SpecRow(label = "Package ID", value = context.packageName)
                SpecRow(label = "Min Android Version", value = "Android 8.0 (API 26)+")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step-by-Step Installation Guide
        Text(
            text = "HOW TO INSTALL ON YOUR PHONE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AuraTextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AuraDarkSurface),
            border = BorderStroke(1.dp, AuraDarkBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InstallStepRow(
                    stepNumber = "1",
                    title = "Download Musicy.apk",
                    description = "Tap the 'Download Musicy.apk' button above or click the direct browser link."
                )
                InstallStepRow(
                    stepNumber = "2",
                    title = "Open Downloaded File",
                    description = "Go to your phone's 'Downloads' folder or swipe down your notification shade and tap 'Musicy.apk'."
                )
                InstallStepRow(
                    stepNumber = "3",
                    title = "Allow Install from This Source",
                    description = "If Android asks for permission, toggle 'Allow from this source' in your device settings."
                )
                InstallStepRow(
                    stepNumber = "4",
                    title = "Enjoy High-Res FLAC Audio",
                    description = "Tap 'Install'. The Musicy app will be added to your home screen and app drawer!"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Desktop / AI Studio instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
            border = BorderStroke(1.dp, AuraDarkBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AuraCyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Downloading to your PC / Mac",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "You can also click the ⚙️ Settings menu at the top-right of AI Studio and select 'Generate APK / AAB', or right-click 'public/Musicy.apk' in the file explorer to download.",
                        fontSize = 11.sp,
                        color = AuraTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Installation Troubleshooting & Fixes Card
        Text(
            text = "INSTALLATION TROUBLESHOOTING & FIXES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AuraTextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("download_troubleshooting_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AuraDarkCard),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TroubleshootRow(
                    issue = "App Not Installed / Package Conflicts",
                    solution = "If you previously installed an earlier build of Musicy, uninstall that version from your device first before installing the new APK."
                )
                TroubleshootRow(
                    issue = "Blocked by Play Protect",
                    solution = "Because this is a custom debug build, Google Play Protect will show a warning. Tap 'More details' and then 'Install anyway'."
                )
                TroubleshootRow(
                    issue = "Can't Install Unknown Apps",
                    solution = "Go to device Settings > Apps > Special App Access > Install Unknown Apps > Select your browser / Files app > Turn on 'Allow from this source'."
                )
                TroubleshootRow(
                    issue = "Problem Parsing Package",
                    solution = "Ensure the APK file finished downloading completely (~38 MB). If corrupted, delete and re-download from AI Studio."
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TroubleshootRow(
    issue: String,
    solution: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFFF59E0B), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = issue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = solution,
            fontSize = 11.sp,
            color = AuraTextSecondary,
            lineHeight = 15.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun SpecRow(
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

@Composable
private fun InstallStepRow(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(AuraCyanPrimary.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, AuraCyanPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AuraCyanPrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = AuraTextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
