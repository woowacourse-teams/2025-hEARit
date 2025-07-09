package com.onair.hearit.integration;

import com.onair.hearit.dto.request.HearitCreationRequest;
import com.onair.hearit.dto.response.HearitCreationResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HearitCreationTest {

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUpPort() {
        RestAssured.port = port;
    }

    @DisplayName("사용자 입력 텍스트로 팟캐스트를 생성한다.")
    @Test
    void createHearit() {
        HearitCreationRequest request = new HearitCreationRequest("""
                     자바 30주년 - 기술을 바꾼 코드의 천재, 제임스 고슬링 인터뷰 (thenewstack.io)
                28P by GN⁺ 2달전 | ★ favorite | 댓글 13개
                제임스 고슬링은 자바의 창시자이자, 30년간 현대 컴퓨팅에 영향을 끼친 실용적 천재로 평가됨
                가난한 환경에서 쓰레기 더미 속 부품으로 컴퓨터를 조립하며 프로그래밍을 배웠고, 이 자기주도적 학습은 이후 언어 설계 철학에도 반영
                Sun Microsystems에서 장난과 혁신이 공존했던 시절은 고슬링 특유의 창의성과 기술 문화 조성의 기반이 됨
                최근 그는 생성형 AI 도구와 AI 붐에 대해 강한 회의감을 드러냈으며, 프로그래밍 교육의 중요성은 오히려 커졌다고 강조
                자바의 생존 비결은 화려함이 아닌 안정성, 하위호환성, 개발자 생산성을 철저히 지향한 실용주의적 설계 철학 덕분이었음
                Java at 30: The Genius Behind the Code That Changed Tech
                자바는 5월 23일로 30주년을 맞이하는 범용 고수준 객체지향 언어로, 오늘날에도 다양한 규모의 시스템을 구동하는 핵심 기술임
                자바가 존재할 수 있었던 근본에는 제임스 고슬링의 실용적 기술 감각과 창조적 직관력이 자리함
                고슬링은 쓰레기통에서 부품을 모아 컴퓨터를 조립하던 캐나다 출신의 자립심 강한 십대에서 출발해 세계적 프로그래머로 성장함
                ‘한 번 작성하면 어디서나 실행된다’는 철학은 자바의 상징이며, 이는 곧 소프트웨어 개발 방식에 근본적 전환을 이끈 언어 철학으로 이어짐
                고슬링은 커리어 내내 기술적 탁월성과 장난기, 그리고 뚜렷한 윤리 의식을 조화시키며 현대 컴퓨팅 문화 형성에 지속적 영향을 준 개발자상을 구현함
                James Gosling: The Brilliant Mind Behind Java
                제임스 고슬링은 단순한 ‘자바의 아버지’가 아니라, 복잡한 개념을 직관적으로 설명할 줄 아는 겸손한 천재임
                자바를 만든 지 30년이 지난 지금, 그는 기술 여정을 되돌아보며 언어와 개발 문화의 진화 과정을 되짚는 시간을 가짐
                The Path To Programming: Resourceful Beginnings
                어린 시절 극도로 가난한 환경 속에서 고슬링은 쓰레기통에서 텔레비전을 주워 기술적 창의력을 키운 경험을 가짐
                첫 컴퓨터는 전화국의 폐기 릴레이 랙으로 조립했으며, 이는 이른 시기부터 드러난 기계적 감각과 조립 능력을 상징함
                캘거리 대학의 컴퓨터 센터를 방문하며 화면, 깜빡이는 불빛, 테이프 장치 등에 매료되어 프로그래밍에 대한 평생의 호기심이 시작됨
                그는 펀치카드를 뒤져 비밀번호를 얻는 방식으로 독학했으며, 고등학생 시절 대학 물리학과에서 위성 데이터 분석 프로그램을 만들어 돈을 받으며 프로그래밍을 즐긴 경험을 축적함
                그의 초기 프로그래밍 경험은 IBM 메인프레임의 PL/1, Fortran, PDP-8 어셈블리어, CDC 6400 코드에 걸쳐 있음. 특유의 절제된 어조로 "여름엔 COBOL 컴파일러를 개발하는 아르바이트를 했다"고 가볍게 언급했는데, 이는 많은 노련한 프로그래머들이 감당하기 어려워하는 작업이었음
                Academia to Industry: Finding His Way
                고슬링은 학계를 “대학원생을 값싼 인건비로 활용하는 연구소”라고 표현하며, 이론보다 실용을 중시하는 직설적 관점을 드러냄
                카네기멜론 박사과정 중에도 스타트업에서 근무하며 현실적 경험을 쌓고, 이후 학위 과정을 마치는 산업과 학계를 병행한 유연한 진로 선택을 실현함
                첫 직장은 IBM 리서치였지만, “자기 발을 쏘는 데 헌신적인 회사”라는 평가를 남기며 기업 운영과 기술 전략에 대한 냉철한 분석 태도를 유지함
                이러한 초기 경험들은 이후 Sun Microsystems에서의 활동 방식에 영향을 준 현실 중심의 조직문화 이해 기반이 됨
                """);

        HearitCreationResponse response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/hearit")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(HearitCreationResponse.class);

        System.out.println(response.title());
        System.out.println(response.script());
    }
}
