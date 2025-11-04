package com.oliver.utils

import java.util.Date

fun makeEmergencyScript(
    location: String,
    detectionTime: Date
): String {
    return "AI 화재 신고입니다. 화재가 발생했습니다. 위치는 $location 입니다. 화재 감지 시간은 ${localizationTime(detectionTime)} 입니다. 즉시 출동 바랍니다. 다시 한번 말씀 해드리겠습니다. 화재가 발생했습니다. 위치는 $location 입니다. 화재 감지 시간은 ${
        localizationTime(
            detectionTime
        )
    } 입니다."
}