package com.dong.baselib.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.dong.baselib.api.UnitFun0
import com.dong.baselib.api.putExtraSmart
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

abstract class BaseFragment<VB : ViewBinding>(
      private val bindingFactory: (LayoutInflater) -> VB,
      open var isFullSc: Boolean = false,
      open var backPress: Boolean = true
) : Fragment() {
    private var appContext: Context? = null
    private val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }
    var appActivity: AppCompatActivity? = null
        private set
    var fragmentAttach: FragmentAttachEvent? = null
    private var _binding: VB? = null
    val binding: VB
        get() = _binding
            ?: throw IllegalStateException("Binding accessed outside of view lifecycle")

    /** Safe binding access, returns null if not available */
    val bindingOrNull: VB? get() = _binding
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

    fun appContext(): Context =
        context ?: appActivity ?: throw IllegalStateException("Fragment not attached")

    fun appContextOrNull(): Context? = context ?: appActivity
    val isFragmentVisible: Boolean
        get() = isAdded && !isHidden && view != null && isVisible
    val isSafeToUpdateUI: Boolean
        get() = isAdded && !isDetached && view != null && activity?.isFinishing != true

    abstract fun VB.initView()
    abstract fun VB.onClick()

    open fun initialize(context: Context) {}

    open fun backPress() {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            fragmentAttach = context as? FragmentAttachEvent
        }
        catch (e: Exception) {
            Log.e("BaseFragment", "Class cast exception: ${e.message}")
        }
        this.appContext = context
        initialize(context)
        applyLocale(context)
        (activity as? AppCompatActivity)?.let { this.appActivity = it }
    }

    override fun onCreateView(
          inflater: LayoutInflater,
          container: ViewGroup?,
          savedInstanceState: Bundle?,
    ): View? {
        _binding = bindingFactory(inflater)
        if (binding.root.isBackgroundTransparent()) {
            binding.root.setBackgroundColor(Color.WHITE)
        }
        binding.root.isClickable = true
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFullSc) {
            applyStatusBarPadding()
        }

        if (backPress) {
            appActivity?.onBackPressedDispatcher?.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        backPress()
                    }
                })
        }

        binding.initView()
        binding.onClick()

        binding.root.setOnTouchListener { _, ev ->
            handleTouchOutsideEditText(ev)
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainHandler.removeCallbacksAndMessages(null)
        activityResultCallback = null
        binding.root.setOnTouchListener(null)
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        fragmentAttach = null
        appActivity = null
        appContext = null
    }
    // endregion
    // region Arguments - Easy access to fragment arguments
    /** Get String argument with default value */
    fun stringArg(key: String, default: String = ""): String =
        arguments?.getString(key) ?: default

    /** Get Int argument with default value */
    fun intArg(key: String, default: Int = 0): Int =
        arguments?.getInt(key, default) ?: default

    /** Get Long argument with default value */
    fun longArg(key: String, default: Long = 0L): Long =
        arguments?.getLong(key, default) ?: default

    /** Get Boolean argument with default value */
    fun boolArg(key: String, default: Boolean = false): Boolean =
        arguments?.getBoolean(key, default) ?: default

    /** Get Float argument with default value */
    fun floatArg(key: String, default: Float = 0f): Float =
        arguments?.getFloat(key, default) ?: default

    /** Get Double argument with default value */
    fun doubleArg(key: String, default: Double = 0.0): Double =
        arguments?.getDouble(key, default) ?: default

    /** Get Parcelable argument */
    inline fun <reified T : android.os.Parcelable> parcelableArg(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(key)
        }

    /** Get Serializable argument */
    inline fun <reified T : java.io.Serializable> serializableArg(key: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(key) as? T
        }

    /** Get ArrayList String argument */
    fun stringArrayListArg(key: String): ArrayList<String>? =
        arguments?.getStringArrayList(key)

    /** Get ArrayList Int argument */
    fun intArrayListArg(key: String): ArrayList<Int>? =
        arguments?.getIntegerArrayList(key)
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
        val intent = Intent(requireContext(), T::class.java).apply {
            params.takeIf { it.isNotEmpty() }?.forEach { (key, value) ->
                try {
                    putExtraSmart(key, value)
                }
                catch (t: Throwable) {
                    Log.w("launchActivity", "Skip extra \"$key\": ${t.message}")
                }
            }
        }
        startActivity(intent)
    }

    /**
     * Launch activity and finish parent
     */
    inline fun <reified T : Any> launchAndFinish(vararg params: Pair<String, Any?>) {
        launchActivity<T>(*params)
        activity?.finish()
    }

    /**
     * Launch activity and clear task
     */
    inline fun <reified T : Any> launchAndClearTask(vararg params: Pair<String, Any?>) {
        val intent = Intent(requireContext(), T::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            params.forEach { (key, value) ->
                try {
                    putExtraSmart(key, value)
                }
                catch (t: Throwable) {
                    Log.w("launchActivity", "Skip extra \"$key\": ${t.message}")
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
        val intent = Intent(requireActivity(), T::class.java).apply {
            params.takeIf { it.isNotEmpty() }?.forEach { (key, value) ->
                try {
                    putExtraSmart(key, value)
                }
                catch (t: Throwable) {
                    Log.w("launchActivity", "Skip extra \"$key\": ${t.message}")
                }
            }
        }
        resultLauncher.launch(intent)
    }
    // endregion
    // region Coroutines - Lifecycle-aware async operations
    /** Launch coroutine on Main dispatcher */
    fun launchMain(block: suspend CoroutineScope.() -> Unit): Job =
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main, block = block)

    /** Launch coroutine on IO dispatcher */
    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO, block = block)

    /** Launch coroutine on Default dispatcher */
    fun launchDefault(block: suspend CoroutineScope.() -> Unit): Job =
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default, block = block)

    /** Run block after delay on main thread using coroutines */
    fun runDelayed(delayMs: Long, action: () -> Unit): Job = launchMain {
        delay(delayMs)
        if (isSafeToUpdateUI) {
            action()
        }
    }

    suspend fun <T> withMain(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.Main, block = block)

    suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.IO, block = block)

    suspend fun <T> withDefault(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.Default, block = block)

    open fun <T> StateFlow<T>.defaultCollect(
          dispatcher: CoroutineDispatcher = Dispatchers.Default,
          collector: (T) -> Unit
    ): Job = viewLifecycleOwner.lifecycleScope.launch(dispatcher) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { collector(it) }
        }
    }

    open fun <T> StateFlow<T>.IOCollect(
          dispatcher: CoroutineDispatcher = Dispatchers.IO,
          collector: (T) -> Unit
    ): Job = viewLifecycleOwner.lifecycleScope.launch(dispatcher) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { collector(it) }
        }
    }

    open fun <T> StateFlow<T>.mainCollect(
          dispatcher: CoroutineDispatcher = Dispatchers.Main,
          collector: (T) -> Unit
    ): Job = viewLifecycleOwner.lifecycleScope.launch(dispatcher) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { collector(it) }
        }
    }

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

    /** Run block only if fragment is safe to update UI */
    fun runSafe(action: () -> Unit) {
        if (isSafeToUpdateUI) action()
    }

    /** Run block on main thread only if safe to update UI */
    fun runOnMainSafe(action: () -> Unit) {
        runOnMain {
            if (isSafeToUpdateUI) action()
        }
    }
    // endregion
    // region Toast & Snackbar
    /** Show short toast */
    fun toast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    /** Show short toast from string resource */
    fun toast(@StringRes resId: Int) {
        context?.let {
            Toast.makeText(it, resId, Toast.LENGTH_SHORT).show()
        }
    }

    /** Show long toast */
    fun toastLong(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    /** Show long toast from string resource */
    fun toastLong(@StringRes resId: Int) {
        context?.let {
            Toast.makeText(it, resId, Toast.LENGTH_LONG).show()
        }
    }

    /** Show snackbar */
    fun snackbar(
          message: String,
          duration: Int = Snackbar.LENGTH_SHORT,
          actionText: String? = null,
          action: UnitFun0? = null
    ) {
        if (!isSafeToUpdateUI) return
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
        if (!isSafeToUpdateUI) return
        val snackbar = Snackbar.make(binding.root, messageRes, duration)
        if (actionTextRes != null && action != null) {
            snackbar.setAction(actionTextRes) { action() }
        }
        snackbar.show()
    }
    // endregion
    // region Keyboard
    /** Show keyboard for specific view */
    open fun showKeyboard(view: View?) {
        try {
            val act = activity ?: return
            val imm = act.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
        catch (_: Throwable) {
        }
    }

    /** Show keyboard for root view */
    open fun showKeyboard() {
        try {
            val act = activity ?: return
            val imm = act.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.root, InputMethodManager.SHOW_IMPLICIT)
        }
        catch (_: Throwable) {
        }
    }

    /** Hide keyboard */
    open fun hideKeyboard() {
        try {
            val act = activity ?: return
            val imm = act.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(act.window.decorView.rootView.windowToken, 0)
        }
        catch (_: Throwable) {
        }
    }

    private fun handleTouchOutsideEditText(ev: MotionEvent) {
        val focusedView = appActivity?.currentFocus
        if (focusedView is EditText) {
            val rect = Rect()
            focusedView.getGlobalVisibleRect(rect)
            if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                hideKeyboard()
                focusedView.clearFocus()
            }
        }
    }
    // endregion
    // region View Helpers
    /** Apply status bar padding to root view */
    fun applyStatusBarPadding() {
        val paddingTop = binding.root.paddingTop + statusBarHeight
        binding.root.setPadding(
            binding.root.paddingLeft,
            paddingTop,
            binding.root.paddingRight,
            binding.root.paddingBottom
        )
    }

    /** Apply navigation bar padding to root view */
    fun applyNavigationBarPadding() {
        val paddingBottom = binding.root.paddingBottom + navigationBarHeight
        binding.root.setPadding(
            binding.root.paddingLeft,
            binding.root.paddingTop,
            binding.root.paddingRight,
            paddingBottom
        )
    }

    /** Apply both status and navigation bar padding */
    fun applySystemBarsPadding() {
        binding.root.setPadding(
            binding.root.paddingLeft,
            binding.root.paddingTop + statusBarHeight,
            binding.root.paddingRight,
            binding.root.paddingBottom + navigationBarHeight
        )
    }

    private fun View.isBackgroundTransparent(): Boolean {
        val background = this.background
        return background is ColorDrawable && background.color == Color.TRANSPARENT
    }

    /** Find view by id with type inference */
    inline fun <reified T : View> findView(id: Int): T? = view?.findViewById(id)
    // endregion
    // region Locale
    @Suppress("DEPRECATION")
    private fun applyLocale(context: Context): Context {
        val languageCode = LocateManager.getPreLanguage(context)
        val locale = if (languageCode.isNullOrEmpty()) {
            Locale.getDefault()
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
    // region Fragment Result
    /**
     * Set result to parent fragment or activity
     *
     * Example:
     * ```kotlin
     * setFragmentResult("request_key", "result" to "success")
     * ```
     */
    fun setFragmentResult(requestKey: String, vararg params: Pair<String, Any?>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    is android.os.Parcelable -> putParcelable(key, value)
                    is java.io.Serializable -> putSerializable(key, value)
                }
            }
        }
        parentFragmentManager.setFragmentResult(requestKey, bundle)
    }

    /**
     * Listen for fragment result
     *
     * Example:
     * ```kotlin
     * listenFragmentResult("request_key") { bundle ->
     *     val result = bundle.getString("result")
     * }
     * ```
     */
    fun listenFragmentResult(requestKey: String, listener: (Bundle) -> Unit) {
        parentFragmentManager.setFragmentResultListener(
            requestKey,
            viewLifecycleOwner
        ) { _, bundle -> listener(bundle) }
    }
    // endregion
    // region Companion
    companion object {
        /**
         * Create fragment instance with arguments
         *
         * Example:
         * ```kotlin
         * val fragment = DetailFragment.newInstance<DetailFragment>(
         *     "id" to 123,
         *     "title" to "Hello"
         * )
         * ```
         */
        inline fun <reified T : Fragment> newInstance(vararg params: Pair<String, Any?>): T {
            val fragment = T::class.java.getDeclaredConstructor().newInstance()
            fragment.arguments = Bundle().apply {
                params.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Float -> putFloat(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                        is android.os.Parcelable -> putParcelable(key, value)
                        is java.io.Serializable -> putSerializable(key, value)
                        is IntArray -> putIntArray(key, value)
                        is LongArray -> putLongArray(key, value)
                        is FloatArray -> putFloatArray(key, value)
                        is DoubleArray -> putDoubleArray(key, value)
                        is BooleanArray -> putBooleanArray(key, value)
                        is Array<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            if (value.isArrayOf<String>()) {
                                putStringArray(key, value as Array<String>)
                            }
                        }
                        is ArrayList<*> -> {
                            if (value.firstOrNull() is String) {
                                @Suppress("UNCHECKED_CAST")
                                putStringArrayList(key, value as ArrayList<String>)
                            } else if (value.firstOrNull() is Int) {
                                @Suppress("UNCHECKED_CAST")
                                putIntegerArrayList(key, value as ArrayList<Int>)
                            }
                        }
                    }
                }
            }
            return fragment
        }
    }
    // endregion
}
