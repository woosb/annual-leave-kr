package io.github.woosb.annualleave;

import io.github.woosb.annualleave.internal.Rule;
import io.github.woosb.annualleave.internal.Rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 연차유급휴가 계산 진입점.
 *
 * <p>상태가 없고 스레드 안전하다. 인스턴스를 만들 필요 없이 정적 메서드로 쓴다.
 *
 * <pre>{@code
 * var record = new AttendanceRecord(
 *         LocalDate.of(2024, 3, 15),   // 입사일
 *         LocalDate.of(2026, 8, 13),   // 기준일
 *         248,                          // 소정근로일수
 *         240);                         // 출근일수
 *
 * AnnualLeave leave = AnnualLeaveCalculator.calculate(record);
 * leave.totalDays();   // 15.0
 * leave.grants();      // [Grant[days=15.0, basis=ARTICLE_60_1, ...]]
 * }</pre>
 *
 * <p><b>현재 지원 범위 (0.1.0)</b> — 입사일 기준 산정만 지원한다.
 * 회계연도 기준 산정은 이후 버전에서 다룬다.
 *
 * @since 0.1.0
 */
public final class AnnualLeaveCalculator {

    private AnnualLeaveCalculator() {
        throw new AssertionError("인스턴스를 만들 수 없다");
    }

    /**
     * 기준일 시점에 발생한 연차를 계산한다.
     *
     * @param record 근태 기록
     * @return 계산 결과. 발생분이 없으면 총 0일이고 내역은 비어 있다.
     * @throws NullPointerException {@code record} 가 null 인 경우
     */
    public static AnnualLeave calculate(AttendanceRecord record) {
        Objects.requireNonNull(record, "record 는 null 일 수 없다");

        List<AnnualLeave.Grant> grants = new ArrayList<>();
        for (Rule rule : Rules.all()) {
            grants.addAll(rule.apply(record));
        }
        return AnnualLeave.of(grants);
    }
}
