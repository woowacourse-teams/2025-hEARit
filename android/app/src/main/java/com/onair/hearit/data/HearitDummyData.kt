package com.onair.hearit.data

import android.net.Uri
import com.onair.hearit.R
import com.onair.hearit.domain.HearitShortsItem
import com.onair.hearit.domain.SubtitleLine
import java.time.LocalDateTime

object HearitDummyData {
    fun getShorts(requirePackageName: String): List<HearitShortsItem> {
        val audioUri = Uri.parse("android.resource://$requirePackageName/${R.raw.test_audio3}")
        val audioUri2 = Uri.parse("android.resource://$requirePackageName/${R.raw.test_audio2}")

        return listOf(
            HearitShortsItem(
                id = 1L,
                title = "Intent란?",
                summary = "Intent란 안드로이드의 구성요소 4가지 사이에서 데이터를 전달하거나...(요약)",
                audioUri = audioUri,
                script =
                    listOf(
                        SubtitleLine(0L, 0, "척추 디스크 비수술 치료"),
                        SubtitleLine(1L, 1500, "광화문 자생 한방병원으로 가실 분은"),
                        SubtitleLine(2L, 4000, "서대문역 6번 출구로 나가시기 바랍니다"),
                    ),
                playTime = 1000,
                categoryId = 10,
                createdAt = LocalDateTime.of(2025, 3, 10, 10, 0),
            ),
            HearitShortsItem(
                id = 2L,
                title = "Context란?",
                summary = "Context는 앱 전체의 환경 정보를 나타내며, Activity, Application, Service 등에서 사용돼.",
                audioUri = audioUri2,
                script =
                    listOf(
                        SubtitleLine(0L, 0, "I'm your pookie in the morning"),
                        SubtitleLine(1L, 1500, "You're my pookie in the night"),
                        SubtitleLine(2L, 4000, "너만 보면 Super crazy"),
                        SubtitleLine(3L, 6500, "Oh my 아찔 gets the vibe"),
                        SubtitleLine(4L, 8000, "Don't matter what I do"),
                        SubtitleLine(5L, 11000, "하나 둘 셋 Gimme that cue"),
                        SubtitleLine(6L, 13000, "Cuz I'm your pookie"),
                        SubtitleLine(7L, 14000, "두근두근 Gets the sign"),
                        SubtitleLine(8L, 16000, "That's right"),
                        SubtitleLine(9L, 18000, "내 Fresh new 립스틱 Pick해, 오늘의 color"),
                        SubtitleLine(10L, 21000, "Oh my, 새로 고침 거울 속 느낌 공기도 달라"),
                        SubtitleLine(11L, 25000, "밉지 않지 All I gotta do is blow a kiss"),
                    ),
                playTime = 29000,
                categoryId = 11,
                createdAt = LocalDateTime.of(2025, 3, 12, 10, 0),
            ),
        )
    }
}
