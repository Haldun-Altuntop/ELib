package arc.haldun.mylibrary.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.IOException;

import arc.haldun.mylibrary.server.api.ELibUtilities;

public class LogoutService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(this::sendLogoutRequest).start();
        stopSelf();
        return START_NOT_STICKY;
    }

    private void sendLogoutRequest() {
        try {
            ELibUtilities.quit();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
