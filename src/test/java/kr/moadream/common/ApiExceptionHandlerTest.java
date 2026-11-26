package kr.moadream.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = ApiExceptionHandlerTest.ThrowingController.class)
@Import(ApiExceptionHandlerTest.ThrowingController.class)
class ApiExceptionHandlerTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void 비즈니스_오류는_직접_넣은_code를_내린다() {
        assertProblem(mvc.get().uri("/test/business").exchange(), 409, "FUNDING_CLOSED");
    }

    @Test
    void 처리하지_못한_예외는_500_ProblemDetail로_내린다() {
        assertProblem(mvc.get().uri("/test/unexpected").exchange(), 500, "INTERNAL_SERVER_ERROR");
    }

    @Test
    void 없는_경로도_ProblemDetail로_내린다() {
        assertProblem(mvc.get().uri("/test/none").exchange(), 404, "NOT_FOUND");
    }

    private static void assertProblem(MvcTestResult result, int status, String code) {
        assertThat(result).hasStatus(status)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.code").isEqualTo(code);
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/business")
        void business() {
            ErrorResponseException e = new ErrorResponseException(HttpStatus.CONFLICT);
            e.getBody().setProperty("code", "FUNDING_CLOSED");
            throw e;
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("boom");
        }
    }
}
