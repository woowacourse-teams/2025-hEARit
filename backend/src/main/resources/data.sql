INSERT INTO category (id, name)
VALUES (1, '컴퓨터 네트워크');

INSERT INTO hearit (title, summary, play_time, original_audio_url, short_audio_url, script_url, source, created_at, category_id)
VALUES ('웹 세계 문지기 HTTP와 HTTPS 부수기!',
        '웹 통신의 핵심인 HTTP와 HTTPS가 무엇인지 알아봅니다. HTTP의 멱등성, HTTPS가 우리의 개인정보를 어떻게 안전하게 지켜주는지 설명합니다.',
        457,
        '/hearit/audio/original/ORG_7b1774ed-0979-42af-98db-94cf04fba17c.mp3',
        '/hearit/audio/short/SHT_7b1774ed-0979-42af-98db-94cf04fba17c.mp3',
        '/hearit/script/SCR_7b1774ed-0979-42af-98db-94cf04fba17c.json',
        '우아한 테크 테코톡',
        NOW(),
        1);