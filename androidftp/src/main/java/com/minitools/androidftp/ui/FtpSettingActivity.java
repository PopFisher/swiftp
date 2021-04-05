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

import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.widget.Toast;

import com.minitools.androidftp.FsSettings;
import com.minitools.androidftp.R;

import net.vrallev.android.cat.Cat;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * This is the main activity for swiftp, it enables the user to start the server service
 * and allows the users to change the settings.
 */
public class FtpSettingActivity extends AppCompatActivity {

    final static int PERMISSIONS_REQUEST_CODE = 12;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Cat.d("created");
        setTheme(FsSettings.getTheme());
        super.onCreate(savedInstanceState);

        if (!haveReadWritePermissions()) {
            requestReadWritePermissions();
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferenceFragment())
                .commit();
    }

    private boolean haveReadWritePermissions() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
                    && checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestReadWritePermissions() {
        if (VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        String[] permissions = new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
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
}
