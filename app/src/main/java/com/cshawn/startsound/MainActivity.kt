package com.cshawn.startsound

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private lateinit var startChooser: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Column {
                        button(getString(R.string.select_audio)) {
                            selectAudioFile()
                        }
                        button(getString(R.string.reset_audio)) {
                            getSharedPreferences(spName, Context.MODE_PRIVATE)?.edit()?.remove(pathKey)?.apply()
                            startPlay(this@MainActivity)
                        }
                    }
                }
                Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = getString(R.string.author),
                            color = Color.Gray,
                        )
                        Text(
                            text = "V "+packageManager.getPackageInfo(packageName, 0).versionName,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }
        startChooser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.apply {
                    onChoose(this)
                }
            }
        }
        getSharedPreferences(spName, Context.MODE_PRIVATE)?.getString(pathKey, null).also { path ->
            startPlay(this, path)
        }
    }

    private fun selectAudioFile() {
        val param = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_TITLE, getString(R.string.select_audio))
            putExtra(Intent.EXTRA_INTENT, param)
        }
        startChooser.launch(chooser)
    }

    private fun onChoose(intent: Intent) {
        intent.takeIf {
            it.type?.startsWith("audio/") == true
        }?.apply {
            getSharedPreferences(spName, Context.MODE_PRIVATE)?.edit()?.putString(pathKey, dataString)?.apply()
            dataString?.takeIf { it.isNotEmpty() }?.also{ startPlay(this@MainActivity, it) }
        } ?: Toast.makeText(this, getText(R.string.select_audio_file), Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        stopPlay()
    }

    @Composable
    fun button(text: String, onClick: () -> Unit) {
        val interactionSource = remember {
            MutableInteractionSource()
        }
        val pressState by interactionSource.collectIsPressedAsState()

        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
            modifier = Modifier.wrapContentSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = if (pressState)
                        R.drawable.btn_pressed else R.drawable.btn),
                    contentDescription = "",
                    modifier = Modifier.size(150.dp, 40.dp)
                )
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                )
            }
        }
    }
}

private var mediaPlayer: MediaPlayer? = null
private var fd: AssetFileDescriptor? = null

fun startPlay(context: Context, path: String? = null) {
    try {
        stopPlay()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.apply{
            path?.takeIf { it.isNotEmpty() }?.apply {
                val uri = Uri.parse(path)
                context.grantUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataSource(context, uri)
            } ?: context.assets.openFd("start.mp3").also {
                fd = it
                setDataSource(it)
            }
            prepare()
            start()
            setOnCompletionListener {
                stopPlay()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun stopPlay() {
    try {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        fd?.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}