# annual-leave-kr

근로기준법 제60조 연차유급휴가 계산 라이브러리.
의존성 0, Java 17+, 상태 없음.

```java
var record = new AttendanceRecord(
        LocalDate.of(2024, 3, 15),   // 입사일
        LocalDate.of(2025, 3, 15),   // 기준일
        248,                          // 소정근로일수
        248);                         // 출근일수

AnnualLeave leave = AnnualLeaveCalculator.calculate(record);

leave.totalDays();                        // 15.0
leave.grants().get(0).basis().article();  // "근로기준법 제60조 제1항"
```

총 일수만 주지 않고 **발생 내역과 근거 조문을 함께** 준다.
"왜 15일인가" 를 답할 수 없으면 실무에서 쓸 수 없기 때문이다.

## 상태

**0.1.0 개발 중. 아직 배포 전이다.**

| 조문 | 내용 | 상태 |
|---|---|---|
| 제60조 제1항 | 1년 80% 이상 출근 시 15일 | 구현 |
| 제60조 제2항 | 1년 미만 월 개근당 1일 | 미구현 |
| 제60조 제4항 | 3년 이상 매 2년 1일 가산 (25일 한도) | 미구현 |

입사일 기준 산정만 지원한다. 회계연도 기준은 0.2.0 이후.

## 설치

아직 Maven Central 에 없다. 로컬에서 쓰려면:

```bash
./gradlew publishToMavenLocal
```

## 개발

### 1. JDK 21

`build.gradle` 에 toolchain 21 이 걸려 있다. 자동 다운로드는 붙이지 않았으므로
JDK 21 이 없으면 `No matching toolchains found` 로 실패한다.

```bash
# macOS
brew install --cask temurin@21
```

Windows 는 IntelliJ 가 설치해둔 JDK 를 그대로 쓸 수 있다 (`~/.jdks/temurin-21.x`).
다만 IntelliJ 밖의 셸에서 `gradlew` 를 돌리려면 `JAVA_HOME` 을 직접 잡아야 한다.
안 잡으면 시스템 기본 JVM(대개 8)이 잡혀 `Gradle requires JVM 17 or later` 로 죽는다.

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\temurin-21.0.12"
```

빌드는 21 로 하지만 **바이트코드는 17 로 떨어진다** (`options.release = 17`).
Spring Boot 3 이상 환경 전체를 커버하기 위한 선택이고, CI 에서 major version 61 인지 검증한다.

### 2. 빌드

```bash
./gradlew build
```

DB 도 Docker 도 `.env` 도 필요 없다. 순수 단위 테스트뿐이다.
여기서 깨지면 JDK 문제다.

### 3. 의존성 추가는 빌드가 막는다

```
> 런타임 의존성이 추가되었다: [guava-33.4.0-jre.jar]
  이 라이브러리는 의존성 0 을 유지한다.
```

`checkNoRuntimeDependencies` 태스크가 `check` 에 물려 있다.
이 라이브러리의 핵심 가치가 "클래스패스 충돌 없음" 이라 규칙으로 고정해뒀다.

## 미구현 조문 작업하는 법

테스트가 명세다. 조문을 먼저 테스트로 적고, 그 다음에 구현한다.

이미 적혀 있는 조문(제2항·제4항)은 `AnnualLeaveCalculatorTest` 에
`@Disabled` 붙은 `@Nested` 클래스로 들어 있다.

1. 해당 `@Disabled` 를 지운다
2. `./gradlew test` — **red 를 확인한다**
3. `internal/Article60SectionNRule.java` 를 작성한다
4. `internal/Rules.ALL` 에 등록한다
5. green

red 를 안 보고 넘어가면 테스트가 실제로 뭘 검증하는지 알 수 없다.

아직 적히지 않은 조문을 새로 들일 때는 0 번이 하나 더 붙는다:
`LegalBasis` 에 상수를 추가하고, `@Nested` 클래스로 케이스를 먼저 적는다.
케이스는 조문 텍스트에서 뽑는다 — 요건 충족, 요건 미달, 경계값(발생일 하루 전/당일), 상한.

### `@Disabled` 는 초록색으로 보인다

`@Disabled` 클래스는 테스트 로그에 아예 줄이 안 찍히고 skipped 로만 집계된다.
`./gradlew test` 가 통과했다고 전 조문이 검증된 게 아니다.
지금 실제로 도는 건 제1항 6건과 입력 검증 4건뿐이고, 제2항·제4항은 각각 skipped 1 이다.
집계는 `build/reports/tests/test/index.html` 에서 확인한다.

## 구조

```
src/main/java/
  module-info.java                    internal 패키지를 export 하지 않는다
  io/github/woosb/annualleave/
    AnnualLeaveCalculator.java        public — 유일한 진입점
    AttendanceRecord.java             public — 입력
    AnnualLeave.java                  public — 결과 (+ Grant)
    LegalBasis.java                   public — 근거 조문
    internal/
      Rule.java                       조문 하나 = 구현체 하나
      Rules.java                      규칙 등록부
      Article60Section1Rule.java
```

`internal` 은 관용적 신호일 뿐 컴파일러가 막아주지 않는다 —
그래서 `module-info.java` 로 export 를 끊었다. 모듈 경로 소비자는 접근 자체가 안 된다.

### 왜 출근율을 `double` 로 안 받나

`AttendanceRecord` 는 소정근로일수와 출근일수를 따로 받는다.
출근율은 계산해서 나오는 값이지 입력값이 아니고, 소정근로일수 자체가
육아휴직·업무상 부상 등 법정 사유로 조정된다. 분모를 보존해야
그 조정을 나중에 라이브러리 안에서 처리할 수 있다.

## 배포 (0.1.0 시점)

- Central Portal (`central.sonatype.com`) — 구 OSSRH 는 2025-06-30 종료
- GitHub 계정으로 가입하면 `io.github.woosb` 네임스페이스가 자동 검증됨
- 필수: GPG 서명, sources jar, javadoc jar, POM 메타데이터
- **한 번 올리면 수정·삭제 불가.** 로컬 소비자 프로젝트에서 실제로 import 해보고 올린다

## 라이선스

Apache License 2.0
