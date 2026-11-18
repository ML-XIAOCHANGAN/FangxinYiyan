package com.fangxin.yiyan.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fangxin.yiyan.FangxinViewModel
import com.fangxin.yiyan.domain.CheckSource
import com.fangxin.yiyan.ui.components.NoticeCard
import com.fangxin.yiyan.ui.components.PageColumn
import com.fangxin.yiyan.ui.components.PrimaryButton
import com.fangxin.yiyan.ui.components.ProcessingOverlay
import com.fangxin.yiyan.ui.components.QuietButton
import com.fangxin.yiyan.ui.components.SecondaryButton
import com.fangxin.yiyan.ui.components.TopBackBar
import com.fangxin.yiyan.ui.components.rememberGalleryLauncher
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Line
import com.fangxin.yiyan.util.Bitmaps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CameraScreen(
    viewModel: FangxinViewModel,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var granted by remember { mutableStateOf(hasCamera(context)) }
    var decoding by remember { mutableStateOf(false) }
    val processing by viewModel.processing.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { allowed ->
        granted = allowed
        if (!allowed) onMessage("没给相机权限，可以先用相册。")
    }

    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val openGallery = rememberGalleryLauncher { uri ->
        scope.launch {
            decoding = true
            val bitmap = withContext(Dispatchers.IO) { Bitmaps.fromUri(context, uri) }
            decoding = false
            if (bitmap == null) {
                onMessage("这张图打不开，换一张。")
            } else {
                viewModel.analyzeBitmap(bitmap, CheckSource.ALBUM, onDone)
            }
        }
    }

    if (!granted) {
        Box(modifier = Modifier.fillMaxSize()) {
            PageColumn {
                TopBackBar(onBack = onBack, title = "回上一步")
                Text(
                    "相机还没打开",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Ink,
                )
                Text(
                    "点下面允许，就能拍。只看这一次，不上传。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkSoft,
                )
                NoticeCard("不想开相机，也可以从相册选一张。")
                PrimaryButton(
                    text = "允许用相机",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                )
                SecondaryButton(
                    text = "从相册选",
                    onClick = openGallery,
                    icon = Icons.Rounded.PhotoLibrary,
                )
                QuietButton(text = "回上一步", onClick = onBack)
            }
            ProcessingOverlay(visible = processing || decoding)
        }
        return
    }

    CameraPreview(
        viewModel = viewModel,
        onBack = onBack,
        onMessage = onMessage,
        onDone = onDone,
        onGallery = openGallery,
        decoding = decoding,
    )
}

@Composable
private fun CameraPreview(
    viewModel: FangxinViewModel,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    onDone: () -> Unit,
    onGallery: () -> Unit,
    decoding: Boolean,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val processing by viewModel.processing.collectAsState()

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val providerFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    DisposableEffect(Unit) {
        val listener = Runnable {
            runCatching {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
            }.onFailure {
                onMessage("相机打不开，可以先用相册。")
            }
        }
        providerFuture.addListener(listener, executor)
        onDispose {
            runCatching {
                if (providerFuture.isDone) providerFuture.get().unbindAll()
            }
        }
    }

    val takePhoto: () -> Unit = {
        if (!processing) {
            imageCapture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val degrees = image.imageInfo.rotationDegrees
                        val raw = runCatching { image.toBitmap() }.getOrNull()
                        image.close()
                        if (raw == null) {
                            onMessage("这张没拍好，再按一次。")
                        } else {
                            viewModel.analyzeBitmap(
                                Bitmaps.rotate(raw, degrees),
                                CheckSource.PHOTO,
                                onDone,
                            )
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onMessage("没拍上，再按一次。")
                    }
                },
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.86f)
                .height(300.dp)
                .border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "把字放进框里",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                "对准屏幕上的字，手别抖。",
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            )
            Spacer(Modifier.height(18.dp))
            IconButton(
                onClick = takePhoto,
                enabled = !processing,
                modifier = Modifier
                    .size(94.dp)
                    .background(Color.White, CircleShape),
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .border(4.dp, Line, CircleShape)
                        .background(Color.White, CircleShape),
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onGallery) {
                Text(
                    "用相册选",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        ProcessingOverlay(visible = processing || decoding, title = "正在看，请稍等")
    }
}

private fun hasCamera(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
