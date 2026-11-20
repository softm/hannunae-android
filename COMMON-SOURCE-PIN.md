# 공통 WebView 소스 고정

- Repository: https://github.com/softm/hannun-webview-android
- Path: `libraries/hannun-webview-android`
- Commit: `1d0ca7a4974addc46a66952d028c2f71bd28e523`
- Gradle project: `:hannun-webview` → `library` source module

`git clone --recurse-submodules` 또는 `git submodule update --init --recursive`로 같은 소스를 가져옵니다. 앱 전체 템플릿이 아닌 WebView 반복 기능만 독립 모듈로 재사용합니다. 업데이트 시 단위 테스트/앱 빌드를 확인하고 gitlink와 이 문서를 함께 갱신합니다.
