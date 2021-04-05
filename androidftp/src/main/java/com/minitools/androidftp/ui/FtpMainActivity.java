/*******************************************************************************
 * Copyright (c) 2012-2013 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors:
 * Pieter Pareit - initial API and implementation
 ******************************************************************************/

package com.minitools.androidftp.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.minitools.androidftp.FsService;
import com.minitools.androidftp.FsSettings;
import com.minitools.androidftp.R;

import net.vrallev.android.cat.Cat;

import java.net.InetAddress;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * This is the main activity for swiftp, it enables the user to start the server service
 * and allows the users to change the settings.
 */
public class FtpMainActivity extends AppCompatActivity {

    final static int PERMISSIONS_REQUEST_CODE = 12;
    private TextView networkName;
    private TextView ftpUseTip;
    private TextView address;
    private TextView openCloseBtn;

    private View addressRoot;
    private View addressShare;
    private View settings;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!haveReadWritePermissions()) {
            requestReadWritePermissions();
        }
        setContentView(R.layout.main_layout);
        initViews();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViewState();
    }

    private void initListeners() {
        openCloseBtn.setOnClickListener(v -> {
            openOrCloseFtp();
        });
        addressShare.setOnClickListener(v -> {
            shareFtpAddress();
        });
        addressRoot.setOnLongClickListener(v -> {
            copy2ClipBoard();
            return false;
        });
        settings.setOnClickListener(v -> {
            startActivity(new Intent(this, FtpSettingActivity.class));
        });
    }

    private void shareFtpAddress() {
        if (address.getText().toString().isEmpty()) {
            return;
        }
        String content = address.getText().toString();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, content);
        startActivity(
                Intent.createChooser(
                        share,
                        getString(R.string.share_to)
                )
        );
    }

    private void openOrCloseFtp() {
        boolean isOpen = FsService.isRunning();
        if (isOpen) {
            FsService.stop();
        } else {
            FsService.start();
        }
        updateAddressState(true, !isOpen);
    }

    private void copy2ClipBoard() {
        if (address.getText().toString().isEmpty()) {
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("text", address.getText()));

        Toast.makeText(this, R.string.copy_2_clipboard_tip, Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        networkName = findViewById(R.id.net_name);
        ftpUseTip = findViewById(R.id.ftp_use_tip);
        address = findViewById(R.id.ftp_address_tv);
        openCloseBtn = findViewById(R.id.ftp_open_close);
        addressRoot = findViewById(R.id.ftp_address_root);
        addressShare = findViewById(R.id.ftp_address_share);
        settings = findViewById(R.id.ftp_setting);
    }

    private void updateViewState() {
        updateWifiName();
        updateAddressState(false, false);
    }

    private void updateAddressState(boolean isLoopUpdate, boolean destState) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        boolean isOpen = FsService.isRunning();
        openCloseBtn.setText(getString(isOpen ? R.string.close_ftp : R.string.open_ftp));
        ftpUseTip.setText(getString(isOpen ? R.string.ftp_input_tip : R.string.ftp_use_tip));
        addressRoot.setVisibility(isOpen ? View.VISIBLE : View.GONE);

        InetAddress ipAddress = FsService.getLocalInetAddress();
        String ipText = ipAddress != null ? ("ftp://" + ipAddress.getHostAddress() + ":"
                + FsSettings.getPortNumber() + "/") : "";
        address.setText(ipText);

        if (isLoopUpdate && destState != isOpen) {
            handler.postDelayed(() -> updateAddressState(isLoopUpdate, destState), 300);
        }
    }

    private boolean haveReadWritePermissions() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
                    && checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED
                    && checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestReadWritePermissions() {
        if (VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        String[] permissions = new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION};
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST_CODE) {
            Cat.e("Unhandled request code");
            return;
        }
        Cat.d("permissions: " + Arrays.toString(permissions));
        Cat.d("grantResults: " + Arrays.toString(grantResults));
        if (grantResults.length > 0) {
            // Permissions not granted, close down
            for (int result : grantResults) {
                if (result != PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.unable_to_proceed_no_permissions,
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void updateWifiName() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiManager.getWifiState();
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null) {
            networkName.setText(info.getSSID());
        }
    }
}
