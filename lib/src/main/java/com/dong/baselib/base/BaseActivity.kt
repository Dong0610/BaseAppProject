package com.dong.baselib.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import androidx.viewbinding.ViewBinding
import com.dong.baselib.api.UnitFun0
import com.dong.baselib.api.hideSystemBar
import com.dong.baselib.api.putExtraSmart
import com.dong.baselib.api.setFullScreen
import com.dong.baselib.lifecycle.launchCollect
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

abstract class BaseActivity<VB : ViewBinding>(
    open val bindingFactory: (LayoutInflater) -> VB,
    open var fullStatus: Boolean = false,
) : AppCompatActivity(), FragmentAttachEvent {

    val binding: VB by lazy { bindingFactory(layoutInflater) }

    private val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    val statusBarHeight: Int by lazy {
        @SuppressLint("DiscouragedApi", "InternalInsetResource")
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    val navigationBarHeight: Int by lazy {
        @SuppressLint("DiscouragedApi", "InternalInsetResource")
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private val windowInsetsController: WindowInsetsControllerCompat by lazy {
        WindowCompat.getInsetsController(window, window.decorView)
    }

    var keyboardState: KeyboardState = KeyboardState(false, 0)
        private set

    abstract fun backPressed()
    abstract fun initialize()
    abstract fun VB.setData()
    abstract fun VB.onClick()
    open fun isFinishFirstFlow(): Boolean = true

    private var statusBarPaddingApplied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val content = findViewById<View>(android.R.id.content)
        content.observeKeyboardState(this) { state ->
            keyboardState = state
        }

        setContentView(binding.root)

        if (!fullStatus && !statusBarPaddingApplied) {
            statusBarPaddingApplied = true
            binding.root.setPadding(
                binding.root.paddingLeft,
                binding.root.paddingTop + statusBarHeight,
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
        cleanupKeyboardObservers()
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

    fun stringExtra(key: String, default: String = ""): String =
        intent.getStringExtra(key) ?: default

    fun intExtra(key: String, default: Int = 0): Int =
        intent.getIntExtra(key, default)

    fun longExtra(key: String, default: Long = 0L): Long =
        intent.getLongExtra(key, default)

    fun boolExtra(key: String, default: Boolean = false): Boolean =
        intent.getBooleanExtra(key, default)

    fun floatExtra(key: String, default: Float = 0f): Float =
        intent.getFloatExtra(key, default)

    fun doubleExtra(key: String, default: Double = 0.0): Double =
        intent.getDoubleExtra(key, default)

    inline fun <reified T : android.os.Parcelable> parcelableExtra(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(key)
        }

    inline fun <reified T : java.io.Serializable> serializableExtra(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(key) as? T
        }

    fun stringArrayListExtra(key: String): ArrayList<String>? =
        intent.getStringArrayListExtra(key)

    fun intArrayListExtra(key: String): ArrayList<Int>? =
        intent.getIntegerArrayListExtra(key)

    inline fun <reified T : Any> launchActivity(vararg params: Pair<String, Any?>) {
        val intent = Intent(this, T::class.java).apply {
            params.forEach { (key, value) ->
                try {
                    putExtraSmart(key, value)
                } catch (t: Throwable) {
                    android.util.Log.w("launchActivity", "Skip extra \"$key\": ${t.message}")
                }
            }
        }
        startActivity(intent)
    }

    inline fun <reified T : Any> launchAndFinish(vararg params: Pair<String, Any?>) {
        launchActivity<T>(*params)
        finish()
    }

    inline fun <reified T : Any> launchAndClearTask(vararg params: Pair<String, Any?>) {
        val intent = Intent(this, T::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            params.forEach { (key, value) ->
                try {
                    putExtraSmart(key, value)
                } catch (t: Throwable) {
                    android.util.Log.w("launchActivity", "Skip extra \"$key\": ${t.message}")
                }
            }
        }
        startActivity(intent)
    }

    var activityResultCallback: ((ActivityResult) -> Unit)? = null

    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        listenerResult(result)
        activityResultCallback?.invoke(result)
        activityResultCallback = null
    }

    open fun listenerResult(result: ActivityResult) {}

    inline fun <reified T : Any> launcherForResult(
        vararg params: Pair<String, Any?>,
        noinline dataResult: (ActivityResult) -> Unit = { _ -> }
    ) {
        activityResultCallback = dataResult
        val intent = Intent(this, T::class.java).apply {
            params.forEach { (key, value) ->
                try {
                    putExtraSmart(key, value)
                } catch (t: Throwable) {
                    android.util.Log.w("launchActivity", "Skip extra \"$key\": ${t.message}")
                }
            }
        }
        resultLauncher.launch(intent)
    }

    fun launchMain(block: suspend CoroutineScope.() -> Unit): Job =
        lifecycleScope.launch(Dispatchers.Main, block = block)

    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
        lifecycleScope.launch(Dispatchers.IO, block = block)

    fun launchDefault(block: suspend CoroutineScope.() -> Unit): Job =
        lifecycleScope.launch(Dispatchers.Default, block = block)

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

    fun runOnMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    fun post(action: () -> Unit) = mainHandler.post(action)

    fun postDelayed(delayMs: Long, action: () -> Unit) =
        mainHandler.postDelayed(action, delayMs)

    fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun toast(@StringRes resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun toastLong(@StringRes resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
    }

    fun <T> StateFlow<T>.collect(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        state: Lifecycle.State = Lifecycle.State.STARTED,
        collector: (T) -> Unit,
    ): Job = launchCollect(this@BaseActivity, dispatcher, state, collector)

    fun <T> StateFlow<T>.mainCollect(collector: (T) -> Unit) =
        collect(Dispatchers.Main, collector = collector)

    fun <T> StateFlow<T>.ioCollect(collector: (T) -> Unit) =
        collect(Dispatchers.IO, collector = collector)

    fun <T> StateFlow<T>.defaultCollect(collector: (T) -> Unit) =
        collect(Dispatchers.Default, collector = collector)

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

    data class KeyboardState(val visible: Boolean, val heightPx: Int)

    private val keyboardObservers = mutableListOf<DefaultLifecycleObserver>()

    private fun cleanupKeyboardObservers() {
        keyboardObservers.forEach { lifecycle.removeObserver(it) }
        keyboardObservers.clear()
    }

    open fun showKeyboard(view: View?) {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        } catch (_: Throwable) {
        }
    }

    open fun showKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.root, InputMethodManager.SHOW_IMPLICIT)
        } catch (_: Throwable) {
        }
    }

    open fun hideKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
        } catch (_: Throwable) {
        }
    }

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

    fun View.observeKeyboardState(
        owner: LifecycleOwner,
        onChanged: (KeyboardState) -> Unit
    ): () -> Unit {
        val root = this

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

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    super.onEnd(animation)
                    onChanged(root.currentKeyboardState())
                }
            }

        fun cleanup() {
            ViewCompat.setWindowInsetsAnimationCallback(root, null)
        }

        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                cleanup()
                keyboardObservers.remove(this)
                owner.lifecycle.removeObserver(this)
            }
        }

        keyboardObservers.add(observer)
        owner.lifecycle.addObserver(observer)
        ViewCompat.setWindowInsetsAnimationCallback(root, animCallback)
        root.post { onChanged(root.currentKeyboardState()) }

        return { cleanup() }
    }

    fun View.currentKeyboardState(): KeyboardState {
        val insets = ViewCompat.getRootWindowInsets(this)
            ?: return KeyboardState(visible = false, heightPx = 0)
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val visible = insets.isVisible(WindowInsetsCompat.Type.ime())
        return KeyboardState(visible, imeInsets.bottom)
    }

    fun hideNavigation() {
        windowInsetsController.apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun showNavigation() {
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

    fun hideStatusBar() {
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
    }

    fun showStatusBar() {
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
    }

    fun setLightStatusBar(light: Boolean) {
        windowInsetsController.isAppearanceLightStatusBars = light
    }

    fun setLightNavigationBar(light: Boolean) {
        windowInsetsController.isAppearanceLightNavigationBars = light
    }

    open fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    fun shouldShowDialog(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true
            }
        }
        return false
    }

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

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = createLocale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    private fun createLocale(languageCode: String): Locale {
        val parts = languageCode.split("_")
        return if (parts.size > 1) {
            val region = parts[1].removePrefix("r")
            Locale.Builder()
                .setLanguage(parts[0])
                .setRegion(region)
                .build()
        } else {
            Locale.Builder()
                .setLanguage(parts[0])
                .build()
        }
    }
}
