package com.b096.dramarush5.ui.feature

import android.os.Parcelable
import androidx.annotation.StringRes
import com.b096.dramarush5.R
import kotlinx.parcelize.Parcelize

data class FeatureModel(
    val index: Int = 0,
    @StringRes val label: Int = 0,
    val isSelected: Boolean = false,
)

val listFeatureContent = listOf(
    FeatureModel(1, R.string.feature_content_item_1, false),
    FeatureModel(2, R.string.feature_content_item_2, false),
    FeatureModel(3, R.string.feature_content_item_3, false),
    FeatureModel(4, R.string.feature_content_item_4, false),
    FeatureModel(5, R.string.feature_content_item_5, false),
    FeatureModel(6, R.string.feature_content_item_6, false),
)

sealed class FeatureScreenType : Parcelable {
    @Parcelize
    data object Feature1 : FeatureScreenType()

    @Parcelize
    data object Feature2 : FeatureScreenType()
    @Parcelize
    data object Feature3 : FeatureScreenType()
    @Parcelize
    data object Feature4 : FeatureScreenType()
}
