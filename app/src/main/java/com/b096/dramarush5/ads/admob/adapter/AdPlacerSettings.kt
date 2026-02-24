package com.b096.dramarush5.ads.admob.adapter

class AdPlacerSettings(
    private var listAdUnit: List<String>,
    private var layoutCustomAd: Int,
    private var layoutAdPlaceHolder: Int,
) {

    private var positionFixAd = -1
    private var isRepeatingAd = false
    private var startRepeatingAd = -1

    fun setFixedPosition(positionAd: Int) {
        positionFixAd = positionAd
        isRepeatingAd = false
    }

    fun setRepeatingInterval(positionAd: Int, startRepeatingAd: Int = -1) {
        this.startRepeatingAd = startRepeatingAd - 1
        positionFixAd = positionAd - 1
        isRepeatingAd = true
    }

    fun getStartRepeatingAd(): Int {
        return startRepeatingAd
    }

    fun getListAdUnitId(): List<String> {
        return listAdUnit
    }

    fun setListAdUnitId(adUnitId: List<String>) {
        this.listAdUnit = adUnitId
    }

    fun getPositionFixAd(): Int {
        return positionFixAd
    }

    fun isRepeatingAd(): Boolean {
        return isRepeatingAd
    }

    fun getLayoutCustomAd(): Int {
        return layoutCustomAd
    }

    fun getLayoutAdPlaceHolder(): Int {
        return layoutAdPlaceHolder
    }

    fun setLayoutAdPlaceHolder(layoutAdPlaceHolder: Int) {
        this.layoutAdPlaceHolder = layoutAdPlaceHolder
    }

    fun setLayoutCustomAd(layoutCustomAd: Int) {
        this.layoutCustomAd = layoutCustomAd
    }
}
