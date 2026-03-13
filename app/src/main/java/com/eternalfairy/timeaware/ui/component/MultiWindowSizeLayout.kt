package com.eternalfairy.timeaware.ui.component

//import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

/**
 * A utility function to display different composable layouts based on the current window size class.
 *
 * This function acts as a wrapper around [WindowSizeClass.display], simplifying the process of defining
 * layouts for various screen sizes and orientations.
 *
 * @param default The default composable to display when no specific size class conditions are met.
 * @param expanded (Optional) The composable to display for large screen devices in landscape or desktop-like layouts.
 *                 Defaults to the [default] composable if not provided.
 * @param portrait (Optional) The composable to display for portrait configurations. Defaults to the [default] composable if not provided.
 * @param portraitTablet (Optional) The composable to display for portrait tablet configurations.
 * @param portraitPhone (Optional) The composable to display for portrait phone configurations.
 */
@Composable
fun MultiWindowSizeLayout(
    default: @Composable () -> Unit,
//    expanded: (@Composable () -> Unit)? = null,
//    portrait: (@Composable () -> Unit)? = null,
    landscapePhone: (@Composable () -> Unit)? = null,
    landscapeTablet: (@Composable () -> Unit)? = null,
    portraitPhone: (@Composable () -> Unit)? = null,
    portraitTablet: (@Composable () -> Unit)? = null
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    windowSizeClass.display(
        default = default,
//        expanded =  expanded ?: default,
//        portraitMulti = portrait ?: default,
        landscapeTablet = landscapeTablet,
        landscapePhone = landscapePhone,
        portraitPhone = portraitPhone,
        portraitTablet = portraitTablet
    )
}

/**
 * Displays a composable layout based on the current [WindowSizeClass] configuration.
 *
 * This function selects and invokes a composable lambda corresponding to the device's width and height
 * size classes. It is particularly useful for adapting layouts to different screen sizes, such as phones,
 * tablets, or larger devices.
 *
 * @param default The default composable to display when no specific size class conditions are met.
 * @param expanded The composable to display for large screen devices in landscape or desktop-like layouts.
 * @param portraitMulti The composable to display for portrait multi-window configurations (default for portrait modes).
 * @param portraitTablet (Optional) The composable to display for portrait tablet configurations.
 * @param portraitPhone (Optional) The composable to display for portrait phone configurations.
 */
@Composable
fun WindowSizeClass.display(
    default: @Composable () -> Unit,
//    expanded: @Composable () -> Unit,
//    portraitMulti: @Composable () -> Unit,
    landscapeTablet: (@Composable () -> Unit)? = null,
    landscapePhone: (@Composable () -> Unit)? = null,
    portraitTablet: (@Composable () -> Unit)? = null,
    portraitPhone: (@Composable () -> Unit)? = null
) {
    when {
        // Portrait phone configuration
        (
            // The width is below 600 dp (Compact) and the height is at least 480 dp (Medium)
            !isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
                    && isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)
                ) -> {
//            portraitPhone?.invoke() ?: portraitMulti()
            portraitPhone?.invoke()
        }

        // Landscape phone configuration
        (
            // The width is at least 600 dp (Medium) and the height is below 480 dp (Compact)
            isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
                    && !isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)
            ) -> {
//            landscapePhone?.invoke() ?: portraitMulti()
            landscapePhone?.invoke()
        }

//        // Expanded (landscape or large screen) configuration
//        // The width is at least 840 dp (Expanded width) and the height is at least 480 dp (Medium height)
//        isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) && isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND) -> {
//            expanded()
//        }

        // Portrait tablet configuration
        (
                // The width is at least 600 dp (Medium) and the height is at least 900 dp (Expanded)
                isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
                        && isHeightAtLeastBreakpoint(HEIGHT_DP_EXPANDED_LOWER_BOUND)
                ) -> {
            portraitTablet?.invoke()
        }

        // Landscape tablet configuration
        (
                // The width is at least 840 dp (Expanded) and the height is at least 480 dp (Medium)
                isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
                        && isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)
                ) -> {
            landscapeTablet?.invoke()
        }

        // Default fallback
        else -> {
            default()
        }
    }
}