package com.oliver.utils

/**
 * 한국 전화번호를 E.164 형식으로 변환합니다.
 */
fun formatPhoneNumberToE164(phoneNumber: String): String {
    if (phoneNumber.startsWith("82") && phoneNumber.length >= 10) {
        return phoneNumber
    }

    val cleaned = phoneNumber.replace(Regex("[\\s-]"), "")

    return when {
        cleaned.startsWith("010") -> "82${cleaned.substring(1)}"
        cleaned.startsWith("011") -> "82${cleaned.substring(1)}"
        cleaned.startsWith("016") -> "82${cleaned.substring(1)}"
        cleaned.startsWith("017") -> "82${cleaned.substring(1)}"
        cleaned.startsWith("018") -> "82${cleaned.substring(1)}"
        cleaned.startsWith("019") -> "82${cleaned.substring(1)}"
        cleaned.startsWith("0") && cleaned.length == 10 || cleaned.length == 11 -> "82$cleaned"
        else -> cleaned
    }
}