package ali.abdullahmansour.egypt;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class OffLine extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


    }
}
