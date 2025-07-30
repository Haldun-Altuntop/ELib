package arc.haldun.hurda.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class Utilities {

    public static final int SCREEN_WIDTH;
    public static final int SCREEN_HEIGHT;

    static {
        SCREEN_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
        SCREEN_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private static AlertDialog loadingDialog;

    public static void unauthorizedUserDetected(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.unauthorized_user_title)
                .setMessage(R.string.invalid_session_message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    context.startActivity(new Intent(context, LoginActivity.class));
                    ((Activity) context).finish();
                });

        builder.create().show();
    }

    public static void showLoadingDialog(Context context, String msg) {

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        TextView label = view.findViewById(R.id.dialog_loading_tv_label);
        label.setText(msg);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        loadingDialog = builder.create();

        loadingDialog.show();
    }

    public static void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing())  loadingDialog.dismiss();
    }
}
