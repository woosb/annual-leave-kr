package io.github.woosb.annualleave.internal;

import io.github.woosb.annualleave.AnnualLeave;
import io.github.woosb.annualleave.AttendanceRecord;
import io.github.woosb.annualleave.LegalBasis;

import java.time.LocalDate;
import java.util.List;

/**
 * 근로기준법 제60조 제1항 — 1년간 80% 이상 출근한 근로자에게 15일.
 *
 * <p>발생 시점은 입사 후 만 1년이 되는 날이다. 즉 2024-03-15 입사자는
 * 2025-03-15 에 15일이 발생한다.
 */
public final class Article60Section1Rule implements Rule {

    private static final double BASE_DAYS = 15.0;
    private static final double REQUIRED_RATE = 0.8;

    @Override
    public List<AnnualLeave.Grant> apply(AttendanceRecord record) {
        LocalDate firstAnniversary = record.hireDate().plusYears(1);

        // 아직 만 1년 미달 → 제60조 제2항(월차) 영역
        if (record.baseDate().isBefore(firstAnniversary)) {
            return List.of();
        }
        // 출근율 미달 → 제60조 제2항으로 처리
        if (record.attendanceRate() < REQUIRED_RATE) {
            return List.of();
        }

        return List.of(new AnnualLeave.Grant(
                BASE_DAYS,
                LegalBasis.ARTICLE_60_1,
                "%s 발생 (출근율 %.1f%%)".formatted(
                        firstAnniversary, record.attendanceRate() * 100)));
    }
}
