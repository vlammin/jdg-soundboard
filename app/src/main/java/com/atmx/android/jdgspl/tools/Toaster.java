/*
 * ToastUtils
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl.tools;

import android.app.Activity;
import android.widget.Toast;

public class Toaster {

    public static void showLong(Activity activity, int stringResId) {
        Toast.makeText(activity, stringResId, Toast.LENGTH_LONG).show();
    }

    public static void showShort(Activity activity, int stringResId) {
        Toast.makeText(activity, stringResId, Toast.LENGTH_SHORT).show();
    }
}
