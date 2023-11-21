package arc.haldun.mylibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import arc.haldun.database.driver.Connector;
import arc.haldun.mylibrary.main.ErrorActivity;

public class Tools {

    public static void makeText(Context c, String message) {
        Toast.makeText(c, message, Toast.LENGTH_LONG).show();
    }

    public static Update hasUpdate(Activity activity) {

        Update update = new Update();

        try {

            String sql = "SELECT * FROM updates";
            Statement statement = Connector.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {

                int versionCode = resultSet.getInt("VersionCode");

                if (versionCode > BuildConfig.VERSION_CODE) {
                    update.setHasNew(true);
                    update.setVersionCode(versionCode);
                    break;
                }
            }
        } catch (SQLException e) {
            startErrorActivity(activity, e);
        }

        return update;
    }

    public static void startErrorActivity(Context context, Exception exception) {

        exception.printStackTrace();

        Intent intent = new Intent(context, ErrorActivity.class);
        intent.setAction(Intent.ACTION_APP_ERROR);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("ErrorMessage", exception.getMessage());
        context.startActivity(intent);
    }

    public static void startErrorActivity(Context context, Exception exception, int errorCode) {

        exception.printStackTrace();

        Intent intent = new Intent(context, ErrorActivity.class);
        intent.setAction(Intent.ACTION_APP_ERROR);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("ErrorMessage", exception.getMessage());
        intent.putExtra("ErrorCode", errorCode);
        context.startActivity(intent);
    }

    public static class Update {

        private boolean hasNew = false;
        private int versionCode;

        public boolean hasNew() {
            return hasNew;
        }

        public void setHasNew(boolean hasNew) {
            this.hasNew = hasNew;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }
    }

    public static class Preferences {

        public static String NAME = "preferences";

        private final SharedPreferences sharedPreferences;

        public Preferences(SharedPreferences sharedPreferences) {
            this.sharedPreferences = sharedPreferences;
        }

        public void setValue(String key, boolean value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
        }

        public void setValue(String key, String value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        }

        public void setValue(String key, int value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, value);
            editor.apply();
        }

        public int getInt(String key) {
            return sharedPreferences.getInt(key, -1);
        }

        public String getString(String key) {
            return sharedPreferences.getString(key, null);
        }

        public boolean getBoolean(String key) {
            return sharedPreferences.getBoolean(key, false);
        }

        public static class Keys {

            public static String REMEMBER_ME = "remember_me";
            public static String LANGUAGE = "language";
            public static String BOOK_SORTING_TYPE = "book_sorting_type";
        }

    }
}
