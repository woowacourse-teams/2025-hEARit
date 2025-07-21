DELETE
FROM hearit_keyword;
DELETE
FROM hearit;
DELETE
FROM keyword;
DELETE
FROM category;

INSERT INTO category (id, name)
VALUES (1, 'Network'),
       (2, 'Android'),
       (3, 'Spring'),
       (4, 'Java');

INSERT INTO keyword (id, name)
VALUES (1, '테코톡'),
       (2, 'HTTP'),
       (3, 'HTTPS'),
       (4, 'Activity'),
       (5, '생명주기'),
       (6, '직렬화'),
       (7, '역직렬화'),
       (8, 'Jackson');

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
        486, '2025-07-18 16:56:03', 3);

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
       (10, 3, 8);