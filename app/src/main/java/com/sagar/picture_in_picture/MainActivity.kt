package com.sagar.picture_in_picture

import android.app.PictureInPictureParams
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.viewinterop.AndroidView
import com.sagar.picture_in_picture.ui.theme.Picture_in_pictureTheme

class MainActivity : ComponentActivity() {

    private var videoViewBounds = Rect()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Picture_in_pictureTheme {
                MainContent(
                    packageName = packageName,
                    temp = { videoViewBounds = it }
                )
            }
        }
    }


    /*
     gets called when the user leaves the current activity without explicitly finishing it,
     such as by pressing the Home button, receiving a phone call, or launching another activity.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        updatedPipParams()?.let { params ->
            if (checkSupport()) {
                enterPictureInPictureMode(params)
            }
        }

    }

    private fun updatedPipParams(): PictureInPictureParams? {
        return if (checkSupport()) {
            PictureInPictureParams.Builder()
                // provides smoother animation for transitioning to video content in PiP mode
                .setSourceRectHint(videoViewBounds)
                .setAspectRatio(Rational(16, 9))
                .build()
        } else null
    }
}


@Composable
fun MainContent(
    packageName: String,
    temp: (Rect) -> Unit
) {
    val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.video_name}")
    AndroidView(
        factory = { context ->
            VideoView(context, null).apply {
                setVideoURI(videoUri)
                start()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                val boundsInWindow = it.boundsInWindow()
                temp(
                    Rect(
                        boundsInWindow.left.toInt(),
                        boundsInWindow.top.toInt(),
                        boundsInWindow.right.toInt(),
                        boundsInWindow.bottom.toInt()
                    )
                )
            }
    )
}

private fun checkSupport() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O