package io.github.woosb.annualleave;

/**
 * 각 휴가 발생분의 법적 근거.
 *
 * <p>계산 결과에 근거 조문을 함께 실어 보내는 것이 이 라이브러리의 설계 의도다.
 * 인사담당자·근로자에게 "왜 이 일수인가" 를 설명할 수 있어야 실무에서 쓸 수 있다.
 *
 * @since 0.1.0
 */
public enum LegalBasis {

    /** 근로기준법 제60조 제1항 — 1년간 80% 이상 출근 시 15일. */
    ARTICLE_60_1("근로기준법 제60조 제1항", "1년간 80퍼센트 이상 출근한 근로자에게 15일의 유급휴가"),

    /** 근로기준법 제60조 제2항 — 1년 미만이거나 80% 미만 출근 시 1개월 개근당 1일. */
    ARTICLE_60_2("근로기준법 제60조 제2항", "계속근로 1년 미만 또는 80퍼센트 미만 출근 시 1개월 개근에 1일"),

    /** 근로기준법 제60조 제4항 — 3년 이상 계속근로 시 매 2년마다 1일 가산, 총 25일 한도. */
    ARTICLE_60_4("근로기준법 제60조 제4항", "3년 이상 계속근로 시 최초 1년을 초과하는 매 2년에 1일 가산");

    private final String article;
    private final String summary;

    LegalBasis(String article, String summary) {
        this.article = article;
        this.summary = summary;
    }

    /** @return 조문 표기. 예: {@code "근로기준법 제60조 제1항"} */
    public String article() {
        return article;
    }

    /** @return 조문 요지 */
    public String summary() {
        return summary;
    }
}
