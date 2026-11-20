package net.softm.hannunae;

import android.content.Intent;
import android.webkit.WebView;
import android.widget.LinearLayout;
import net.softm.hannun.webview.HannunWebViewActivity;
import net.softm.hannun.webview.HannunWebViewConfig;
import softm.hannun.ads.HannunAdConfig;
import softm.hannun.ads.HannunAds;
import softm.hannun.ads.HannunWebViewAds;
import softm.hannun.update.HannunInAppUpdateManager;

public final class MainActivity extends HannunWebViewActivity {
    private HannunAds ads;
    private HannunInAppUpdateManager updater;

    @Override protected HannunWebViewConfig webConfig() {
        return new HannunWebViewConfig(getString(R.string.app_name), BuildConfig.PUBLIC_WEB_URL, BuildConfig.DEBUG);
    }

    @Override protected void onWebViewCreated(WebView view, LinearLayout adHost) {
        HannunWebViewAds.markNativeAds(view.getSettings());
        updater = new HannunInAppUpdateManager(this, null);
        if (BuildConfig.ADMOB_ENABLED) {
            ads = new HannunAds(this, adHost, new HannunAdConfig(
                    getString(R.string.admob_native_ad_unit_id),
                    getString(R.string.admob_adaptive_banner_ad_unit_id),
                    getString(R.string.admob_interstitial_ad_unit_id),
                    getString(R.string.admob_exit_banner_ad_unit_id)));
            ads.start();
        }
    }

    @Override protected void onCheckForUpdateRequested() {
        if (updater == null) return;
        updater.checkManually(state -> {
            String status = state.optString("status");
            if ("not_play_install".equals(status)) {
                toast("Google Play 설치본에서 앱 업데이트를 확인할 수 있습니다.");
            } else if ("up_to_date".equals(status)) {
                toast("최신 버전입니다.");
            } else if ("unavailable".equals(status) || "failed".equals(status)) {
                toast("지금은 업데이트를 확인할 수 없습니다. 잠시 후 다시 확인해 주세요.");
            }
        });
    }

    @Override protected void onPrivacyOptionsRequested() {
        if (ads != null) ads.showPrivacyOptions();
        else super.onPrivacyOptionsRequested();
    }

    @Override protected void onExitRequested() {
        if (ads != null) ads.showExitConfirmation(this::finish);
        else showPlainExitConfirmation();
    }

    @Override protected void onResume() {
        super.onResume();
        if (updater != null) updater.onResume();
        if (ads != null) ads.onResume();
    }

    @Override protected void onPause() {
        if (ads != null) ads.onPause();
        if (updater != null) updater.onPause();
        super.onPause();
    }

    @Override @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (updater != null && updater.onActivityResult(requestCode, resultCode)) return;
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override protected void onDestroy() {
        if (ads != null) ads.destroy();
        if (updater != null) updater.destroy();
        super.onDestroy();
    }
}
