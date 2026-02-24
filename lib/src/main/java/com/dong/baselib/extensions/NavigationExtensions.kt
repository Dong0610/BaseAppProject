package com.dong.baselib.base

import android.net.Uri
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.dong.baselib.api.putAnySafe

/**
 * Navigate with custom NavOptions
 *
 * @param destination The destination ID
 * @param args Key-value pairs for bundle arguments
 * @param navOptions Custom NavOptions
 *
 * Example:
 * ```
 * val options = NavOptions.Builder()
 *     .setLaunchSingleTop(true)
 *     .setEnterAnim(R.anim.fade_in)
 *     .build()
 *
 * navController.animateNavigate(
 *     R.id.detailFragment,
 *     navOptions = options,
 *     "id" to 123
 * )
 * ```
 */
fun NavController.animateNavigateWithOption(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>,
    popUpToCurrent: Boolean = false,
    builder: NavOptions.Builder.() -> Unit = {}
) {
    val bundle = if (args.isNotEmpty()) {
        Bundle().apply {
            args.forEach { (key, value) ->
                try {
                    putAnySafe(key, value)
                } catch (_: Throwable) {
                }
            }
        }
    } else {
        null
    }
    val navOptionsBuilder = NavOptions.Builder().apply(builder)

    if (popUpToCurrent) {
        currentDestination?.id?.let { currentId ->
            navOptionsBuilder.setPopUpTo(currentId, inclusive = true)
        }
    }

    navigate(destination, bundle, navOptionsBuilder.build())
}
/**
 * Navigate with smooth animations and optionally close the current fragment
 *
 * @param destination The destination ID (e.g., R.id.someFragment)
 * @param args Key-value pairs for bundle arguments (e.g., "key" to "value")
 * @param popUpToCurrent If true, removes current fragment from back stack (closes it)
 * @param enterAnim Animation when the new destination enters
 * @param exitAnim Animation when the current destination exits
 * @param popEnterAnim Animation when returning to this destination
 * @param popExitAnim Animation when the new destination pops
 *
 * Example:
 * ```
 * // Navigate and close SearchInputFragment
 * navController.animateNavigate(
 *     R.id.resultFragment,
 *     "query" to searchText,
 *     popUpToCurrent = true  // closes SearchInputFragment
 * )
 * ```
 */
fun NavController.animateNavigate(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>,
    popUpToCurrent: Boolean = false,
    @AnimRes enterAnim: Int = com.dong.baselib.R.anim.fade_in,
    @AnimRes exitAnim: Int = com.dong.baselib.R.anim.fade_out,
    @AnimRes popEnterAnim: Int = com.dong.baselib.R.anim.fade_in,
    @AnimRes popExitAnim: Int = com.dong.baselib.R.anim.fade_out
) {
    val bundle = if (args.isNotEmpty()) {
        Bundle().apply {
            args.forEach { (key, value) ->
                try {
                    putAnySafe(key, value)
                } catch (_: Throwable) {
                }
            }
        }
    } else {
        null
    }
    val navOptionsBuilder = NavOptions.Builder()
        .setEnterAnim(enterAnim)
        .setExitAnim(exitAnim)
        .setPopEnterAnim(popEnterAnim)
        .setPopExitAnim(popExitAnim)

    if (popUpToCurrent) {
        currentDestination?.id?.let { currentId ->
            navOptionsBuilder.setPopUpTo(currentId, inclusive = true)
        }
    }

    navigate(destination, bundle, navOptionsBuilder.build())
}

/**
 * Safe navigation that prevents crashes from rapid double-clicks or invalid states
 *
 * @param destination The destination ID
 * @param args Key-value pairs for bundle arguments
 *
 * Example:
 * ```
 * navController.safeNavigate(R.id.detailFragment, "id" to 123)
 * ```
 */
fun NavController.safeNavigate(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>
) {
    try {
        if (currentDestination?.id != destination) {
            val bundle = if (args.isNotEmpty()) {
                Bundle().apply {
                    args.forEach { (key, value) ->
                        try {
                            putAnySafe(key, value)
                        } catch (_: Throwable) {
                        }
                    }
                }
            } else {
                null
            }
            navigate(destination, bundle)
        }
    } catch (_: Exception) {
    }
}

