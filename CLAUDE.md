# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트

한국 근로기준법 제60조 연차유급휴가 계산 라이브러리. 의존성 0, Java 17 바이트코드, 상태 없음.
0.1.0 개발 중이며 아직 Maven Central 에 배포되지 않았다.

## 빌드 / 테스트

```bash
./gradlew build          # 컴파일 + 테스트 + check (checkNoRuntimeDependencies 포함)
./gradlew test
./gradlew publishToMavenLocal
```

단일 테스트 실행:

```bash
./gradlew test --tests "io.github.woosb.annualleave.AnnualLeaveCalculatorTest\$Article60Section1"
```

DB·Docker·`.env` 없이 순수 단위 테스트만 돈다. 빌드가 깨지면 대부분 JDK 문제다.

### JDK: 셸에서 돌리려면 JAVA_HOME 을 먼저 잡는다

이 Windows 머신의 시스템 기본 JVM 은 8(`C:\Program Files\Java\jdk1.8.0_301`)이고 `JAVA_HOME` 이 비어 있다. 그대로 `./gradlew` 를 부르면 `Gradle requires JVM 17 or later` 로 즉시 죽는다. JDK 21 은 IntelliJ 가 설치해둔 게 있으므로 그걸 가리키면 된다.

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\temurin-21.0.12"; .\gradlew.bat test
```

- `build.gradle` 에 toolchain 21 이 걸려 있고 **자동 다운로드는 꺼져 있다**. JDK 21 이 없으면 `No matching toolchains found` 로 실패한다.
- 빌드는 21 로 하되 바이트코드는 17 로 떨어진다(`options.release = 17`). CI 가 `javap` 로 major version 61 을 검증하므로 이 설정을 바꾸면 CI 가 깨진다.

### 런타임 의존성 추가는 빌드가 막는다

`checkNoRuntimeDependencies` 태스크가 `check` 에 물려 있어 `implementation`/`api` 의존성이 하나라도 붙으면 빌드가 실패한다. "클래스패스 충돌 없음" 이 이 라이브러리의 핵심 가치라 규칙으로 고정했다. 우회하지 말고, 정말 필요하면 태스크를 먼저 제거하는 것이 의도된 절차다.

## 아키텍처

계산 로직은 **조문 하나 = `Rule` 구현체 하나** 로 분해되어 있다. 법 개정 시 조문 번호만으로 고칠 파일이 결정되도록 한 구조다.

```
AnnualLeaveCalculator.calculate(record)   유일한 public 진입점, static
  └─ Rules.all() 을 순회하며 각 Rule.apply(record) 결과를 모음
       └─ AnnualLeave.of(grants)  총 일수는 grants 합으로 자동 산출
```

- `AttendanceRecord` — 입력. 출근율을 `double` 로 받지 않고 소정근로일수/출근일수를 따로 받는다. 소정근로일수가 육아휴직·업무상 부상 등 법정 사유로 조정되므로 분모를 보존해야 그 규칙을 나중에 라이브러리 안에서 처리할 수 있다. 생성자에서 입력 검증(날짜 순서, 음수, 출근일수 초과)을 한다.
- `AnnualLeave` + 내부 `Grant` record — 결과. 총계만이 아니라 **발생 내역과 근거 조문**을 함께 담는 것이 이 라이브러리의 설계 의도다. "왜 15일인가" 를 답할 수 없으면 실무에서 쓸 수 없다.
- `LegalBasis` enum — 조문 표기와 요지. 아직 미구현인 조문(제2항, 제4항)도 이미 정의되어 있다.
- `internal` 패키지 — `Rule`, `Rules`, `Article60Section1Rule`. `module-info.java` 에서 export 하지 않아 모듈 경로 소비자는 접근 자체가 불가능하다. 새 public 패키지를 만들면 `module-info.java` 에 `exports` 를 추가해야 한다.

## 미구현 조문 작업 절차

테스트가 명세다. `AnnualLeaveCalculatorTest` 에 미구현 조문이 `@Disabled` 붙은 `@Nested` 클래스로 이미 들어 있다.

1. 해당 `@Disabled` 를 지운다 (테스트에 없는 새 조문이면 `LegalBasis` 상수 추가 + `@Nested` 케이스 작성이 먼저다)
2. `./gradlew test` — **red 를 먼저 확인한다** (테스트가 실제로 뭘 검증하는지 알기 위한 단계이므로 건너뛰지 않는다)
3. `internal/Article60SectionNRule.java` 를 작성한다
4. `internal/Rules.ALL` 에 등록한다
5. green

현재 구현 상태: 제60조 제1항만 구현. 제2항(1년 미만 월 개근당 1일), 제4항(3년 이상 매 2년 1일 가산, 25일 한도)은 미구현. 산정 방식은 **입사일 기준만** 지원하며 회계연도 기준은 0.2.0 이후다.

`@Disabled` 클래스는 테스트 로그에 줄이 찍히지 않고 skipped 로만 집계되므로, `./gradlew test` 통과가 전 조문 검증을 뜻하지 않는다. 실제 도는 건 제1항 6건 + 입력 검증 4건이고 제2항·제4항은 각각 skipped 1 이다. 집계는 `build/test-results/test/*.xml` 또는 `build/reports/tests/test/index.html` 로 확인한다.

## 컨벤션

- 코드 주석·Javadoc·예외 메시지·테스트 메서드명 모두 한국어이고 평서체("~이다", "~할 수 없다")를 쓴다. 커밋 메시지도 한국어다.
- `.gitattributes` 가 저장소 전체를 LF 로 고정한다(`*.bat`/`*.cmd` 만 CRLF). CI 가 ubuntu 러너에서 `./gradlew` 를 직접 실행하기 때문이다.
- 컴파일에 `-Xlint:all` 이 켜져 있다.

## 배포

`build.gradle` 의 `signing` 은 SNAPSHOT 이 아니고 `publish` 태스크일 때만 강제되므로 로컬 빌드는 서명 없이 통과한다. Central Portal 업로드는 아직 붙어 있지 않고 0.1.0 릴리즈 시점에 추가한다. 지금은 `publishToMavenLocal` 로만 검증한다.
