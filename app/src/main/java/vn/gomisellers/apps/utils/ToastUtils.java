package vn.gomisellers.apps.utils;

import android.widget.Toast;

import vn.gomisellers.apps.EappsApplication;

/**
 * Created by KHOI LE on 3/26/2020.
 */
public class ToastUtils {
    private static Toast toast;

    public static void showToast(String msg) {
        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(EappsApplication.getInstance(), msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
