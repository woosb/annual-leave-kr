package io.github.woosb.annualleave;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 연차 산정에 필요한 최소 입력.
 *
 * <p>출근율을 단순 {@code double} 로 받지 않는 이유는 근로기준법상 출근율이
 * "소정근로일수 대비 출근일수" 로 정의되고, 소정근로일수 자체가 육아휴직·업무상 부상 등
 * 법정 사유에 따라 조정되기 때문이다. 분자와 분모를 그대로 보존해야
 * 그 조정 규칙을 나중에 라이브러리 안에서 처리할 수 있다.
 *
 * @param hireDate                입사일
 * @param baseDate                산정 기준일. 이 날짜 시점에 보유한 연차를 계산한다.
 * @param prescribedWorkingDays   산정 대상 기간의 소정근로일수
 * @param actualAttendanceDays    그중 실제 출근일수
 *
 * @since 0.1.0
 */
public record AttendanceRecord(
        LocalDate hireDate,
        LocalDate baseDate,
        int prescribedWorkingDays,
        int actualAttendanceDays
) {

    public AttendanceRecord {
        Objects.requireNonNull(hireDate, "hireDate 는 null 일 수 없다");
        Objects.requireNonNull(baseDate, "baseDate 는 null 일 수 없다");

        if (baseDate.isBefore(hireDate)) {
            throw new IllegalArgumentException(
                    "baseDate(%s) 가 hireDate(%s) 보다 이르다".formatted(baseDate, hireDate));
        }
        if (prescribedWorkingDays < 0) {
            throw new IllegalArgumentException(
                    "prescribedWorkingDays 는 음수일 수 없다: " + prescribedWorkingDays);
        }
        if (actualAttendanceDays < 0) {
            throw new IllegalArgumentException(
                    "actualAttendanceDays 는 음수일 수 없다: " + actualAttendanceDays);
        }
        if (actualAttendanceDays > prescribedWorkingDays) {
            throw new IllegalArgumentException(
                    "출근일수(%d)가 소정근로일수(%d)를 초과한다"
                            .formatted(actualAttendanceDays, prescribedWorkingDays));
        }
    }

    /**
     * 소정근로일수가 0 이면 출근율을 정의할 수 없으므로 1.0 으로 본다.
     * (전 기간이 법정 제외 사유로 소진된 경우 — 판례상 불이익하게 취급하지 않는다)
     *
     * @return 0.0 이상 1.0 이하의 출근율
     */
    public double attendanceRate() {
        if (prescribedWorkingDays == 0) {
            return 1.0;
        }
        return (double) actualAttendanceDays / prescribedWorkingDays;
    }
}
