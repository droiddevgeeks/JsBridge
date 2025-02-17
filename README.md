# payment-checkout

## Steps to Use & integrate
1. Init `CheckoutJsBridge` class & pass Activity context. Please init this in onCreate of activity.
2. When you are loading your payment page, then call `setJsBridge` method. It accepts WebView reference object & `JsMetaData` object.

## Usage
```kotlin
private var paymentJsBridge: CheckoutJsBridge? = null

paymentJsBridge = CheckoutJsBridge(this)

paymentJsBridge?.setJsBridge(
    binding.paymentWebview,
    JsMetaData(true, true)
)
```




## License
<pre>
SDK is licensed under the MIT License.
See the LICENSE file distributed with this work for additional
information regarding copyright ownership.

Except as contained in the LICENSE file, the name(s) of the above copyright
holders shall not be used in advertising or otherwise to promote the sale,
use or other dealings in this Software without prior written authorization.
</pre>
