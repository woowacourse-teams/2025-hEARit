package com.onair.hearit.data.dummy

import com.onair.hearit.domain.SearchedItem

object SearchedHearitDummyData {
    val searchedItems =
        listOf(
            SearchedItem(
                title = "Intent란 무엇인가?",
                time = 3000,
                summary = "Intent란 안드로이드의 구성요소 4가지 사이에서 데이터를 전달하거나 컴포넌트를 실행할 때 사용하는 메시지 객체입니다.",
            ),
            SearchedItem(
                title = "Activity 생명주기",
                time = 5000,
                summary = "Activity는 onCreate부터 onDestroy까지의 생명주기를 가지며, 사용자의 상호작용과 상태 유지에 중요한 역할을 합니다.",
            ),
            SearchedItem(
                title = "Fragment와의 차이점",
                time = 4000,
                summary = "Fragment는 Activity 내에서 재사용 가능한 UI 단위로, 유연한 레이아웃 구성이 가능합니다.",
            ),
            SearchedItem(
                title = "RecyclerView 사용법",
                time = 6000,
                summary = "RecyclerView는 대량의 데이터를 효율적으로 리스트 형태로 보여주기 위해 사용되는 ViewGroup입니다.",
            ),
            SearchedItem(
                title = "ViewModel이란?",
                time = 3000,
                summary = "ViewModel은 UI 데이터를 저장하고 관리하여 화면 회전 시에도 데이터가 유지되도록 도와줍니다.",
            ),
            SearchedItem(
                title = "LiveData로 상태 관리",
                time = 4500,
                summary = "LiveData는 관찰 가능한 데이터 홀더 클래스로, 데이터 변경 시 자동으로 UI에 반영됩니다.",
            ),
            SearchedItem(
                title = "Coroutine의 기본 개념",
                time = 5500,
                summary = "Coroutine은 비동기 처리를 간단하고 안전하게 작성할 수 있도록 도와주는 Kotlin의 기능입니다.",
            ),
            SearchedItem(
                title = "Navigation Component",
                time = 6500,
                summary = "Navigation Component는 Fragment 간의 이동과 백스택 관리를 쉽게 도와주는 Jetpack 라이브러리입니다.",
            ),
            SearchedItem(
                title = "Dependency Injection 이란?",
                time = 4800,
                summary = "DI는 객체 간의 의존성을 외부에서 주입하여 코드의 유연성과 테스트 용이성을 높이는 패턴입니다.",
            ),
            SearchedItem(
                title = "Jetpack Compose 개요",
                time = 7000,
                summary = "Jetpack Compose는 선언형 UI 툴킷으로, XML 없이 UI를 Kotlin 코드만으로 작성할 수 있습니다.",
            ),
        )
}
