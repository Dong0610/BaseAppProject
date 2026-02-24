package com.dong.baselib.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.dong.baselib.api.UnitFun0
import com.dong.baselib.api.hideSystemBar
import com.dong.baselib.api.putExtraSmart
import com.dong.baselib.api.setFullScreen
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Base Activity with ViewBinding, lifecycle utilities, and common helpers.
 *
 * Example usage:
 * ```kotlin
 * class MainActivity : BaseActivity<ActivityMainBinding>(
 *     ActivityMainBinding::inflate,
 *     fullStatus = false
 * ) {
 *     override fun initialize() {
 *         // Called before setData and onClick
 *     }
 *
 *     override fun ActivityMainBinding.setData() {
 *         tvTitle.text = "Hello"
 *         loadData()
 *     }
 *
 *     override fun ActivityMainBinding.onClick() {
 *         btnSubmit.click { submit() }
 *         btnBack.click { finish() }
 *     }
 *
 *     override fun backPressed() {
 *         showExitDialog()
 *     }
 * }
 * ```
 */
abstract class BaseActivity<VB : ViewBinding>(
      open val bindingFactory: (LayoutInflater) -> VB,
      open var fullStatus: Boolean = false,
) : AppCompatActivity(), FragmentAttachEvent {

   
    // region Core Properties
   

    val binding: VB by lazy { bindingFactory(layoutInflater) }

    private val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    val statusBarHeight: Int
        @SuppressLint("DiscouragedApi", "InternalInsetResource")
        get() {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }

    val navigationBarHeight: Int
        @SuppressLint("DiscouragedApi", "InternalInsetResource")
        get() {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }

    // endregion

   
    // region Abstract Methods
   

    /** Called when back button is pressed */
    abstract fun backPressed()

    /** Called before setData and onClick, use for initial setup */
    abstract fun initialize()

    /** Setup data and UI state */
    abstract fun VB.setData()

    /** Setup click listeners */
    abstract fun VB.onClick()

    /** Return false to skip first flow language check */
    open fun isFinishFirstFlow(): Boolean = true

    // endregion

   
    // region Lifecycle
   

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val content = findViewById<View>(android.R.id.content)
        content.observeKeyboardState(this) { state ->
            keyboardState = KeyboardState(heightPx = state.heightPx, visible = state.visible)
        }

        setContentView(binding.root)

        if (!fullStatus) {
            val paddingTop = binding.root.paddingTop + statusBarHeight
            binding.root.setPadding(
                binding.root.paddingLeft,
                paddingTop,
                binding.root.paddingRight,
                binding.root.paddingBottom
            )
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        })

        initialize()
        binding.setData()
        binding.onClick()

        window.hideSystemBar()
        window.setFullScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
        activityResultCallback = null
        observer?.let { lifecycle.removeObserver(it) }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            mainHandler.post { hideNavigation() }
        }
    }

    // endregion

   
    // region Intent Extras - Easy access to intent data
   

    /** Get String extra with default value */
    fun stringExtra(key: String, default: String = ""): String =
        intent.getStringExtra(key) ?: default

    /** Get Int extra with default value */
    fun intExtra(key: String, default: Int = 0): Int =
        intent.getIntExtra(key, default)

    /** Get Long extra with default value */
    fun longExtra(key: String, default: Long = 0L): Long =
        intent.getLongExtra(key, default)

    /** Get Boolean extra with default value */
    fun boolExtra(key: String, default: Boolean = false): Boolean =
        intent.getBooleanExtra(key, default)

    /** Get Float extra with default value */
    fun floatExtra(key: String, default: Float = 0f): Float =
        intent.getFloatExtra(key, default)

    /** Get Double extra with default value */
    fun doubleExtra(key: String, default: Double = 0.0): Double =
        intent.getDoubleExtra(key, default)

    /** Get Parcelable extra */
    inline fun <reified T : android.os.Parcelable> parcelableExtra(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(key)
        }

    /** Get Serializable extra */
    inline fun <reified T : java.io.Serializable> serializableExtra(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(key) as? T
        }

    /** Get ArrayList String extra */
    fun stringArrayListExtra(key: String): ArrayList<String>? =
        intent.getStringArrayListExtra(key)

    /** Get ArrayList Int extra */
    fun intArrayListExtra(key: String): ArrayList<Int>? =
        intent.getIntegerArrayListExtra(key)

    // endregion

   
    // region Navigation - Launch activities
   

    /**
     * Launch activity with optional extras
     *
     * Example:
     * ```kotlin
     * launchActivity<DetailActivity>(
     *     "id" to 123,
     *     "name" to "John"
     * )
     * ```
     */
    inline fun <reified T : Any> launchActivity(vararg params: Pair<String, Any?>) {
        val intent = Intent(this, T::class.java).apply {
            params.forEach { (key, value) ->
                try {
                    putExtraSmart(key, value)
                }
                catch (t: Throwable) {
                    android.util.Log.w("launchActivity", "Skip extra \"$key\": ${t.message}")
                }
            }
        }
        startActivity(intent)
    }

    /**
     * Launch activity and finish current
     */
    inline fun <reified T : Any> launchAndFinish(vararg params: Pair<String, Any?>) {
        launchActivity<T>(*params)
        finish()
    }

    /**
     * Launch activity and clear task
     */
    inline fun <reified T : Any> launchAndClearTask(vararg params: Pair<String, Any?>) {
        val intent = Intent(this, T::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            params.forEach { (key, value) ->
                try {
                    putExtraSmart(key, value)
                }
                catch (t: Throwable) {
                    android.util.Log.w("launchActivity", "Skip extra \"$key\": ${t.message}")
                }
            }
        }
        startActivity(intent)
    }

    // endregion

   
    // region Activity Result
   

    var activityResultCallback: ((ActivityResult) -> Unit)? = null

    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        listenerResult(result)
        activityResultCallback?.invoke(result)
        activityResultCallback = null
    }

    /** Override to handle activity result */
    open fun listenerResult(result: ActivityResult) {}

    /**
     * Launch activity for result
     *
     * Example:
     * ```kotlin
     * launcherForResult<PickImageActivity>("type" to "gallery") { result ->
     *     if (result.resultCode == RESULT_OK) {
     *         val uri = result.data?.data
     *         // handle result
     *     }
     * }
     * ```
     */
    inline fun <reified T : Any> launcherForResult(
          vararg params: Pair<String, Any?>,
          noinline dataResult: (ActivityResult) -> Unit = { _ -> }
    ) {
        activityResultCallback = dataResult
        val intent = Intent(this, T::class.java).apply {
            params.forEach { (key, value) ->
                try {
                    putExtraSmart(key, value)
                }
                catch (t: Throwable) {
                    android.util.Log.w("launchActivity", "Skip extra \"$key\": ${t.message}")
                }
            }
        }
        resultLauncher.launch(intent)
    }

    // endregion

   
    // region Coroutines - Lifecycle-aware async operations
   

    /** Launch coroutine on Main dispatcher */
    fun launchMain(block: suspend CoroutineScope.() -> Unit): Job =
        lifecycleScope.launch(Dispatchers.Main, block = block)

    /** Launch coroutine on IO dispatcher */
    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
        lifecycleScope.launch(Dispatchers.IO, block = block)

    /** Launch coroutine on Default dispatcher */
    fun launchDefault(block: suspend CoroutineScope.() -> Unit): Job =
        lifecycleScope.launch(Dispatchers.Default, block = block)

    /** Run block after delay on main thread using coroutines */
    fun runDelayed(delayMs: Long, action: () -> Unit): Job = launchMain {
        delay(delayMs)
        action()
    }
    suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.Main, block = block)

    suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.IO, block = block)

    suspend fun <T> withDefault(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.Default, block = block)
    /** Run block on main thread */
    fun runOnMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    /** Post action to main handler */
    fun post(action: () -> Unit) = mainHandler.post(action)

    /** Post action to main handler with delay */
    fun postDelayed(delayMs: Long, action: () -> Unit) =
        mainHandler.postDelayed(action, delayMs)

    // endregion

   
    // region Toast & Snackbar
   

    /** Show short toast */
    fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /** Show short toast from string resource */
    fun toast(@StringRes resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    /** Show long toast */
    fun toastLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /** Show long toast from string resource */
    fun toastLong(@StringRes resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
    }

    /** Show snackbar */
    fun snackbar(
          message: String,
          duration: Int = Snackbar.LENGTH_SHORT,
          actionText: String? = null,
          action: UnitFun0? = null
    ) {
        val snackbar = Snackbar.make(binding.root, message, duration)
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) { action() }
        }
        snackbar.show()
    }

    /** Show snackbar from string resource */
    fun snackbar(
          @StringRes messageRes: Int,
          duration: Int = Snackbar.LENGTH_SHORT,
          @StringRes actionTextRes: Int? = null,
          action: UnitFun0? = null
    ) {
        val snackbar = Snackbar.make(binding.root, messageRes, duration)
        if (actionTextRes != null && action != null) {
            snackbar.setAction(actionTextRes) { action() }
        }
        snackbar.show()
    }

    // endregion

   
    // region Keyboard
   

    data class KeyboardState(val visible: Boolean, val heightPx: Int)

    companion object {
        @Volatile
        var keyboardState: KeyboardState = KeyboardState(false, 0)
    }

    /** Show keyboard for specific view */
    open fun showKeyboard(view: View?) {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
        catch (_: Throwable) {
        }
    }

    /** Show keyboard for root view */
    open fun showKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.root, InputMethodManager.SHOW_IMPLICIT)
        }
        catch (_: Throwable) {
        }
    }

    /** Hide keyboard */
    open fun hideKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
        }
        catch (_: Throwable) {
        }
    }

    /** Auto hide keyboard when touch outside EditText */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val focusedView = currentFocus
        if (focusedView is EditText) {
            val rect = Rect()
            focusedView.getGlobalVisibleRect(rect)
            if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                hideKeyboard()
                focusedView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private var observer: DefaultLifecycleObserver? = null

    fun View.observeKeyboardState(
          owner: LifecycleOwner,
          onChanged: (KeyboardState) -> Unit
    ): () -> Unit {
        val root = this
        val applyListener = { _: View, insets: WindowInsetsCompat ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val visible = insets.isVisible(WindowInsetsCompat.Type.ime())
            onChanged(KeyboardState(visible, imeInsets.bottom))
            insets
        }
        val compatListener = androidx.core.view.OnApplyWindowInsetsListener(applyListener)
        val animCallback =
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                override fun onProgress(
                      insets: WindowInsetsCompat,
                      runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
                    val visible = insets.isVisible(WindowInsetsCompat.Type.ime())
                    onChanged(KeyboardState(visible, imeInsets.bottom))
                    return insets
                }
            }
        val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            root.getWindowVisibleDisplayFrame(r)
            val screenHeight = root.rootView.height
            val heightDiff = (screenHeight - r.bottom).coerceAtLeast(0)
            val thresholdPx = (root.resources.displayMetrics.density * 100).toInt()
            val visible = heightDiff > thresholdPx
            val height = if (visible) heightDiff else 0
            onChanged(KeyboardState(visible, height))
        }

        fun cleanup() {
            ViewCompat.setOnApplyWindowInsetsListener(root, null)
            ViewCompat.setWindowInsetsAnimationCallback(root, null)
            root.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        }

        observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) = cleanup()
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, compatListener)
        ViewCompat.setWindowInsetsAnimationCallback(root, animCallback)
        root.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        observer?.let { owner.lifecycle.addObserver(it) }

        root.post { onChanged(root.currentKeyboardState()) }
        return { cleanup() }
    }

    fun View.currentKeyboardState(): KeyboardState {
        val insets = ViewCompat.getRootWindowInsets(this) ?: return KeyboardState(
            visible = false,
            heightPx = 0
        )
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val visible = insets.isVisible(WindowInsetsCompat.Type.ime())
        return KeyboardState(visible, imeInsets.bottom)
    }

    // endregion

   
    // region System UI
   

    /** Hide navigation bar */
    fun hideNavigation() {
        val controller = if (Build.VERSION.SDK_INT >= 30) {
            WindowCompat.getInsetsController(window, window.decorView)
        } else {
            WindowInsetsControllerCompat(window, binding.root)
        }
        controller.let {
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            it.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    /** Show navigation bar */
    fun showNavigation() {
        val controller = if (Build.VERSION.SDK_INT >= 30) {
            WindowCompat.getInsetsController(window, window.decorView)
        } else {
            WindowInsetsControllerCompat(window, binding.root)
        }
        controller.show(WindowInsetsCompat.Type.navigationBars())
    }

    /** Hide status bar */
    fun hideStatusBar() {
        val controller = if (Build.VERSION.SDK_INT >= 30) {
            WindowCompat.getInsetsController(window, window.decorView)
        } else {
            WindowInsetsControllerCompat(window, binding.root)
        }
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }

    /** Show status bar */
    fun showStatusBar() {
        val controller = if (Build.VERSION.SDK_INT >= 30) {
            WindowCompat.getInsetsController(window, window.decorView)
        } else {
            WindowInsetsControllerCompat(window, binding.root)
        }
        controller.show(WindowInsetsCompat.Type.statusBars())
    }

    /** Set light status bar (dark icons) */
    fun setLightStatusBar(light: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = light
    }

    /** Set light navigation bar (dark icons) */
    fun setLightNavigationBar(light: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightNavigationBars = light
    }

    // endregion

   
    // region Settings & Permissions
   

    /** Open app settings */
    open fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    /** Check if should show permission rationale */
    fun shouldShowDialog(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true
            }
        }
        return false
    }

    // endregion

   
    // region Locale
   

    override fun attachBaseContext(newBase: Context?) {
        if (newBase == null) {
            super.attachBaseContext(null)
            return
        }
        val languageCode = if (isFinishFirstFlow()) {
            LocateManager.getPreLanguage(newBase)
        } else {
            LocateManager.getDeviceLanguageCode()
        }
        val localizedContext = applyLocale(newBase, languageCode)
        super.attachBaseContext(localizedContext)
    }

    fun applyLocale(context: Context, languageCode: String?): Context {
        val locale = if (languageCode.isNullOrEmpty()) {
            Locale(LocateManager.getDeviceLanguageCode())
        } else {
            val parts = languageCode.split("_")
            if (parts.size > 1) {
                Locale(parts[0], parts[1])
            } else {
                Locale(parts[0])
            }
        }
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return context.createConfigurationContext(config)
    }

    // endregion
    fun <T> StateFlow<T>.defaultCollect(
          dispatcher: CoroutineDispatcher = Dispatchers.Default,
          collector: (T) -> Unit
    ): Job = lifecycleScope.launch(dispatcher) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { collector(it) }
        }
    }
    fun <T> StateFlow<T>.IOCollect(
          dispatcher: CoroutineDispatcher = Dispatchers.IO,
          collector: (T) -> Unit
    ): Job = lifecycleScope.launch(dispatcher) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { collector(it) }
        }
    }
    fun <T> StateFlow<T>.mainCollect(
          dispatcher: CoroutineDispatcher = Dispatchers.Main,
          collector: (T) -> Unit
    ): Job = lifecycleScope.launch(dispatcher) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { collector(it) }
        }
    }
    fun <T> StateFlow<T>.collect(
          dispatcher: CoroutineDispatcher = Dispatchers.Main,
          collector: (T) -> Unit
    ): Job = lifecycleScope.launch(dispatcher) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { collector(it) }
        }
    }
}
