package com.oliver.utils

/**
 * 한국 전화번호를 E.164 형식으로 변환합니다.
 * 예: 01012345678 -> 821012345678
 */
fun formatPhoneNumberToE164(phoneNumber: String): String {
    // 이미 E.164 형식인 경우 (82로 시작하고 10자리 이상)
    if (phoneNumber.startsWith("82") && phoneNumber.length >= 10) {
        return phoneNumber
    }

    val cleaned = phoneNumber.replace(Regex("[\\s-]"), "")

    return when {
        cleaned.startsWith("010") -> "82${cleaned.substring(1)}" // 01012345678 -> 821012345678
        cleaned.startsWith("011") -> "82${cleaned.substring(1)}" // 01112345678 -> 821112345678
        cleaned.startsWith("016") -> "82${cleaned.substring(1)}" // 01612345678 -> 821612345678
        cleaned.startsWith("017") -> "82${cleaned.substring(1)}" // 01712345678 -> 821712345678
        cleaned.startsWith("018") -> "82${cleaned.substring(1)}" // 01812345678 -> 821812345678
        cleaned.startsWith("019") -> "82${cleaned.substring(1)}" // 01912345678 -> 821912345678
        cleaned.startsWith("0") && cleaned.length == 10 || cleaned.length == 11 -> "82$cleaned" // 02xxxxyyyy -> 8202xxxxyyyy
        else -> cleaned // 이미 올바른 형식이거나 다른 형식인 경우 그대로 반환
    }
}