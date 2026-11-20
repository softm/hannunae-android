# 한눈애 Android

**net.softm.hannunae** · 순수 HTML 웹을 오프라인 번들로 여는 Android 앱입니다.

| 항목 | 구성 |
|---|---|
| 앱 이름 | 한눈애 |
| 운영 패키지 | net.softm.hannunae |
| Debug 패키지 | net.softm.hannunae.debug |
| 웹 원본 | [softm/hannunae-web](https://github.com/softm/hannunae-web) |
| 공개 웹 URL | https://softm.github.io/hannunae-web/ |
| 실행 문서 | https://appassets.androidplatform.net/assets/index.html |
| Android 기준 | compileSdk/targetSdk 36 · minSdk 24 · Java 17 |
| 빌드 | AGP 8.13.2 · Gradle 8.13 |

## 공통 기능을 독립 라이브러리로 사용

앱 전체 템플릿을 가져오지 않았습니다. 이 저장소는 앱별 설정, 작은 MainActivity, 자체 웹 번들만 소유합니다. WebView 반복 기능은 **[hannun-webview-android](https://github.com/softm/hannun-webview-android)**의 고정된 Git submodule을 실제 Gradle source module로 의존합니다.

| 독립 라이브러리 | 고정 값 | 역할 |
|---|---|---|
| hannun-webview-android | 이 저장소의 gitlink commit | HTTPS assets, SAF 백업/복원, 인쇄, 공유, 외부 브라우저, 메시지 bridge |
| hannun-admob-android | 1.0.4 / abc750491d68605053ee93740600e35cf3fdf51c | UMP, 하단 native/adaptive fallback, 시간 기반 전면, 종료 300×250 |
| hannun-in-app-update-android | 1.0.0 / 01e15b9fd58e76efeea1d1d069a5187140cdebff | Google Play Flexible/Immediate 업데이트 |

AdMob 1.0.4를 의도적으로 고정했습니다. 1.0.5의 종료창은 광고 로드 실패 시 종료 버튼을 계속 비활성화하는 변경이 있습니다. 1.0.4는 실패/미초기화/5초 타임아웃에 종료를 허용합니다. 해당 버전의 소스 build 파일은 project.version=1.0.3이지만 소비 좌표는 실제 Git tag 1.0.4입니다.

## 바로 빌드

```sh
git clone --recurse-submodules https://github.com/softm/hannunae-android.git
cd hannunae-android
./gradlew :hannun-webview:testDebugUnitTest :app:assembleDebug
```

SDK 36과 Build Tools 35.0.0, JDK 17이 필요합니다. CI는 같은 명령으로 Debug APK를 만들며 artifact 보존 기간은 1일입니다. 공유 모듈을 인접 경로에서 개발할 때만 `-PhannunWebViewDir=../hannun-webview-android`를 사용할 수 있습니다.

## 웹과 앱의 콘텐츠 동기화

웹 저장소의 원본 파일을 수정하고 검증한 다음 다음 명령으로 번들에 반영합니다.

```sh
python3 scripts/sync-web-assets.py ../hannunae-web
./gradlew :app:assembleDebug
```

JavaScript 빌드/번들러는 없습니다. HTML/CSS/JS/manifest/이미지 원본을 복사하며 `web-assets.lock.json`에 파일별 SHA-256을 기록합니다. 웹의 변경이 이미 설치된 Android 앱에 자동 반영되지는 않으며 새 versionCode로 앱을 배포해야 합니다.

## 광고와 릴리스

Debug에는 [Google 공식 테스트 광고](https://developers.google.com/admob/android/test-ads)만 들어갑니다.

운영 광고를 활성화하려면 `admob.properties.example`을 `admob.properties`로 복사하고 **이 앱용으로 발급된** 앱 ID, native, adaptive banner, interstitial, exit banner ID를 각각 입력합니다. 동일한 값은 환경변수로도 전달할 수 있습니다. 광고 단위 4개는 서로 달라야 하며 Google 테스트 publisher를 Release에서 허용하지 않습니다.

ID가 누락되거나 유효하지 않으면 Release는 광고를 비활성화하고 일반 종료창을 사용합니다. 생성되는 Release manifest overlay는 `MobileAdsInitProvider` 및 앱 ID metadata를 제거하므로 빈 AdMob 앱 ID 자동 초기화 오류를 피합니다. 개인정보 옵션은 네이티브 앱 메뉴와 UMP에서 접근합니다.

운영 서명은 **기존 softm.keystore.jks**만 사용합니다. `signing.properties.example`을 참고해 로컬 `signing.properties`를 설정합니다. 키나 비밀번호를 저장소에 넣지 않습니다. 미설정 시 `assembleRelease` 결과는 unsigned APK입니다. 신규 운영키를 만들지 않습니다.

```sh
./gradlew :app:assembleRelease
```

## 기기에서 확인할 흐름

1. 첫 실행과 비행기 모드에서 번들 HTML을 연다.
2. 기록 저장 → JSON 백업 → SAF 저장 위치 선택 → 삭제/복원 순서로 데이터가 보존되는지 확인한다.
3. 한글·이모지 제목 텍스트 내보내기, 저장 취소, 5 MiB 초과 거부를 확인한다.
4. 인쇄 → PDF 저장 및 인쇄 취소 후 웹 화면 복귀를 확인한다.
5. 공통계정/외부 HTTPS 링크가 브라우저에서 열리고 앱 로컬 기록을 자동 업로드하지 않는지 확인한다.
6. Debug 테스트 광고의 native 실패 시 adaptive fallback, 종료창 실패/타임아웃, 개인정보 옵션을 확인한다.
7. Play 내부 테스트 설치본에서 더 높은 versionCode를 올려 업데이트를 검증한다. 직접 설치와 Debug는 not_play_install 상태다.

실기기 광고 노출·지역별 UMP·Play 업데이트 성공 및 Play Store 출시는 별도 배포 검증 단계입니다. 계정 로그인과 기기별 로컬 기록은 별개이며 자동 동기화하지 않습니다.
