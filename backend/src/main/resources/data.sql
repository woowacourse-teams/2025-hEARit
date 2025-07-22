DELETE
FROM hearit_keyword;
DELETE
FROM hearit;
DELETE
FROM keyword;
DELETE
FROM category;

INSERT INTO category (id, name, color_code)
VALUES (1, 'Network', '#9900A3E4'),
       (2, 'Android', '#99A4C639'),
       (3, 'Spring', '#996DB33F'),
       (4, 'Java', '#99E14A3A'),
       (5, 'Kotlin', '#998A48FA');

INSERT INTO keyword (id, name)
VALUES (1, '테코톡'),
       (2, 'HTTP'),
       (3, 'HTTPS'),
       (4, 'Activity'),
       (5, '생명주기'),
       (6, '직렬화'),
       (7, '역직렬화'),
       (8, 'Jackson'),
       (9, 'Record'),
       (10, '객체'),
       (11, '다형성'),
       (12, '컬렉션'),
       (13, 'Stream'),
       (14, 'AI'),
       (15, 'XR'),
       (16, 'Coroutines'),
       (17, 'Flow');

INSERT INTO hearit (id, title, original_audio_url, short_audio_url, script_url, source, summary, play_time, created_at,
                    category_id)
VALUES (1, '사나, 수양의 웹 세계 문지기 HTTP와 HTTPS 부수기!', '/hearit/audio/original/ORG_7b1774ed-0979-42af-98db-94cf04fba17c.mp3',
        '/hearit/audio/short/SHT_7b1774ed-0979-42af-98db-94cf04fba17c.mp3',
        '/hearit/script/SCR_7b1774ed-0979-42af-98db-94cf04fba17c.json', '우아한 테크 - 테코톡',
        '웹 통신의 핵심인 HTTP와 HTTPS가 무엇인지 알아봅니다. HTTP의 멱등성, HTTPS가 우리의 개인정보를 어떻게 안전하게 지켜주는지 설명합니다.', 457,
        '2025-07-16 14:39:15', 1),
       (2, '비비의 Activity LifeCycle', '/hearit/audio/original/ORG_39181115-ae25-460a-809e-e974cb74ce78.mp3',
        '/hearit/audio/short/SHT_39181115-ae25-460a-809e-e974cb74ce78.mp3',
        '/hearit/script/SCR_39181115-ae25-460a-809e-e974cb74ce78.json', '우아한 테크 - 테코톡',
        '안드로이드 4대 컴포넌트 중 하나인 Activity의 생성부터 소멸까지의 흐름, 즉 Activity LifeCycle을 자세히 알아봅니다.', 451, '2025-07-17 16:55:25', 2),
       (3, '슬링키의 직렬화, 역직렬화 in Spring', '/hearit/audio/original/ORG_43740a20-a6bb-4815-9aae-5cf8f33c07eb.mp3',
        '/hearit/audio/short/SHR_43740a20-a6bb-4815-9aae-5cf8f33c07eb.mp3',
        '/hearit/script/SCR_43740a20-a6bb-4815-9aae-5cf8f33c07eb.json', '우아한 테크 - 테코톡',
        'Spring의 @RequestBody, @ResponseBody 뒤에서 동작하는 Jackson의 ObjectMapper와 HttpMessageConverter를 알아봅니다. 리플렉션과 어노테이션이 언제, 어떻게 동작하는지도 함께 살펴봅니다.',
        486, '2025-07-18 16:56:03', 3),
       (4, '대니의 Record 그렇게 쓰는거 아닌데', '/hearit/audio/original/ORG_4c209f08-bad4-4a0f-8082-fdfddbdd4245.mp3',
        '/hearit/audio/short/SHR_4c209f08-bad4-4a0f-8082-fdfddbdd4245.mp3',
        '/hearit/script/SCR_4c209f08-bad4-4a0f-8082-fdfddbdd4245.json', '우아한 테크 - 테코톡',
        '자바 레코드 잘 쓰고 계신가요? 자바의 레코드 기능이 불변 객체를 얼마나 쉽고 간결하게 만들 수 있는지 알아봅니다.', 457, '2025-07-19 01:48:08', 4),
       (5, '모다의 자바 Object 알고쓰기', '/hearit/audio/original/ORG_8c5140c0-88cc-4b7a-8f15-50a7032e876a.mp3',
        '/hearit/audio/short/SHR_8c5140c0-88cc-4b7a-8f15-50a7032e876a.mp3',
        '/hearit/script/SCR_8c5140c0-88cc-4b7a-8f15-50a7032e876a.json', '우아한 테크 - 테코톡',
        '자바의 모든 클래스 조상인 \'오브젝트 클래스\'에 대해 깊이 파고드는 시간입니다. equals, hashCode, toString 등 주요 메서드의 역할과 중요성을 통해 자바 언어의 철학을 이해해 보세요.',
        586, '2025-07-20 01:48:08', 4),
       (6, '모코의 Stream API', '/hearit/audio/original/ORG_9737b5fe-9ef7-4b91-94cc-2572d1549a52.mp3',
        '/hearit/audio/short/SHR_9737b5fe-9ef7-4b91-94cc-2572d1549a52.mp3',
        '/hearit/script/SCR_9737b5fe-9ef7-4b91-94cc-2572d1549a52.json', '우아한 테크 - 테코톡',
        '자바 스트림 API는 왜 사용하는 걸까요? 이 에피소드에서는 스트림의 간결한 선언형 프로그래밍 방식과 내부 동작 원리인 수직적 연산, 지연 처리 등 핵심 개념을 깊이 있게 다룹니다. 스트림의 진짜 장점과 주의할 점을 이해하여 코드 가독성과 효율성을 높이는 방법을 알아보세요.',
        606, '2025-07-21 01:48:08', 4),
       (7, '코드만 짜는 게 아니다, 행동한다 – Gemini 탑재된 Android Studio',
        '/hearit/audio/original/ORG_ebb6822f-77f3-43a5-8b3e-9eb6c66b3eda.mp3',
        '/hearit/audio/short/SHR_ebb6822f-77f3-43a5-8b3e-9eb6c66b3eda.mp3',
        '/hearit/script/SCR_ebb6822f-77f3-43a5-8b3e-9eb6c66b3eda.json', '조이',
        'Google의 최신 개발 도구 소식인 Android Studio Narwhal Feature Drop 2025.1.2 RC1에 대해 알아봅니다. 7월 18일 베타 채널에 공개된 이 버전은 정식 출시 직전 버전이며, 안드로이드 개발 환경을 어떻게 변화시킬지 핵심 기능을 중심으로 설명해 드립니다.',
        450, '2025-07-21 20:48:08', 2),
       (8, '코틀린 Flow - Flow 연산자', '/hearit/audio/original/ORG_26ee593b-3852-4b42-b266-e84e98e311b4.mp3',
        '/hearit/audio/short/SHR_26ee593b-3852-4b42-b266-e84e98e311b4.mp3',
        '/hearit/script/SCR_26ee593b-3852-4b42-b266-e84e98e311b4.json', '미플',
        'Kotlin Flow의 연산자에 대해 알아봅니다. 중간 연산자와 최종 연산자로 나뉘어 유연한 데이터 처리 파이프라인에 대해 설명합니다.', 404, '2025-07-21 20:17:08', 5);

INSERT INTO hearit_keyword (id, hearit_id, keyword_id)
VALUES (1, 1, 1),
       (2, 1, 2),
       (3, 1, 3),
       (4, 2, 1),
       (5, 2, 4),
       (6, 2, 5),
       (7, 3, 1),
       (8, 3, 6),
       (9, 3, 7),
       (10, 3, 8),
       (11, 4, 1),
       (12, 4, 9),
       (13, 4, 10),
       (14, 5, 1),
       (15, 5, 10),
       (16, 5, 11),
       (17, 6, 1),
       (18, 6, 12),
       (19, 6, 13),
       (20, 7, 14),
       (21, 7, 15),
       (22, 8, 16),
       (23, 8, 17);