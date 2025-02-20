# Checkout Bridge SDK Integration
Android library that works with both Java and Kotlin. It facilitates seamless integration of Cashfree's payment gateway with `WebView-based checkout pages`. 
The library provides a `CheckoutJsBridge` class, which acts as a bridge between your WebView and JavaScript-based payment flows


## Why Use This Library?
Merchants integrating the Cashfree Web checkout using the `Cashfree JS SDK` often face issues where the UPI intent option is not visible on the checkout page when opened in a WebView. 
This library resolves that issue by enabling UPI intent functionality. Additionally, it provides the benefit of OTP auto-read and submit functionality without requiring any additional code.
Even if merchants have integrated Cashfree on their website and are opening their website in their mobile browser or WebView, this library will still work to enable UPI intent and OTP auto-read features seamlessly.

## Prerequisites
1. Android SDK version 21 or above
2. Internet permission in AndroidManifest.xml:
3. WebView must have JavaScript enabled

## Installation
Add the following dependency in your `build.gradle` file:

```groovy
dependencies {
 implementation 'com.cashfree.pg:checkout-bridge:1.0.0'
}
```

## Integration Steps
With this library, merchants only need to initialize CheckoutJsBridge by passing the activity context.
When opening the Cashfree checkout page, they should pass their WebView object reference to the CheckoutJsBridge class.

```kotlin
private var paymentJsBridge: CheckoutJsBridge? = null
fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    paymentJsBridge = CheckoutJsBridge(this)
}
```

Before loading the payment page, set the bridge by calling `setJsBridge`. Pass the WebView reference and `JsMetaData` object:

```kotlin
paymentJsBridge?.setJsBridge(
binding.paymentWebview,
JsMetaData(addUpiJS = true, addOtpJS = true)
)

```

Always reset and release CheckoutJsBridge in `onDestroy()` to avoid memory leaks:

```kotlin
fun onDestroy() {
    super.onDestroy()
    paymentJsBridge?.resetData()
    paymentJsBridge = null
}
```


## Additional Notes

1. Ensure the WebView is configured correctly with JavaScript and DOM storage enabled.
2. `setJsBridge` should be called before the payment page starts interacting with the JavaScript bridge.
3. The library is optimized to work with Cashfree’s Web checkout flows.





## License
<pre>
SDK is licensed under the MIT License.
See the LICENSE file distributed with this work for additional
information regarding copyright ownership.

Except as contained in the LICENSE file, the name(s) of the above copyright
holders shall not be used in advertising or otherwise to promote the sale,
use or other dealings in this Software without prior written authorization.
</pre>
