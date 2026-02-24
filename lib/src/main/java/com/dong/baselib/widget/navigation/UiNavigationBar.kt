package com.dong.baselib.widget.navigation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.core.graphics.toColorInt
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.dong.baselib.R
import com.dong.baselib.base.animateNavigate
import com.dong.baselib.widget.layout.UiLinearLayout
import kotlin.math.min

class UiNavigationBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : UiLinearLayout(context, attrs, defStyleAttr) {
    // Enums
    enum class IndicatorStyle { NONE, BOTTOM_BAR, BACKGROUND, PILL }
    enum class LabelVisibility { ALWAYS, SELECTED, NEVER }
    enum class ItemLayout { VERTICAL, HORIZONTAL }
    enum class AnimationType { NONE, SCALE, FADE, SLIDE, BOUNCE }
    enum class GradientOrientation { HORIZONTAL, VERTICAL }

    // Data class
    data class NavItemData(
            val id: Int,
            val title: CharSequence,
            val icon: Int,
            val iconSelected: Int = icon
    )

    // Collections
    private val navItems = mutableListOf<NavItemData>()
    private val itemViews = mutableListOf<View>()
    private val pendingNavItems = mutableListOf<NavItem>()
    private val badgeCounts = mutableMapOf<Int, Int>()

    // State
    private var currentSelectedIndex = 0
    private var navGraphResId = 0

    // Indicator config
    private var indicatorStyle = IndicatorStyle.BOTTOM_BAR
    private var indicatorColor = "#CE41FF".toColorInt()
    private var indicatorWidth: Float? = null  // null means auto (50% of item width)
    private var indicatorWidthFraction: Float = 0.5f  // default 50%
    private var indicatorHeight = dp(3f)
    private var indicatorRadius = dp(2f)
    private var indicatorAnimated = true
    private var indicatorAnimDuration = 250

    // Indicator gradient
    private var indicatorGradientEnabled = false
    private var indicatorGradientStart = "#CE41FF".toColorInt()
    private var indicatorGradientCenter = Color.TRANSPARENT
    private var indicatorGradientEnd = "#60EFFF".toColorInt()
    private var indicatorGradientOrientation = GradientOrientation.HORIZONTAL

    // Text colors
    private var textColorSelected = "#CE41FF".toColorInt()
    private var textColorUnselected = "#86909C".toColorInt()
    private var labelFontFamily: android.graphics.Typeface? = null

    // Text gradient
    private var textGradientEnabled = false
    private var textGradientStart = "#CE41FF".toColorInt()
    private var textGradientCenter = Color.TRANSPARENT
    private var textGradientEnd = "#FF6B6B".toColorInt()
    private var textGradientOrientation = GradientOrientation.HORIZONTAL

    // Icon tint
    private var iconTintSelected: Int? = null
    private var iconTintUnselected: Int? = null

    // Sizes
    private var textSize = sp(12f)
    private var iconSize = dp(24f)
    private var itemPadding = dp(8f)
    private var iconTextSpacing = dp(4f)

    // Layout
    private var itemLayout = ItemLayout.VERTICAL
    private var labelVisibility = LabelVisibility.ALWAYS

    // Animation
    private var animationType = AnimationType.SCALE
    private var animationDuration = 200

    // Navigation Animation
    private var navAnimateEnabled = false
    private var navEnterAnim = R.anim.fade_in
    private var navExitAnim = R.anim.fade_out
    private var navPopEnterAnim = R.anim.fade_in
    private var navPopExitAnim = R.anim.fade_out
    private var navAnimOptions: NavOptions? = null

    // Badge
    private var badgeColor = Color.RED
    private var badgeTextColor = Color.WHITE

    // Ripple
    private var rippleEnabled = true
    private var rippleColor = "#20CE41FF".toColorInt()

    // Paint & animation
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var indicatorLeft = 0f
    private var indicatorAnimator: ValueAnimator? = null

    fun getNavItemList(): List<NavItemData> = navItems

