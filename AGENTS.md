# 한눈애 Android

- 순수 HTML/CSS/일반 JavaScript 웹 콘텐츠를 이 앱의 web-assets에 번들한다. 현대 웹 프레임워크나 앱 전체 템플릿을 복제하지 않는다.
- 반복 WebView 기능은 고정된 submodule libraries/hannun-webview-android의 독립 library 모듈로만 공유한다.
- 광고는 hannun-admob-android 1.0.4, 업데이트는 hannun-in-app-update-android 1.0.0을 사용한다.
- 하단 compact native + 같은 지면 adaptive fallback, 시간 기반 interstitial, 종료 확인창 300×250, UMP/개인정보 옵션, 실패 시 빈 공간 제거를 유지한다.
- 운영 AdMob 앱/지면 ID는 이 앱용으로 새로 발급하며 다른 앱의 운영 ID를 복사하지 않는다. Debug는 Google 공식 테스트 ID만 사용한다.
- Release 운영 ID가 모두 설정되지 않으면 광고와 SDK 자동 초기화 provider를 제거하고 일반 종료창을 사용한다.
- WebView UA의 AdMobNative/1, AdFit 네트워크 차단 및 빈 컨테이너 제거를 유지한다. AdFit을 공통 지면으로 추가하지 않는다.
- Java 17, compileSdk/targetSdk 36, minSdk 24.
- 운영 서명은 기존 softm.keystore.jks만 사용한다. 신규 운영키 생성이나 업로드 키 reset을 하지 않는다. 키/비밀번호는 커밋하지 않는다.
- 운영 서명 미설정 상태는 unsigned release다. Debug APK를 Play 출시 완료로 표현하지 않는다.
