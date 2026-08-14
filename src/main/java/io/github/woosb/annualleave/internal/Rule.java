package io.github.woosb.annualleave.internal;

import io.github.woosb.annualleave.AnnualLeave;
import io.github.woosb.annualleave.AttendanceRecord;

import java.util.List;

/**
 * 조문 하나에 대응하는 계산 규칙.
 *
 * <p><b>내부 API다.</b> 이 패키지의 타입은 예고 없이 바뀐다.
 * {@code module-info.java} 에서 export 하지 않으므로 모듈 경로 소비자는 접근할 수 없고,
 * 클래스패스 소비자는 접근은 되지만 호환성을 보장하지 않는다.
 *
 * <p>구현체는 조문 하나만 담당한다. 법 개정 시 어느 파일을 고칠지가
 * 조문 번호만으로 바로 결정되도록 하기 위한 구조다.
 */
public interface Rule {

    /**
     * 규칙을 적용해 발생 내역을 만든다.
     *
     * @param record 근태 기록
     * @return 이 규칙으로 발생한 내역. 해당 없으면 빈 리스트.
     */
    List<AnnualLeave.Grant> apply(AttendanceRecord record);
}
