package arc.haldun.mylibrary.services;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import arc.haldun.database.database.Manager;
import arc.haldun.database.database.MariaDB;
import arc.haldun.database.objects.User;

public class FirebaseUserService {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    public FirebaseUserService() {

        Handler UIThread = new Handler(Looper.getMainLooper());
        UIThread.post(() -> {

        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    public void createUser(String email, String password, com.google.android.gms.tasks
            .OnCompleteListener<AuthResult> onCompleteListener) {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener);
    }

    public boolean hasLoggedInUser() {

        return firebaseUser != null;

    }

    public void isUserValid(OnCompleteListener onCompleteListener) {


        HandlerThread thread = new HandlerThread("FirebaseUserServiceThread");
        thread.start();

        Runnable rUserValidator = () -> {

            Manager databaseManager = new Manager(new MariaDB());
            User user = null;

            if (firebaseUser != null) user = databaseManager.getUser(firebaseUser.getUid());

            boolean isValid = user != null;

            onCompleteListener.onComplete(isValid, user);

            thread.quit();

        };

        Handler handler = new Handler(thread.getLooper());
        handler.post(rUserValidator);
    }

    public void quit() {

        firebaseAuth = null;
        firebaseUser = null;

    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public interface OnCompleteListener {
        void onComplete(boolean b, User user);
    }

}
