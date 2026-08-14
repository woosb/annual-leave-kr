package io.github.woosb.annualleave;

import java.util.List;
import java.util.Objects;

/**
 * 연차 계산 결과.
 *
 * <p>총 일수만이 아니라 발생 내역({@link Grant})을 함께 담는다.
 * 총계만 반환하면 검증도 설명도 불가능하고, 결국 소비자가 계산을 다시 짜게 된다.
 *
 * @since 0.1.0
 */
public final class AnnualLeave {

    private final double totalDays;
    private final List<Grant> grants;

    private AnnualLeave(double totalDays, List<Grant> grants) {
        this.totalDays = totalDays;
        this.grants = List.copyOf(grants);
    }

    /**
     * 발생 내역으로부터 결과를 만든다. 총 일수는 내역의 합으로 자동 계산된다.
     *
     * @param grants 발생 내역. 비어 있을 수 있다(연차 미발생).
     * @return 계산 결과
     */
    static AnnualLeave of(List<Grant> grants) {
        Objects.requireNonNull(grants, "grants 는 null 일 수 없다");
        double sum = grants.stream().mapToDouble(Grant::days).sum();
        return new AnnualLeave(sum, grants);
    }

    /** @return 총 연차 일수 */
    public double totalDays() {
        return totalDays;
    }

    /** @return 발생 내역. 수정 불가능한 리스트. */
    public List<Grant> grants() {
        return grants;
    }

    /**
     * 개별 발생 내역 한 건.
     *
     * @param days        발생 일수
     * @param basis       근거 조문
     * @param description 사람이 읽을 설명. 예: {@code "3년차 가산 (2024-03-15 발생)"}
     */
    public record Grant(double days, LegalBasis basis, String description) {

        public Grant {
            Objects.requireNonNull(basis, "basis 는 null 일 수 없다");
            Objects.requireNonNull(description, "description 은 null 일 수 없다");
            if (days < 0) {
                throw new IllegalArgumentException("days 는 음수일 수 없다: " + days);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnnualLeave other)) return false;
        return Double.compare(totalDays, other.totalDays) == 0
                && grants.equals(other.grants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalDays, grants);
    }

    @Override
    public String toString() {
        return "AnnualLeave[totalDays=%s, grants=%d건]".formatted(totalDays, grants.size());
    }
}
