# 🚀 Compose State 케이스 스터디

[![API](https://img.shields.io/badge/API-28%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=28)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.12.01-green)](https://developer.android.com/jetpack/compose)
[![Material3](https://img.shields.io/badge/Material%20Design%203-yes-orange)](https://m3.material.io/)

> **Jetpack Compose에서 다양한 상태 관리 및 아키텍처 패턴을 학습할 수 있는 종합 교육용 Android 애플리케이션**

## 📖 프로젝트 소개

이 프로젝트는 **Android 개발자를 위한 실무 중심의 학습 자료**로, Jetpack Compose 환경에서 사용할 수 있는 22가지 이상의 상태 관리 및 아키텍처 패턴을 실제 구현을 통해 학습할 수 있도록 구성되었습니다.

각 구현은 실제 프로덕션 환경을 고려하여 **에러 처리, 로딩 상태, CRUD 작업** 등을 포함하며, 20% 에러 시뮬레이션을 통해 실제와 같은 네트워크 환경을 재현합니다.

## ✨ 주요 특징

- 🎯 **22개 이상의 구현 패턴** - 다양한 상태 관리 접근법 학습
- 📚 **5개 카테고리 분류** - 체계적인 학습 구조
- 🔄 **공통 CRUD 작업** - 실무와 동일한 기능 구현
- ⚡ **실시간 상태 관리** - 로딩, 성공, 실패 상태 처리
- 🎨 **Material 3 디자인** - 최신 디자인 시스템 적용
- 🧪 **에러 시뮬레이션** - 실제 환경과 유사한 테스트 조건

## 🏗️ 기술 스택

| 기술 | 버전 | 설명 |
|------|------|------|
| **Kotlin** | 2.1.0 | 메인 개발 언어 |
| **Jetpack Compose** | 2024.12.01 | 모던 UI 툴킷 |
| **Material 3** | - | Google Material Design |
| **Navigation Compose** | - | 단일 액티비티 네비게이션 |
| **ViewModel & LiveData** | - | AAC 컴포넌트 |
| **Coroutines & Flow** | - | 비동기 처리 |
| **RxJava 3** | - | 반응형 프로그래밍 |
| **Min SDK** | 28 | Android 9.0+ |
| **Target SDK** | 36 | - |

## 📁 프로젝트 구조

### 🎓 학습 카테고리

#### 📌 Agent01: 상태 관리 (7가지 구현)
1. **StateFlow + Sealed Classes** - 전통적인 반응형 상태 관리
2. **MutableState + Data Classes** - Compose 네이티브 상태 처리
3. **LiveData + Transformations** - 레거시 상태 관리 통합
4. **SharedFlow + Events** - 이벤트 기반 아키텍처
5. **Molecule Library Style** - 함수형 반응 패턴
6. **Coroutine Scope Management** - 고급 코루틴 생명주기 관리
7. **ViewModel Factory Pattern** - 의존성 주입 접근법

#### 🏛️ Agent02: 아키텍처 패턴 (5가지 구현)
1. **MVI (Model-View-Intent)** - 단방향 데이터 플로우
2. **MVP with Compose** - Compose용 프레젠터 패턴
3. **Clean Architecture** - 유스케이스, 레포지토리, 도메인 분리
4. **Redux-like Pattern** - 액션/리듀서 상태 관리
5. **Unidirectional Data Flow** - 모던 UDF 구현

#### ⚡ Agent03: 반응형 프로그래밍 (5가지 구현)
1. **Flow Chain Patterns** - 코루틴 Flow 체이닝과 연산자
2. **RxJava3 Integration** - RxJava를 활용한 반응형 스트림
3. **Channel + Actor Model** - 액터 기반 동시성 패턴
4. **Combined Flows** - 여러 Flow 결합 전략
5. **Hot/Cold Stream Management** - 스트림 생명주기 관리

#### 🎨 Agent04: Compose 상태 (5가지 구현)
1. **remember + mutableStateOf** - 기본 Compose 상태 패턴
2. **rememberSaveable + Parcelable** - 설정 변경 시 상태 보존
3. **CompositionLocal Provider** - Compose를 통한 의존성 주입
4. **State Hoisting** - 상태 관리 베스트 프랙티스
5. **Snapshot State System** - 고급 Compose 상태 내부 구조

#### 🔧 Agent05: 추가 패턴
- 기타 고급 구현 패턴들

## 🚀 시작하기

### 📋 필수 요구사항

- **Android Studio** Ladybug 이상
- **JDK 17** 이상
- **Android SDK 28** 이상

### 🔧 설치 및 실행

1. **레포지토리 클론**
   ```bash
   git clone https://github.com/your-username/MyApplication4.git
   cd MyApplication4
   ```

2. **Android Studio에서 프로젝트 열기**
   - Android Studio 실행
   - "Open an existing project" 선택
   - 클론한 폴더 선택

3. **빌드 및 실행**
   - Gradle 동기화 대기
   - 에뮬레이터 또는 실제 기기에서 실행

### 📱 앱 사용법

1. **메인 화면**에서 5개의 Agent 카테고리 확인
2. **각 Agent**를 선택하여 해당 카테고리의 구현 패턴들 탐색
3. **개별 구현**을 선택하여 실제 동작 확인
4. **CRUD 작업** (추가, 수정, 삭제, 새로고침) 테스트
5. **에러 상황** 시뮬레이션을 통한 예외 처리 학습

## 🎯 구현 특징

### 🔄 공통 기능

모든 구현은 다음과 같은 **일관된 기능**을 제공합니다:

#### 📊 UI 상태 관리
- **🔄 Initializing**: 2초간 로딩 시뮬레이션
- **✅ Succeed**: 성공적으로 로드된 상태  
- **❌ Failed**: 에러 상태 및 재시도 옵션

#### 📝 CRUD 작업
- **➕ 아이템 추가**: 네트워크 지연 시뮬레이션
- **✏️ 아이템 수정**: 다이얼로그를 통한 편집
- **🗑️ 아이템 삭제**: 확인 후 삭제
- **🔄 새로고침**: 서버에서 데이터 재로드
- **⚠️ 에러 처리**: 20% 확률의 에러 시뮬레이션

#### 📋 공통 데이터 모델
```kotlin
data class Item(
    val id: String,
    val title: String, 
    val description: String,
    val timestamp: Long
)
```

### 🧪 시뮬레이션 환경

- **네트워크 지연**: 실제 API 호출과 유사한 지연 시간
- **에러 발생**: 20% 확률로 에러 상황 재현
- **상태 전환**: 실제 앱과 동일한 상태 변화 패턴

## 📚 학습 목표

이 프로젝트를 통해 다음을 학습할 수 있습니다:

### 🎯 핵심 학습 포인트

1. **🔧 상태 관리 패턴**
   - 다양한 상태 관리 접근법의 장단점
   - 프로젝트 규모별 적합한 패턴 선택

2. **🏗️ 아키텍처 설계**
   - 확장 가능하고 유지보수가 쉬운 구조
   - 테스트하기 쉬운 코드 작성법

3. **⚡ 반응형 프로그래밍**
   - 비동기 데이터 스트림 처리
   - 에러 처리 및 재시도 로직

4. **🎨 Compose 통합**
   - 각 패턴과 Jetpack Compose의 효과적인 연동
   - 상태 보존 및 성능 최적화

5. **🚀 실무 적용**
   - 실제 프로덕션에서 사용할 수 있는 구현
   - 베스트 프랙티스 및 안티 패턴 이해

## 📸 스크린샷

*추후 추가 예정*

## 🤝 기여하기

프로젝트에 기여를 원하신다면:

1. Fork 후 새로운 브랜치 생성
2. 변경사항 구현
3. 테스트 확인
4. Pull Request 제출

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.

## 📞 연락처

프로젝트에 대한 문의나 제안사항이 있으시면 언제든 연락주세요.

---

**⭐ 이 프로젝트가 도움이 되셨다면 Star를 눌러주세요!**