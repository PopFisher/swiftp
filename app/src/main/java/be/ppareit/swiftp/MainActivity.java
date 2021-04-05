package be.ppareit.swiftp;

import android.app.Activity;
import android.os.Bundle;

import com.minitools.androidftp.FtpAndroid;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FtpAndroid.startFtpMainActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            FtpAndroid.onDestroy();
        }
    }
}
