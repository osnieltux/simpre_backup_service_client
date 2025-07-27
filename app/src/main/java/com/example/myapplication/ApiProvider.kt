// ApiProvider.kt
package com.example.myapplication

object ApiProvider {
    lateinit var api: QuotesApi
        private set

    var isInitialized = false
        private set

    fun initialize(baseUrl: String) {
        RetrofitHelper.setBaseUrl(baseUrl)
        api = RetrofitHelper.getInstance().create(QuotesApi::class.java)
        isInitialized = true
    }
}
