package in.nishantarora.alternativescroll;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Activity";
    private WebView webview;
    private FingerprintManager fingerprintManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean override = false;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.v(TAG, "Volume Up Pressed");
            override = pageHandler(Directions.UP);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            Log.v(TAG, "Volume Down Pressed");
            override = pageHandler(Directions.DOWN);
        }else if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.v(TAG, "Back Pressed");
            override = pageHandler(Directions.BACK);
        }
        Log.v(TAG, String.valueOf(override));
        return override || super.dispatchKeyEvent(event);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean pageHandler(Integer direction) {
        boolean handled = false;
        if (direction == Directions.DOWN) {
            toastMsg("Going Down");
            handled = webview.pageDown(false);
        } else  if (direction == Directions.UP) {
            toastMsg("Going Up");
            handled = webview.pageUp(false);
            fingerprintManager.authenticate(
                    null, null, 0, new gestureControl(), null);
        } else if (direction == Directions.BACK) {
            handled = webview.canGoBack();
            if (handled) {
                toastMsg("Going Back");
                webview.goBack();
            }
        }
        return handled;
    }

    private abstract class Directions {
        static final int UP = 0;
        static final int DOWN = 1;
        static final int BACK = 2;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