/**
 * Navigate with single top behavior (avoid creating duplicate destinations)
 *
 * @param destination The destination ID
 * @param args Key-value pairs for bundle arguments
 *
 * Example:
 * ```
 * navController.navigateSingleTop(R.id.homeFragment)
 * ```
 */
fun NavController.navigateSingleTop(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>,
    @AnimRes enterAnim: Int = com.dong.baselib.R.anim.fade_in,
    @AnimRes exitAnim: Int = com.dong.baselib.R.anim.fade_out
) {
    val bundle = if (args.isNotEmpty()) {
        Bundle().apply {
            args.forEach { (key, value) ->
                try {
                    putAnySafe(key, value)
                } catch (_: Throwable) {
                }
            }
        }
    } else {
        null
    }

    val navOptions = NavOptions.Builder()
        .setLaunchSingleTop(true)
        .setEnterAnim(enterAnim)
        .setExitAnim(exitAnim)
        .build()

    navigate(destination, bundle, navOptions)
}

/**
 * Navigate and clear entire back stack (useful for login/logout flows)
 *
 * @param destination The destination ID
 * @param args Key-value pairs for bundle arguments
 *
 * Example:
 * ```
 * // After login success, navigate to home and clear login screens
 * navController.navigateClearBackStack(R.id.homeFragment)
 * ```
 */
fun NavController.navigateClearBackStack(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>,
    @AnimRes enterAnim: Int = com.dong.baselib.R.anim.fade_in,
    @AnimRes exitAnim: Int = com.dong.baselib.R.anim.fade_out
) {
    val bundle = if (args.isNotEmpty()) {
        Bundle().apply {
            args.forEach { (key, value) ->
                try {
                    putAnySafe(key, value)
                } catch (_: Throwable) {
                }
            }
        }
    } else {
        null
    }

    graph.startDestinationId.let { startId ->
        val navOptions = NavOptions.Builder()
            .setPopUpTo(startId, inclusive = true)
            .setLaunchSingleTop(true)
            .setEnterAnim(enterAnim)
            .setExitAnim(exitAnim)
            .build()

        navigate(destination, bundle, navOptions)
    }
}

/**
 * Navigate and pop back to a specific destination
 *
 * @param destination The destination ID to navigate to
 * @param popUpTo The destination ID to pop back to
 * @param inclusive Whether to also pop the popUpTo destination
 * @param args Key-value pairs for bundle arguments
 *
 * Example:
 * ```
 * // Navigate to result and pop back to home (removing intermediate screens)
 * navController.navigatePopUpTo(
 *     destination = R.id.resultFragment,
 *     popUpTo = R.id.homeFragment,
 *     inclusive = false
 * )
 * ```
 */
fun NavController.navigatePopUpTo(
    @IdRes destination: Int,
    @IdRes popUpTo: Int,
    inclusive: Boolean = false,
    vararg args: Pair<String, Any?>,
    @AnimRes enterAnim: Int = com.dong.baselib.R.anim.fade_in,
    @AnimRes exitAnim: Int = com.dong.baselib.R.anim.fade_out
) {
    val bundle = if (args.isNotEmpty()) {
        Bundle().apply {
            args.forEach { (key, value) ->
                try {
                    putAnySafe(key, value)
                } catch (_: Throwable) {
                }
            }
        }
    } else {
        null
    }

    val navOptions = NavOptions.Builder()
        .setPopUpTo(popUpTo, inclusive)
        .setEnterAnim(enterAnim)
        .setExitAnim(exitAnim)
        .build()

    navigate(destination, bundle, navOptions)
}

/**
 * Navigate with slide animations (slide in from right, slide out to left)
 *
 * @param destination The destination ID
 * @param args Key-value pairs for bundle arguments
 * @param popUpToCurrent If true, removes current fragment from back stack
 *
 * Example:
 * ```
 * navController.navigateSlide(R.id.detailFragment, "id" to 123)
 * ```
 */
