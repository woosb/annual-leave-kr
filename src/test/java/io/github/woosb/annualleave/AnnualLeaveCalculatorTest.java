package io.github.woosb.annualleave;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 이 테스트가 사실상 명세다. 케이스 하나하나가 조문에 대응한다.
 */
class AnnualLeaveCalculatorTest {

    private static AttendanceRecord record(String hire, String base, int prescribed, int actual) {
        return new AttendanceRecord(
                LocalDate.parse(hire), LocalDate.parse(base), prescribed, actual);
    }

    @Nested
    @DisplayName("제60조 제1항 — 1년 80% 이상 출근 시 15일")
    class Article60Section1 {

        @Test
        @DisplayName("만 1년 도달 + 출근율 100% → 15일")
        void 만1년_출근율100() {
            var result = AnnualLeaveCalculator.calculate(
                    record("2024-03-15", "2025-03-15", 248, 248));

            assertThat(result.totalDays()).isEqualTo(15.0);
            assertThat(result.grants())
                    .singleElement()
                    .extracting(AnnualLeave.Grant::basis)
                    .isEqualTo(LegalBasis.ARTICLE_60_1);
        }

        @ParameterizedTest(name = "출근율 {2}/{1} → 15일 발생 여부 {3}")
        @CsvSource({
                "2025-03-15, 248, 248, true",   // 100%
                "2025-03-15, 248, 199, true",   // 80.2%
                "2025-03-15, 248, 198, false",  // 79.8% - 미달
                "2025-03-14, 248, 248, false",  // 만 1년 하루 전
        })
        void 발생_경계(String base, int prescribed, int actual, boolean granted) {
            var result = AnnualLeaveCalculator.calculate(
                    record("2024-03-15", base, prescribed, actual));

            assertThat(result.totalDays()).isEqualTo(granted ? 15.0 : 0.0);
        }

        @Test
        @DisplayName("결과에 근거 조문이 실려 온다")
        void 근거조문_포함() {
            var result = AnnualLeaveCalculator.calculate(
                    record("2024-03-15", "2025-03-15", 248, 248));

            assertThat(result.grants().get(0).basis().article())
                    .isEqualTo("근로기준법 제60조 제1항");
        }
    }

    @Nested
    @DisplayName("제60조 제2항 — 1년 미만 월차")
    @Disabled("미구현. 이 @Disabled 를 지우고 red 를 확인한 뒤 Article60Section2Rule 을 작성한다")
    class Article60Section2 {

        @ParameterizedTest(name = "{0} 기준 → {1}일")
        @CsvSource({
                "2024-04-15, 1",    // 1개월 개근
                "2024-09-15, 6",    // 6개월
                "2025-02-15, 11",   // 11개월 (1년 미만 최대)
        })
        void 개근_1개월당_1일(String base, double expected) {
            var result = AnnualLeaveCalculator.calculate(
                    record("2024-03-15", base, 248, 248));

            assertThat(result.totalDays()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("제60조 제4항 — 3년 이상 가산")
    @Disabled("미구현. 이 @Disabled 를 지우고 red 를 확인한 뒤 Article60Section4Rule 을 작성한다")
    class Article60Section4 {

        @ParameterizedTest(name = "{0} 기준 → {1}일")
        @CsvSource({
                "2027-01-01, 16",   // 3년차
                "2029-01-01, 17",   // 5년차
                "2045-01-01, 25",   // 상한
        })
        void 매2년_1일_가산_25일_한도(String base, double expected) {
            var result = AnnualLeaveCalculator.calculate(
                    record("2024-01-01", base, 248, 248));

            assertThat(result.totalDays()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("입력 검증")
    class Validation {

        @Test
        void null_입력은_NPE() {
            assertThatThrownBy(() -> AnnualLeaveCalculator.calculate(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void 기준일이_입사일보다_이르면_예외() {
            assertThatThrownBy(() -> record("2024-03-15", "2024-03-14", 248, 248))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("보다 이르다");
        }

        @Test
        void 출근일수가_소정근로일수를_초과하면_예외() {
            assertThatThrownBy(() -> record("2024-03-15", "2025-03-15", 248, 249))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("초과");
        }

        @Test
        void 소정근로일수_0이면_출근율_100퍼센트() {
            assertThat(record("2024-03-15", "2025-03-15", 0, 0).attendanceRate())
                    .isEqualTo(1.0);
        }
    }
}
