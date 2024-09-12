package com.example.inapppurchasedemo.premium

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingViewModel(
    context: Context,
//    private val appPrefs: AppPrefs
) : ViewModel(), PurchasesUpdatedListener, ProductDetailsResponseListener {

    private var retriesRemaining = 2
    var acknowledgeCallback: ((Boolean) -> Unit)? = null

    private val _productWithProductDetails = MutableLiveData<Map<String, ProductDetails>>()
    private val _isBillingConnected = MutableLiveData<Boolean>()
    private val _purchases = MutableStateFlow<List<Purchase>?>(null)
    val purchases: Flow<List<Purchase>?> get() = _purchases.asStateFlow()

    fun isBillingConnected(): LiveData<Boolean> = _isBillingConnected

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
//        .enablePendingPurchases()
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    // Establish a connection to Google Play.
    fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases and product details here
                    queryPurchases()
                    queryProductDetails()
                    _isBillingConnected.postValue(true)
                }
            }

            override fun onBillingServiceDisconnected() {
                if (retriesRemaining > 0) {
                    retriesRemaining--
                }
                _isBillingConnected.postValue(false)
            }
        })
    }

    // Query Google Play Billing for existing purchases.
    // New purchases will be provided to PurchasesUpdatedListener.onPurchasesUpdated().
    fun queryPurchases() {
        if (billingClient.isReady) {

            // Query for existing subscription products that have been purchased.
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult, mPurchaseList ->

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    val purchaseList: MutableList<Purchase> = arrayListOf()


                    if (mPurchaseList.isNotEmpty()) {
                        mPurchaseList.forEach {
                            when (it.purchaseState) {
                                Purchase.PurchaseState.PURCHASED -> {
                                    purchaseList.add(it)
                                    if (!it.isAcknowledged) {
                                        acknowledgePurchases(it)
                                    }
                                }

                                Purchase.PurchaseState.PENDING -> {}
                            }
                        }
                        _purchases.value = purchaseList
                        hasActiveSubs.postValue(purchaseList.isNotEmpty())


                    } else {
                        _purchases.value = emptyList()
                        hasActiveSubs.postValue(false)
                    }
                }
            }
        }
    }

    // Query Google Play Billing for products available to sell and present them in the UI
    fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
        val productList = mutableListOf<QueryProductDetailsParams.Product>()
        for (product in LIST_OF_PRODUCTS) {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }
        params.setProductList(productList).let { productDetailsParams ->
            billingClient.queryProductDetailsAsync(productDetailsParams.build(), this)
        }
    }

    // [ProductDetailsResponseListener] implementation
    // Listen to response back from [queryProductDetails] and emits the results
    // to [_productWithProductDetails].
    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: MutableList<ProductDetails>
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d("", debugMessage)
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                var newMap = emptyMap<String, ProductDetails>()
                if (productDetailsList.isNotEmpty()) {
                    newMap = productDetailsList.associateBy { it.productId }
                }
                _productWithProductDetails.postValue(newMap)
            }
        }
    }

    // Launch Purchase flow
    private fun launchBillingFlow(activity: Activity, params: BillingFlowParams) {
        if (billingClient.isReady) {
            billingClient.launchBillingFlow(activity, params)
        }
    }

    // PurchasesUpdatedListener that helps handle new purchases returned from the API
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
            && !purchases.isNullOrEmpty()
        ) {
            _purchases.value = purchases
            // Then, handle the purchases
            for (purchase in purchases) {
                acknowledgePurchases(purchase)
            }
        }
    }

    // Perform new subscription purchases' acknowledgement client side.
    private fun acknowledgePurchases(purchase: Purchase?) {
        purchase?.let {
            if (!it.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(
                    params
                ) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
//                        MyPostAnalytics.myPostAnalytic("premium_purchase_successful")
                        acknowledgeCallback?.invoke(true)
                    }
                }
            }
        }
    }

    // End Billing connection.
    private fun terminateBillingConnection() {
        billingClient.endConnection()
    }

    val hasActiveSubs: MutableLiveData<Boolean> = MutableLiveData()

    // ProductDetails for the basic subscription.
    fun monthlyProductDetails(): LiveData<Map<String, ProductDetails>> = _productWithProductDetails

    fun buySubscription(
        productDetails: ProductDetails,
        activity: Activity,
    ) {
        try {
            val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
            val billingParams = offerToken?.let {
                billingFlowParamsBuilder(
                    productDetails = productDetails,
                    offerToken = it
                )
            }

            if (billingParams != null) {
                launchBillingFlow(
                    activity,
                    billingParams.build()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun billingFlowParamsBuilder(
        productDetails: ProductDetails,
        offerToken: String
    ): BillingFlowParams.Builder {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        )
    }

    fun isLanguageShown(): Boolean {
//        return appPrefs.getBoolean(IS_LANGUAGE_SHOWN)
        return true
    }

    override fun onCleared() {
        terminateBillingConnection()
        super.onCleared()
    }

    companion object {
        // List of subscription product offerings
        const val MONTHLY_SUBSCRIPTION = "month_sub1"
        const val ANNUAL_SUBSCRIPTION = "annual_sub1"

        private val LIST_OF_PRODUCTS = listOf(
            MONTHLY_SUBSCRIPTION,
            ANNUAL_SUBSCRIPTION
        )
    }
}