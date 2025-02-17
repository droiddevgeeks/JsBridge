package com.checkout.bridge

internal sealed class Modes {
    internal data object OTP : Modes()
    internal data object UPI : Modes()
}

data class JsMetaData(
    val addUpiJS: Boolean = true,
    val addOtpJS: Boolean = false
)