fun NavController.navigateSlide(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>,
    popUpToCurrent: Boolean = false
) {
    animateNavigate(
        destination = destination,
        args = args,
        popUpToCurrent = popUpToCurrent,
        enterAnim = com.dong.baselib.R.anim.slide_in_right,
        exitAnim = com.dong.baselib.R.anim.slide_out_left,
        popEnterAnim = com.dong.baselib.R.anim.slide_in_left,
        popExitAnim = com.dong.baselib.R.anim.slide_out_right
    )
}

/**
 * Navigate with slide up animations (slide in from bottom)
 *
 * @param destination The destination ID
 * @param args Key-value pairs for bundle arguments
 * @param popUpToCurrent If true, removes current fragment from back stack
 *
 * Example:
 * ```
 * navController.navigateSlideUp(R.id.modalFragment)
 * ```
 */
fun NavController.navigateSlideUp(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>,
    popUpToCurrent: Boolean = false
) {
    animateNavigate(
        destination = destination,
        args = args,
        popUpToCurrent = popUpToCurrent,
        enterAnim = com.dong.baselib.R.anim.slide_in_bottom,
        exitAnim = com.dong.baselib.R.anim.fade_out,
        popEnterAnim = com.dong.baselib.R.anim.fade_in,
        popExitAnim = com.dong.baselib.R.anim.slide_out_bottom
    )
}

/**
 * Navigate using deep link URI
 *
 * @param deepLink The deep link URI string
 * @param navOptions Optional NavOptions
 *
 * Example:
 * ```
 * navController.navigateDeepLink("myapp://detail/123")
 * ```
 */
fun NavController.navigateDeepLink(
    deepLink: String,
    navOptions: NavOptions? = null
) {
    try {
        val request = NavDeepLinkRequest.Builder
            .fromUri(Uri.parse(deepLink))
            .build()
        navigate(request, navOptions)
    } catch (_: Exception) {
    }
}

/**
 * Navigate using deep link URI with animations
 *
 * @param deepLink The deep link URI string
 * @param enterAnim Animation when the new destination enters
 * @param exitAnim Animation when the current destination exits
 *
 * Example:
 * ```
 * navController.navigateDeepLinkAnimated("myapp://detail/123")
 * ```
 */
fun NavController.navigateDeepLinkAnimated(
    deepLink: String,
    @AnimRes enterAnim: Int = com.dong.baselib.R.anim.fade_in,
    @AnimRes exitAnim: Int = com.dong.baselib.R.anim.fade_out
) {
    val navOptions = NavOptions.Builder()
        .setEnterAnim(enterAnim)
        .setExitAnim(exitAnim)
        .build()
    navigateDeepLink(deepLink, navOptions)
}

/**
 * Safe pop back stack that catches exceptions
 *
 * @return true if successfully popped, false otherwise
 *
 * Example:
 * ```
 * navController.popBackStackSafe()
 * ```
 */
fun NavController.popBackStackSafe(): Boolean {
    return try {
        popBackStack()
    } catch (_: Exception) {
        false
    }
}

/**
 * Pop back stack to a specific destination
 *
 * @param destination The destination ID to pop back to
 * @param inclusive Whether to also pop the destination itself
 * @return true if successfully popped, false otherwise
 *
 * Example:
 * ```
 * navController.popBackStackTo(R.id.homeFragment, inclusive = false)
 * ```
 */
fun NavController.popBackStackTo(
    @IdRes destination: Int,
    inclusive: Boolean = false
): Boolean {
    return try {
        popBackStack(destination, inclusive)
    } catch (_: Exception) {
        false
    }
}

/**
 * Check if current destination matches the given ID
 *
 * @param destinationId The destination ID to check
 * @return true if current destination matches, false otherwise
 *
 * Example:
 * ```
 * if (navController.isCurrentDestination(R.id.homeFragment)) {
 *     // Already at home
 * }
 * ```
 */
fun NavController.isCurrentDestination(@IdRes destinationId: Int): Boolean {
    return currentDestination?.id == destinationId
}

