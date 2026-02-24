package com.dong.baselib.widget.navigation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.dong.baselib.R

class NavItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var destinationId: Int = 0
        private set
    var icon: Int = 0
        private set
    var iconSelected: Int = 0
        private set
    var title: String = ""
        private set

    init {
        context.obtainStyledAttributes(attrs, R.styleable.NavItem).apply {
            try {
                destinationId = getResourceId(R.styleable.NavItem_destinationId, 0)
                icon = getResourceId(R.styleable.NavItem_nav_icon, 0)
                iconSelected = getResourceId(R.styleable.NavItem_nav_iconSelected, icon)
                title = getString(R.styleable.NavItem_nav_title) ?: ""
            } finally {
                recycle()
            }
        }
        visibility = GONE
    }
}
