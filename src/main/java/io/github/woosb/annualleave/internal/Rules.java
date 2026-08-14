package io.github.woosb.annualleave.internal;

import java.util.List;

/**
 * 규칙 목록. 새 조문을 구현하면 여기에 등록한다.
 *
 * <p><b>내부 API다.</b> 직접 참조하지 말 것.
 */
public final class Rules {

    private static final List<Rule> ALL = List.of(
            new Article60Section1Rule()
            // TODO 0.1.0: Article60Section2Rule  — 1년 미만 월차
            // TODO 0.1.0: Article60Section4Rule  — 3년 이상 가산 (25일 한도)
    );

    private Rules() {
        throw new AssertionError("인스턴스를 만들 수 없다");
    }

    /** @return 적용 순서대로 정렬된 규칙 목록 */
    public static List<Rule> all() {
        return ALL;
    }
}