    // Listeners
    private var onItemSelectedListener: ((NavItemData, Int) -> Unit)? = null
    private var onItemReselectedListener: ((NavItemData, Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setWillNotDraw(false)

        context.obtainStyledAttributes(attrs, R.styleable.UiNavigationBar).apply {
            try {
                // NavGraph
                navGraphResId = getResourceId(R.styleable.UiNavigationBar_navGraph, 0)
                // Indicator
                indicatorStyle = when (getInt(R.styleable.UiNavigationBar_nav_indicatorStyle, 1)) {
                    0 -> IndicatorStyle.NONE
                    1 -> IndicatorStyle.BOTTOM_BAR
                    2 -> IndicatorStyle.BACKGROUND
                    3 -> IndicatorStyle.PILL
                    else -> IndicatorStyle.BOTTOM_BAR
                }
                indicatorColor =
                    getColor(R.styleable.UiNavigationBar_nav_indicatorColor, indicatorColor)
                // Parse indicator width - can be dimension or fraction
                if (hasValue(R.styleable.UiNavigationBar_nav_indicatorWidth)) {
                    val typeValue = peekValue(R.styleable.UiNavigationBar_nav_indicatorWidth)
                    if (typeValue != null && typeValue.type == android.util.TypedValue.TYPE_FRACTION) {
                        indicatorWidthFraction =
                            getFraction(R.styleable.UiNavigationBar_nav_indicatorWidth, 1, 1, 0.5f)
                        indicatorWidth = null
                    } else {
                        indicatorWidth =
                            getDimension(R.styleable.UiNavigationBar_nav_indicatorWidth, 0f)
                        if (indicatorWidth == 0f) indicatorWidth = null
                    }
                }

                indicatorHeight =
                    getDimension(R.styleable.UiNavigationBar_nav_indicatorHeight, indicatorHeight)
                indicatorRadius =
                    getDimension(R.styleable.UiNavigationBar_nav_indicatorRadius, indicatorRadius)
                indicatorAnimated =
                    getBoolean(R.styleable.UiNavigationBar_nav_indicatorAnimated, indicatorAnimated)
                indicatorAnimDuration = getInt(
                    R.styleable.UiNavigationBar_nav_indicatorAnimationDuration,
                    indicatorAnimDuration
                )
                // Indicator gradient
                indicatorGradientEnabled = getBoolean(
                    R.styleable.UiNavigationBar_nav_indicatorGradient,
                    indicatorGradientEnabled
                )
                indicatorGradientStart = getColor(
                    R.styleable.UiNavigationBar_nav_indicatorGradientStart,
                    indicatorGradientStart
                )
                indicatorGradientCenter = getColor(
                    R.styleable.UiNavigationBar_nav_indicatorGradientCenter,
                    indicatorGradientCenter
                )
                indicatorGradientEnd = getColor(
                    R.styleable.UiNavigationBar_nav_indicatorGradientEnd,
                    indicatorGradientEnd
                )
                indicatorGradientOrientation =
                    when (getInt(R.styleable.UiNavigationBar_nav_indicatorGradientOrientation, 0)) {
                        0 -> GradientOrientation.HORIZONTAL
                        1 -> GradientOrientation.VERTICAL
                        else -> GradientOrientation.HORIZONTAL
                    }
                // Text colors
                textColorSelected =
                    getColor(R.styleable.UiNavigationBar_nav_textColorSelected, textColorSelected)
                textColorUnselected = getColor(
                    R.styleable.UiNavigationBar_nav_textColorUnselected,
                    textColorUnselected
                )
                // Label font family
                if (hasValue(R.styleable.UiNavigationBar_nav_labelFontFamily)) {
                    val fontResId =
                        getResourceId(R.styleable.UiNavigationBar_nav_labelFontFamily, 0)
                    if (fontResId != 0) {
                        try {
                            labelFontFamily = androidx.core.content.res.ResourcesCompat.getFont(
                                context,
                                fontResId
                            )
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                // Text gradient
                textGradientEnabled =
                    getBoolean(R.styleable.UiNavigationBar_nav_textGradient, textGradientEnabled)
                textGradientStart =
                    getColor(R.styleable.UiNavigationBar_nav_textGradientStart, textGradientStart)
                textGradientCenter =
                    getColor(R.styleable.UiNavigationBar_nav_textGradientCenter, textGradientCenter)
                textGradientEnd =
                    getColor(R.styleable.UiNavigationBar_nav_textGradientEnd, textGradientEnd)
                textGradientOrientation =
                    when (getInt(R.styleable.UiNavigationBar_nav_textGradientOrientation, 0)) {
                        0 -> GradientOrientation.HORIZONTAL
                        1 -> GradientOrientation.VERTICAL
                        else -> GradientOrientation.HORIZONTAL
                    }
                // Icon tint
                if (hasValue(R.styleable.UiNavigationBar_nav_iconTintSelected)) {
                    iconTintSelected = getColor(
                        R.styleable.UiNavigationBar_nav_iconTintSelected,
                        textColorSelected
                    )
                }
                if (hasValue(R.styleable.UiNavigationBar_nav_iconTintUnselected)) {
                    iconTintUnselected = getColor(
                        R.styleable.UiNavigationBar_nav_iconTintUnselected,
                        textColorUnselected
                    )
                }
                // Sizes
                textSize = getDimension(R.styleable.UiNavigationBar_nav_textSize, textSize)
                iconSize = getDimension(R.styleable.UiNavigationBar_nav_iconSize, iconSize)
                itemPadding = getDimension(R.styleable.UiNavigationBar_nav_itemPadding, itemPadding)
                iconTextSpacing =
                    getDimension(R.styleable.UiNavigationBar_nav_iconTextSpacing, iconTextSpacing)
                // Layout
                itemLayout = when (getInt(R.styleable.UiNavigationBar_nav_itemLayout, 0)) {
                    0 -> ItemLayout.VERTICAL
                    1 -> ItemLayout.HORIZONTAL
                    else -> ItemLayout.VERTICAL
                }
                labelVisibility =
                    when (getInt(R.styleable.UiNavigationBar_nav_labelVisibility, 0)) {
                        0 -> LabelVisibility.ALWAYS
                        1 -> LabelVisibility.SELECTED
                        2 -> LabelVisibility.NEVER
                        else -> LabelVisibility.ALWAYS
                    }
                // Animation
                animationType = when (getInt(R.styleable.UiNavigationBar_nav_animationType, 1)) {
                    0 -> AnimationType.NONE
                    1 -> AnimationType.SCALE
                    2 -> AnimationType.FADE
                    3 -> AnimationType.SLIDE
                    4 -> AnimationType.BOUNCE
                    else -> AnimationType.SCALE
                }
                animationDuration =
                    getInt(R.styleable.UiNavigationBar_nav_animationDuration, animationDuration)
                // Badge
                badgeColor = getColor(R.styleable.UiNavigationBar_nav_badgeColor, badgeColor)
                badgeTextColor =
                    getColor(R.styleable.UiNavigationBar_nav_badgeTextColor, badgeTextColor)
                // Ripple
                rippleEnabled =
                    getBoolean(R.styleable.UiNavigationBar_nav_rippleEnabled, rippleEnabled)
                rippleColor = getColor(R.styleable.UiNavigationBar_nav_rippleColor, rippleColor)
                // Navigation Animation
                navAnimateEnabled =
                    getBoolean(R.styleable.UiNavigationBar_nav_animateEnabled, navAnimateEnabled)
                if (hasValue(R.styleable.UiNavigationBar_nav_enterAnim)) {
                    navEnterAnim = getResourceId(R.styleable.UiNavigationBar_nav_enterAnim, navEnterAnim)
                }
                if (hasValue(R.styleable.UiNavigationBar_nav_exitAnim)) {
                    navExitAnim = getResourceId(R.styleable.UiNavigationBar_nav_exitAnim, navExitAnim)
                }
                if (hasValue(R.styleable.UiNavigationBar_nav_popEnterAnim)) {
                    navPopEnterAnim = getResourceId(R.styleable.UiNavigationBar_nav_popEnterAnim, navPopEnterAnim)
                }
                if (hasValue(R.styleable.UiNavigationBar_nav_popExitAnim)) {
                    navPopExitAnim = getResourceId(R.styleable.UiNavigationBar_nav_popExitAnim, navPopExitAnim)
                }
            }
            finally {
                recycle()
            }
        }

        indicatorPaint.color = indicatorColor
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is NavItem) {
                pendingNavItems.add(child)
            }
        }
        pendingNavItems.forEach { removeView(it) }

        if (pendingNavItems.isNotEmpty()) {
            buildFromNavItems()
        }
    }

    private fun buildFromNavItems() {
        navItems.clear()
        itemViews.clear()

        pendingNavItems.forEach { navItem ->
            navItems.add(
                NavItemData(
                    id = navItem.destinationId,
                    title = navItem.title,
                    icon = navItem.icon,
                    iconSelected = navItem.iconSelected
                )
            )
        }

        buildItemViews()
    }

    fun setItems(items: List<NavItemData>) {
        navItems.clear()
        navItems.addAll(items)
        removeAllViews()
        itemViews.clear()
        buildItemViews()
    }

    private fun buildItemViews() {
        removeAllViews()
        itemViews.clear()

        navItems.forEachIndexed { index, item ->
            val itemView = createItemView(item, index)
            val params = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            addView(itemView, params)
            itemViews.add(itemView)
        }
        updateSelection(currentSelectedIndex, animate = false)
    }

    private fun createItemView(item: NavItemData, index: Int): View {
        return LinearLayout(context).apply {
            orientation = if (itemLayout == ItemLayout.VERTICAL) VERTICAL else HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(
                itemPadding.toInt(),
                itemPadding.toInt(),
                itemPadding.toInt(),
                itemPadding.toInt()
            )
            // Ripple effect
            if (rippleEnabled) {
                val rippleDrawable = android.graphics.drawable.RippleDrawable(
                    ColorStateList.valueOf(rippleColor),
                    null,
                    Color.WHITE.toDrawable()
                )
                background = rippleDrawable
            }
            // Icon
            val iconView = ImageView(context).apply {
                layoutParams = LayoutParams(iconSize.toInt(), iconSize.toInt())
                if (item.icon != 0) {
                    setImageResource(item.icon)
                }
                tag = "icon"
            }
            addView(iconView)
            // Label
            if (labelVisibility != LabelVisibility.NEVER) {
                val textView = TextView(context).apply {
                    val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    if (itemLayout == ItemLayout.VERTICAL) {
                        lp.topMargin = iconTextSpacing.toInt()
                    } else {
                        lp.leftMargin = iconTextSpacing.toInt()
                    }
                    layoutParams = lp
                    text = item.title
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSize)
                    gravity = Gravity.CENTER
                    maxLines = 1
                    tag = "label$index"
                    // Apply font family
                    labelFontFamily?.let { typeface = it }
                }
                addView(textView)
            }

            setOnClickListener {
                if (currentSelectedIndex == index) {
                    onItemReselectedListener?.invoke(item, index)
                    playReselectedAnimation(this)
                } else {
                    selectItem(index)
                }
            }
        }
    }

    fun selectItem(index: Int, triggerListener: Boolean = true) {
        if (index < 0 || index >= navItems.size) return
        val previousIndex = currentSelectedIndex
        currentSelectedIndex = index
        updateSelection(index, animate = true, previousIndex = previousIndex)
        if (triggerListener && previousIndex != index) {
            onItemSelectedListener?.invoke(navItems[index], index)
        }
    }

    fun selectItemById(@IdRes itemId: Int, triggerListener: Boolean = true) {
        val index = navItems.indexOfFirst { it.id == itemId }
        if (index >= 0) {
            selectItem(index, triggerListener)
        }
    }

    private fun updateSelection(selectedIndex: Int, animate: Boolean, previousIndex: Int = -1) {
        itemViews.forEachIndexed { index, view ->
            val isSelected = index == selectedIndex
            val wasSelected = index == previousIndex
            val item = navItems.getOrNull(index) ?: return@forEachIndexed
            val container = view as? LinearLayout ?: return@forEachIndexed
            val iconView = container.findViewWithTag<ImageView>("icon")
            val labelView = container.findViewWithTag<TextView>("label")
            // Animate selection change
            if (animate && (isSelected || wasSelected)) {
                playSelectionAnimation(container, isSelected)
            }
            // Update icon
            iconView?.apply {
                val iconRes =
                    if (isSelected && item.iconSelected != 0) item.iconSelected else item.icon
                if (iconRes != 0) setImageResource(iconRes)
                val tint = if (isSelected) {
                    iconTintSelected ?: textColorSelected
                } else {
                    iconTintUnselected ?: textColorUnselected
                }
                setColorFilter(tint, PorterDuff.Mode.SRC_IN)
            }
            // Update label
            labelView?.apply {
                if (isSelected && textGradientEnabled) {
                    applyTextGradient(this)
                } else {
                    paint.shader = null
                    setTextColor(if (isSelected) textColorSelected else textColorUnselected)
                }

                visibility = when (labelVisibility) {
                    LabelVisibility.ALWAYS -> View.VISIBLE
                    LabelVisibility.SELECTED -> if (isSelected) View.VISIBLE else View.INVISIBLE
                    LabelVisibility.NEVER -> View.GONE
                }
            }
        }
        // Animate indicator
        if (indicatorStyle != IndicatorStyle.NONE && itemViews.isNotEmpty()) {
            post { animateIndicator(selectedIndex, animate && indicatorAnimated) }
        }
    }

    private fun applyTextGradient(textView: TextView) {
        textView.post {
            val width = textView.paint.measureText(textView.text.toString())
            val height = textView.textSize
            val colors = if (textGradientCenter != Color.TRANSPARENT) {
                intArrayOf(textGradientStart, textGradientCenter, textGradientEnd)
            } else {
                intArrayOf(textGradientStart, textGradientEnd)
            }
            val shader = when (textGradientOrientation) {
                GradientOrientation.HORIZONTAL -> LinearGradient(
                    0f, 0f, width, 0f, colors, null, Shader.TileMode.CLAMP
                )
                GradientOrientation.VERTICAL -> LinearGradient(
                    0f, 0f, 0f, height, colors, null, Shader.TileMode.CLAMP
                )
            }
            textView.paint.shader = shader
            textView.invalidate()
        }
    }

    private fun playSelectionAnimation(view: View, selected: Boolean) {
        when (animationType) {
            AnimationType.NONE -> {}
            AnimationType.SCALE -> {
                val scale = if (selected) 1.1f else 1.0f
                view.animate()
                    .scaleX(scale).scaleY(scale)
                    .setDuration(animationDuration.toLong())
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        if (selected) {
                            view.animate().scaleX(1f).scaleY(1f)
                                .setDuration((animationDuration / 2).toLong())
                                .start()
                        }
                    }
                    .start()
            }
            AnimationType.FADE -> {
                val alpha = if (selected) 1f else 0.6f
                view.animate()
                    .alpha(alpha)
                    .setDuration(animationDuration.toLong())
                    .start()
            }
            AnimationType.SLIDE -> {
                if (selected) {
                    view.translationY = dp(10f)
                    view.animate()
                        .translationY(0f)
                        .setDuration(animationDuration.toLong())
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()
                }
            }
            AnimationType.BOUNCE -> {
                if (selected) {
                    view.scaleX = 0.8f
                    view.scaleY = 0.8f
                    view.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(animationDuration.toLong())
                        .setInterpolator(OvershootInterpolator(2f))
                        .start()
                }
            }
        }
    }

