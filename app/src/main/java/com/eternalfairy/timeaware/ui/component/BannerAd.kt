package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.wear.compose.material.Text
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.google.android.gms.ads.AdView

//@Composable
//fun BannerAd (
////    componentHeight: Dp = 90.dp,
//    componentWidth: Dp = 400.dp,
//    paddingHorizontal: Dp = 10.dp,
//    paddingVertical: Dp = 5.dp,
//    adView: AdView,
//    modifier: Modifier = Modifier
//) {
//    if (LocalInspectionMode.current) {
//        Column (
//            modifier = modifier
//                .padding(
//                    horizontal = paddingHorizontal,
//                    vertical = paddingVertical
//                )
//                .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
//                .width(componentWidth)
////                .height(componentHeight)
//                .background(COMPONENT_BACKGROUND_COLOR)
//        ) {
//            Box() {
//                Text(
//                    text = "Google Mobile Ads preview banner",
//                    modifier = modifier
//                        .align(Alignment.Center)
//                )
//            }
//        }
//
//        return
//    }
//
//    AndroidView(
//        modifier = modifier.wrapContentSize(),
//        factory = { adView }
//    )
//
//    // Pause and resume the AdView when the lifecycle is paused and resumed
//    LifecycleResumeEffect(adView) {
//        adView.resume()
//        onPauseOrDispose { adView.pause() }
//    }
//}

@Composable
fun BannerAd(adView: AdView, modifier: Modifier = Modifier) {
    // Ad load does not work in preview mode because it requires a network connection.
    if (LocalInspectionMode.current) {
        Box { Text(text = "Google Mobile Ads preview banner.", modifier.align(Alignment.Center)) }
        return
    }

    AndroidView(modifier = modifier.wrapContentSize(), factory = { adView })

    // Pause and resume the AdView when the lifecycle is paused and resumed.
    LifecycleResumeEffect(adView) {
        adView.resume()
        onPauseOrDispose { adView.pause() }
    }
}