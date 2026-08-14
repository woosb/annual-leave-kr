/**
 * 한국 근로기준법 연차유급휴가 계산 라이브러리.
 *
 * <p>{@code internal} 패키지를 export 하지 않으므로, 모듈 경로로 쓰는 소비자는
 * 내부 구현에 접근할 수 없다. 이것이 "여기 만지지 마라" 를 문서가 아니라
 * 컴파일러로 강제하는 유일한 방법이다.
 */
module io.github.woosb.annualleave {
    exports io.github.woosb.annualleave;
    // io.github.woosb.annualleave.internal 은 의도적으로 export 하지 않는다
}