    private fun playReselectedAnimation(view: View) {
        // Quick pulse animation on reselect
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1.1f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun animateIndicator(toIndex: Int, animate: Boolean) {
        if (itemViews.isEmpty() || toIndex >= itemViews.size) return
        val targetView = itemViews[toIndex]
        val targetLeft = targetView.left.toFloat()

        if (!animate) {
            indicatorLeft = targetLeft
            invalidate()
            return
        }

        indicatorAnimator?.cancel()
        indicatorAnimator = ValueAnimator.ofFloat(indicatorLeft, targetLeft).apply {
            duration = indicatorAnimDuration.toLong()
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                indicatorLeft = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed && itemViews.isNotEmpty() && currentSelectedIndex < itemViews.size) {
            indicatorLeft = itemViews[currentSelectedIndex].left.toFloat()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        // Let UiLinearLayout handle background drawing
        super.dispatchDraw(canvas)
        // Draw indicator on top
        if (indicatorStyle != IndicatorStyle.NONE && itemViews.isNotEmpty() && currentSelectedIndex < itemViews.size) {
            drawIndicator(canvas)
        }
    }

    private fun drawIndicator(canvas: Canvas) {
        val itemView = itemViews.getOrNull(currentSelectedIndex) ?: return
        val itemWidth = itemView.width.toFloat()

        when (indicatorStyle) {
            IndicatorStyle.BOTTOM_BAR -> {
                // Calculate indicator width: use fixed width if set, otherwise use fraction
                val calcIndicatorWidth = indicatorWidth ?: (itemWidth * indicatorWidthFraction)
                val left = indicatorLeft + (itemWidth - calcIndicatorWidth) / 2
                val top = height - indicatorHeight
                val rect = RectF(left, top, left + calcIndicatorWidth, height.toFloat())
                // Apply gradient if enabled
                if (indicatorGradientEnabled) {
                    applyIndicatorGradient(left, top, left + calcIndicatorWidth, height.toFloat())
                } else {
                    indicatorPaint.shader = null
                    indicatorPaint.color = indicatorColor
                }

                canvas.drawRoundRect(rect, indicatorRadius, indicatorRadius, indicatorPaint)
            }
            IndicatorStyle.BACKGROUND -> {
                val padding = dp(8f)
                val left = indicatorLeft + padding
                val top = padding
                val right = indicatorLeft + itemWidth - padding
                val bottom = height - padding
                val rect = RectF(left, top, right, bottom)
                // Apply gradient if enabled
                if (indicatorGradientEnabled) {
                    applyIndicatorGradient(left, top, right, bottom)
                    indicatorPaint.alpha = 30
                } else {
                    indicatorPaint.shader = null
                    indicatorPaint.color = indicatorColor
                    indicatorPaint.alpha = 30
                }

                canvas.drawRoundRect(rect, indicatorRadius, indicatorRadius, indicatorPaint)
                indicatorPaint.alpha = 255
            }
            IndicatorStyle.PILL -> {
                val padding = dp(12f)
                val left = indicatorLeft + padding
                val pillHeight = min(height - padding * 2, dp(60f))
                val top = (height - pillHeight) / 2
                val right = indicatorLeft + itemWidth - padding
                val bottom = top + pillHeight
                val rect = RectF(left, top, right, bottom)
                // Apply gradient if enabled
                if (indicatorGradientEnabled) {
                    applyIndicatorGradient(left, top, right, bottom)
                    indicatorPaint.alpha = 40
                } else {
                    indicatorPaint.shader = null
                    indicatorPaint.color = indicatorColor
                    indicatorPaint.alpha = 40
                }

                canvas.drawRoundRect(rect, pillHeight / 2, pillHeight / 2, indicatorPaint)
                indicatorPaint.alpha = 255
            }
            IndicatorStyle.NONE -> {}
        }
    }

    private fun applyIndicatorGradient(left: Float, top: Float, right: Float, bottom: Float) {
        val colors = if (indicatorGradientCenter != Color.TRANSPARENT) {
            intArrayOf(indicatorGradientStart, indicatorGradientCenter, indicatorGradientEnd)
        } else {
            intArrayOf(indicatorGradientStart, indicatorGradientEnd)
        }
        val shader = when (indicatorGradientOrientation) {
            GradientOrientation.HORIZONTAL -> LinearGradient(
                left, 0f, right, 0f, colors, null, Shader.TileMode.CLAMP
            )
            GradientOrientation.VERTICAL -> LinearGradient(
                0f, top, 0f, bottom, colors, null, Shader.TileMode.CLAMP
            )
        }
        indicatorPaint.shader = shader
    }

    // Public API
    fun setOnItemSelectedListener(listener: (NavItemData, Int) -> Unit) {
        onItemSelectedListener = listener
    }

    fun setOnItemReselectedListener(listener: (NavItemData, Int) -> Unit) {
        onItemReselectedListener = listener
    }

    // Build NavOptions based on current animation settings
    private fun buildNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(navEnterAnim)
            .setExitAnim(navExitAnim)
            .setPopEnterAnim(navPopEnterAnim)
            .setPopExitAnim(navPopExitAnim)
            .build()
    }

    // Set custom navigation animations
    fun setNavAnimations(
            @AnimRes enterAnim: Int = navEnterAnim,
            @AnimRes exitAnim: Int = navExitAnim,
            @AnimRes popEnterAnim: Int = navPopEnterAnim,
            @AnimRes popExitAnim: Int = navPopExitAnim
    ) {
        this.navEnterAnim = enterAnim
        this.navExitAnim = exitAnim
        this.navPopEnterAnim = popEnterAnim
        this.navPopExitAnim = popExitAnim
        navAnimOptions = null // Reset cached options to rebuild
    }

    // Enable or disable navigation animations
    fun setNavAnimateEnabled(enabled: Boolean) {
        navAnimateEnabled = enabled
        navAnimOptions = null // Reset cached options
    }

    fun setupWithNavController(navController: NavController) {
        if (navGraphResId != 0 && navItems.isEmpty()) {
            loadFromNavGraph(navController)
        }

        setOnItemSelectedListener { item, _ ->
            if (item.id != 0) {
                val options = if (navAnimateEnabled) {
                    if (navAnimOptions == null) {
                        navAnimOptions = buildNavOptions()
                    }
                    navAnimOptions
                } else {
                    null
                }
                navController.navigate(item.id, null, options)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val index = navItems.indexOfFirst { it.id == destination.id }
            if (index >= 0 && currentSelectedIndex != index) {
                currentSelectedIndex = index
                updateSelection(index, animate = true)
            }
        }
    }

    private fun loadFromNavGraph(navController: NavController) {
        try {
            val navGraph = navController.navInflater.inflate(navGraphResId)
            navItems.clear()

            navGraph.iterator().forEach { destination ->
                val matchingNavItem = pendingNavItems.find { it.destinationId == destination.id }
                if (matchingNavItem != null) {
                    navItems.add(
                        NavItemData(
                            id = destination.id,
                            title = matchingNavItem.title.ifEmpty {
                                destination.label?.toString() ?: ""
                            },
                            icon = matchingNavItem.icon,
                            iconSelected = matchingNavItem.iconSelected
                        )
                    )
                }
            }

            if (navItems.isNotEmpty()) {
                buildItemViews()
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Badge API
    fun setBadge(itemId: Int, count: Int) {
        badgeCounts[itemId] = count
        invalidate()
    }

    fun removeBadge(itemId: Int) {
        badgeCounts.remove(itemId)
        invalidate()
    }

    // Style setters
    fun setIndicatorStyle(style: IndicatorStyle) {
        indicatorStyle = style
        invalidate()
    }

    /**
     * Set indicator width as a fixed dimension in pixels
     */
    fun setIndicatorWidth(widthPx: Float) {
        indicatorWidth = widthPx
        invalidate()
    }

    /**
     * Set indicator width as a fraction/percentage of item width (0.0 to 1.0)
     * Example: 0.5f = 50% of item width
     */
    fun setIndicatorWidthFraction(fraction: Float) {
        indicatorWidth = null
        indicatorWidthFraction = fraction.coerceIn(0.1f, 1.0f)
        invalidate()
    }

    /**
     * Set indicator height in pixels
     */
    fun setIndicatorHeight(heightPx: Float) {
        indicatorHeight = heightPx
        invalidate()
    }

    /**
     * Set indicator corner radius in pixels
     */
    fun setIndicatorRadius(radiusPx: Float) {
        indicatorRadius = radiusPx
        invalidate()
    }

    fun setAnimationType(type: AnimationType) {
        animationType = type
    }

    fun setItemLayout(layout: ItemLayout) {
        itemLayout = layout
        buildItemViews()
    }

    fun setTextGradient(
            enabled: Boolean,
            start: Int = textGradientStart,
            end: Int = textGradientEnd
    ) {
        textGradientEnabled = enabled
        textGradientStart = start
        textGradientEnd = end
        updateSelection(currentSelectedIndex, animate = false)
    }

    fun setLabelFontFamily(typeface: android.graphics.Typeface?) {
        labelFontFamily = typeface
        // Update existing labels
        itemViews.forEach { view ->
            val container = view as? LinearLayout
            container?.findViewWithTag<TextView>("label")?.typeface = typeface
        }
    }

    fun setLabelFontFamily(fontResId: Int) {
        try {
            labelFontFamily = androidx.core.content.res.ResourcesCompat.getFont(context, fontResId)
            // Update existing labels
            itemViews.forEach { view ->
                val container = view as? LinearLayout
                container?.findViewWithTag<TextView>("label")?.typeface = labelFontFamily
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setIndicatorGradient(
            enabled: Boolean,
            start: Int = indicatorGradientStart,
            center: Int = Color.TRANSPARENT,
            end: Int = indicatorGradientEnd,
            orientation: GradientOrientation = GradientOrientation.HORIZONTAL
    ) {
        indicatorGradientEnabled = enabled
        indicatorGradientStart = start
        indicatorGradientCenter = center
        indicatorGradientEnd = end
        indicatorGradientOrientation = orientation
        invalidate()
    }

    fun getSelectedItemId(): Int = navItems.getOrNull(currentSelectedIndex)?.id ?: 0
    fun getSelectedIndex(): Int = currentSelectedIndex

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
    private fun sp(value: Float): Float = value * resources.displayMetrics.scaledDensity
}
