package com.b096.dramarush5.ui.feature

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.b096.dramarush5.R
import com.b096.dramarush5.app.toastShort
import com.b096.dramarush5.databinding.ActivityFeatureBinding
import com.b096.dramarush5.ui.main.MainActivity
import com.b096.dramarush5.utils.color_86909c
import com.b096.dramarush5.utils.grdEnd
import com.b096.dramarush5.utils.grdStart
import com.b096.dramarush5.utils.mainColor
import com.dong.baselib.api.parcelable
import com.dong.baselib.base.BaseActivity
import com.dong.baselib.widget.flexbox.AlignItems
import com.dong.baselib.widget.flexbox.FlexDirection
import com.dong.baselib.widget.flexbox.FlexWrap
import com.dong.baselib.widget.flexbox.FlexboxLayoutManager
import com.dong.baselib.widget.view.textGradient

abstract class FeatureActivity :
    BaseActivity<ActivityFeatureBinding>(ActivityFeatureBinding::inflate) {
    companion object {
        private const val ARG_SCREEN_TYPE = "ARG_SCREEN_TYPE"

        // Shared state between Feature1 and Feature2 activities
        var savedScrollState: Parcelable? = null
        var featureList = listFeatureContent.toMutableList()

        fun start(
            context: Context,
            screenType: FeatureScreenType,
            scrollState: Parcelable? = null
        ) {
            val clazz = when (screenType) {
                is FeatureScreenType.Feature1 -> Feature1Activity::class.java
                is FeatureScreenType.Feature2 -> Feature2Activity::class.java
                is FeatureScreenType.Feature3 -> Feature3Activity::class.java
                is FeatureScreenType.Feature4 -> Feature4Activity::class.java
            }
            savedScrollState = scrollState
            context.startActivity(Intent(context, clazz).apply {
                putExtra(ARG_SCREEN_TYPE, screenType)
            })
        }
    }

    private val screenType by lazy {
        intent.parcelable<FeatureScreenType>(ARG_SCREEN_TYPE) ?: FeatureScreenType.Feature1
    }
    private val featureAdapter = FeatureAdapter { item ->
        toggleItemSelection(item)
        updateContinueButtonState()
        if (screenType is FeatureScreenType.Feature1) {
            navigateToFeature2()
        }
        if (screenType is FeatureScreenType.Feature2) {
            navigateToFeature3()
        }
    }
    private val hasSelection: Boolean
        get() = featureList.any { it.isSelected }

    override fun ActivityFeatureBinding.onClick() = Unit

    override fun backPressed() = finishAffinity()

    @SuppressLint("SetTextI18n")
    override fun ActivityFeatureBinding.setData() {
        tvContentData.text =
            getString(R.string.pick_3_of_the_most_popular_categories).replace("1", "3")
        rcvFeature.adapter = featureAdapter
        rcvFeature.layoutManager = FlexboxLayoutManager(
            this@FeatureActivity,
            FlexDirection.ROW,
            FlexWrap.WRAP
        ).apply {
            alignItems = AlignItems.FLEX_START
        }
        featureAdapter.submitList(featureList.toList())
        restoreScrollState()
        updateContinueButtonState()
    }

    fun countSelect(): Int = featureList.count { it.isSelected }

    override fun initialize() {
        binding.txtContinue.setOnClickListener {
            if (countSelect() < 3) {
                toastShort(getString(R.string.please_select_at_least_category))
                return@setOnClickListener
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun toggleItemSelection(item: FeatureModel) {
        featureList = featureList.map { current ->
            if (current.index == item.index) current.copy(isSelected = !current.isSelected) else current
        }.toMutableList()
        featureAdapter.submitList(featureList.toList())
    }

    private fun restoreScrollState() {
        savedScrollState?.let { binding.rcvFeature.layoutManager?.onRestoreInstanceState(it) }
    }

    private fun updateContinueButtonState() {
        binding.txtContinue.textGradient(
            if (countSelect() > 2) intArrayOf(
                grdStart,
                grdEnd
            ) else intArrayOf(color_86909c, color_86909c)
        )
    }

    private fun navigateToFeature2() {
        val scrollState = binding.rcvFeature.layoutManager?.onSaveInstanceState()
        start(this, FeatureScreenType.Feature2, scrollState)
        overridePendingTransition(0, 0)
        finish()
    }

    private fun navigateToFeature3() {
        val scrollState = binding.rcvFeature.layoutManager?.onSaveInstanceState()
        start(this, FeatureScreenType.Feature3, scrollState)
        overridePendingTransition(0, 0)
        finish()
    }
    private fun navigateToFeature4() {
        val scrollState = binding.rcvFeature.layoutManager?.onSaveInstanceState()
        start(this, FeatureScreenType.Feature4, scrollState)
        overridePendingTransition(0, 0)
        finish()
    }
}
