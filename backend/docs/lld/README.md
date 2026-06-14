# LLD — 저수준 설계

> LLD = 모듈·컴포넌트 수준 설계("왜 이 클래스/구조로 나눴나"). 파일은 `docs/lld/<주제>.md`.

## 언제 쓰나

- 단순 CRUD 가 아닌 **비자명한 설계 판단**이 있을 때만. (예: 동시성·배치·외부연동·전략 분기)
- 작은 변경은 SPEC 만으로 충분 — LLD 는 큰 변경에 선행.
- **소급 작성 금지.** 기존 복잡 기능은 *다음에 손볼 때* LLD 로 정형화한다.

## LLD 후보 (현재 코드에 존재하는 비자명 설계 — 변경 시 작성 권장)

- 재생기록 버퍼+배치 저장 (`playinghistory.infrastructure.buffer/scheduler`, ShedLock·Circuit Breaker fallback)
- 숏캐스트 탐색 스코어링 (`explore.application.scorefactor/scoreprocessor`)
- AI 콘텐츠 파이프라인 (`admin/ai`: STT→전사→교정→메타생성→클리핑→S3)

> 다이어그램은 Mermaid 사용(이미지 전용 금지).