/**
 * Check if back stack contains a specific destination
 *
 * @param destinationId The destination ID to check
 * @return true if destination is in back stack, false otherwise
 *
 * Example:
 * ```
 * if (navController.hasDestinationInBackStack(R.id.loginFragment)) {
 *     // Login screen is in back stack
 * }
 * ```
 */
fun NavController.hasDestinationInBackStack(@IdRes destinationId: Int): Boolean {
    return try {
        getBackStackEntry(destinationId)
        true
    } catch (_: Exception) {
        false
    }
}

/**
 * Navigate only if not already at the destination
 *
 * @param destination The destination ID
 * @param args Key-value pairs for bundle arguments
 *
 * Example:
 * ```
 * navController.navigateIfNeeded(R.id.homeFragment)
 * ```
 */
fun NavController.navigateIfNeeded(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>,
    @AnimRes enterAnim: Int = com.dong.baselib.R.anim.fade_in,
    @AnimRes exitAnim: Int = com.dong.baselib.R.anim.fade_out
) {
    if (!isCurrentDestination(destination)) {
        val bundle = if (args.isNotEmpty()) {
            Bundle().apply {
                args.forEach { (key, value) ->
                    try {
                        putAnySafe(key, value)
                    } catch (_: Throwable) {
                    }
                }
            }
        } else {
            null
        }

        val navOptions = NavOptions.Builder()
            .setEnterAnim(enterAnim)
            .setExitAnim(exitAnim)
            .build()

        navigate(destination, bundle, navOptions)
    }
}

/**
 * Set result for previous fragment using SavedStateHandle
 *
 * @param key The result key
 * @param value The result value
 *
 * Example:
 * ```
 * navController.setResult("selected_item", item)
 * navController.popBackStack()
 * ```
 */
fun <T> NavController.setResult(key: String, value: T) {
    previousBackStackEntry?.savedStateHandle?.set(key, value)
}

/**
 * Get result from SavedStateHandle as LiveData
 *
 * @param key The result key
 * @return LiveData containing the result
 *
 * Example:
 * ```
 * navController.getResultLiveData<String>("selected_item")
 *     .observe(viewLifecycleOwner) { result ->
 *         // Handle result
 *     }
 * ```
 */
fun <T> NavController.getResultLiveData(key: String) =
    currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)

/**
 * Extension for Fragment to navigate safely
 */
fun Fragment.safeNavigate(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>
) {
    try {
        findNavController().safeNavigate(destination, *args)
    } catch (_: Exception) {
    }
}

/**
 * Extension for Fragment to navigate with animations
 */
fun Fragment.animateNavigate(
    @IdRes destination: Int,
    vararg args: Pair<String, Any?>,
    popUpToCurrent: Boolean = false,
    @AnimRes enterAnim: Int = com.dong.baselib.R.anim.fade_in,
    @AnimRes exitAnim: Int = com.dong.baselib.R.anim.fade_out
) {
    try {
        findNavController().animateNavigate(
            destination = destination,
            args = args,
            popUpToCurrent = popUpToCurrent,
            enterAnim = enterAnim,
            exitAnim = exitAnim
        )
    } catch (_: Exception) {
    }
}

/**
 * Extension for Fragment to pop back stack safely
 */
fun Fragment.popBackStackSafe(): Boolean {
    return try {
        findNavController().popBackStackSafe()
    } catch (_: Exception) {
        false
    }
}

/**
 * Extension for Fragment to set navigation result
 */
fun <T> Fragment.setNavigationResult(key: String, value: T) {
    try {
        findNavController().setResult(key, value)
    } catch (_: Exception) {
    }
}

/**
 * Extension for Fragment to observe navigation result with auto-cleanup
 *
 * Example:
 * ```
 * observeNavigationResult<String>("selected_item") { result ->
 *     // Handle result
 * }
 * ```
 */
inline fun <reified T> Fragment.observeNavigationResult(
    key: String,
    crossinline onResult: (T) -> Unit
) {
    val navController = try {
        findNavController()
    } catch (_: Exception) {
        return
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle ?: return

    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            savedStateHandle.get<T>(key)?.let { result ->
                onResult(result)
                savedStateHandle.remove<T>(key)
            }
        }
    })
}
