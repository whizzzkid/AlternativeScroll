package in.nishantarora.alternativescroll;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Activity";
    private WebView webview;
    private FingerprintManager fingerprintManager;

    public boolean hasPermission(String[] permissions) {
        boolean hasPermission = true;
        for (String permission : permissions) {
            if (getApplicationContext().checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                break;
            }
        }
        if (!hasPermission) requestPermissions(permissions, 1);
        return hasPermission;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        fingerprintManager = (FingerprintManager)
                getSystemService(FINGERPRINT_SERVICE);
        final String[] FP_PERMISSIONS = {Manifest.permission.USE_FINGERPRINT};

        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient() {
        });
        webview.loadUrl("http://reddit.com/r/uoft");
        setContentView(webview);

        if (hasPermission(FP_PERMISSIONS)) {
            fingerprintManager.authenticate(
                    null, null, 0, new gestureControl(), null);
        }
    }

    public void toastMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void pageHandler(Integer direction) {

        if (direction == Directions.DOWN) {
            toastMsg("Going Down");
            webview.pageDown(false);
        }

        if (direction == Directions.UP) {
            toastMsg("Going Up");
            webview.pageUp(false);
            fingerprintManager.authenticate(
                    null, null, 0, new gestureControl(), null);
        }
    }

    private abstract class Directions {
        static final int UP = 0;
        static final int DOWN = 1;
    }

    class gestureControl extends FingerprintManager.AuthenticationCallback {
        @Override
        public void onAuthenticationError(
                int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Log.v(TAG, "Auth Err: Tap Gesture");
            //pageHandler(Directions.UP);
        }

        @Override
        public void onAuthenticationHelp(
                int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
            Log.v(TAG, "Swipe Gesture");
            pageHandler(Directions.DOWN);
        }

        @Override
        public void onAuthenticationSucceeded(
                FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Log.v(TAG, "Go Up");
            pageHandler(Directions.UP);
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Log.v(TAG, "Authentication failed");
            pageHandler(Directions.UP);
        }
    }
}
