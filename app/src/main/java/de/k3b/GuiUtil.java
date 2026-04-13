/*
 * Copyright (C) 2026 k3b
 * 
 * This file is part of de.k3b.android.toGoZip (https://github.com/k3b/ToGoZip/) .
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package de.k3b;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.k3b.intentintercept.R;

/**
 * gui utils
 */
public class GuiUtil {
    @NonNull
    public static String getAppVersionName(final Context context, @StringRes int resourceId) {
        try {

            String date = getDateAsString();
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return context.getString(resourceId,context.getString(R.string.app_name), packageInfo.versionName, packageInfo.versionCode, date);
        } catch (final NameNotFoundException e) {
            e.printStackTrace();
        }
        return "?";
    }

    @NonNull
    public static String getDateAsString() {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
        return date;
    }

}
