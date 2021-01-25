package be.ppareit.swiftp;

import android.app.Application;
import android.content.Context;

import com.minitools.androidftp.FtpAndroid;

public class FtpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FtpAndroid.onCreate(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
