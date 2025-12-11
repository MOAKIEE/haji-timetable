package com.example.timetable.ui.screens

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.timetable.R
import com.example.timetable.ui.theme.TimetableTheme
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * 二维码扫描 Activity
 */
class QRCodeScanActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_SCAN_RESULT = "scan_result"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TimetableTheme {
                QRCodeScanScreen(
                    onScanComplete = { result ->
                        val intent = Intent().apply {
                            putExtra(EXTRA_SCAN_RESULT, result)
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScanScreen(
    onScanComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var showScanner by remember { mutableStateOf(false) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                // 解析二维码
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                
                val source = RGBLuminanceSource(width, height, pixels)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                
                val result = MultiFormatReader().decode(binaryBitmap)
                onScanComplete(result.text)
            } catch (e: Exception) {
                // 解析失败，继续显示选择界面
                showScanner = false
                e.printStackTrace()
            }
        } ?: run {
            // 用户取消选择，返回选择界面
            showScanner = false
        }
    }
    
    // 二维码扫描器
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null) {
            onScanComplete(result.contents)
        } else {
            // 扫描取消，显示选择界面
            showScanner = false
        }
    }
    
    if (showScanner) {
        LaunchedEffect(Unit) {
            val options = ScanOptions().apply {
                setPrompt("将二维码放入框内")
                setBeepEnabled(true)
                setOrientationLocked(false)
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setBarcodeImageEnabled(false)
                setCameraId(0) // 使用后置摄像头
            }
            scanLauncher.launch(options)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫描二维码") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!showScanner) {
            // 显示选择界面
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { showScanner = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("扫描二维码", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_photo_album),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("从相册选择", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
