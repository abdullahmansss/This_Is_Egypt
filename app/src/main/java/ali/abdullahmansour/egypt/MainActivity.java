package ali.abdullahmansour.egypt;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import ali.abdullahmansour.egypt.CompanyApp.Fragments.ChatFragment;
import ali.abdullahmansour.egypt.CompanyApp.Fragments.HomeFragment;
import ali.abdullahmansour.egypt.CompanyApp.Fragments.ProfileFragment;
import ali.abdullahmansour.egypt.CompanyApp.Fragments.TourGuideFragment;
import ali.abdullahmansour.egypt.Models.UserData;
import ali.abdullahmansour.egypt.TouristApp.TouristProfileFragment;

public class MainActivity extends AppCompatActivity
{
    BottomNavigationView navigation;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = findViewById(R.id.navigation);

        fragmentManager = getSupportFragmentManager();

        int tag = getIntent().getIntExtra("TAG", 0);

        if (tag == 1)
        {
            loadFragment(new ProfileFragment());
            navigation.setSelectedItemId(R.id.profile);
        } else if (tag == 2)
        {
            loadFragment(new TouristProfileFragment());
            navigation.setSelectedItemId(R.id.profile);
        } else
            {
                loadFragment(new HomeFragment());
            }

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.home:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment home = new HomeFragment();
                        loadFragment(home);
                        return true;
                    case R.id.find:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment tourguide = new TourGuideFragment();
                        loadFragment(tourguide);
                        return true;
                    case R.id.chat:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment chat = new ChatFragment();
                        loadFragment(chat);
                        return true;
                    case R.id.profile:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null)
                        {
                            category();
                        } else
                        {
                            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                            startActivity(intent);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    public void category()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        final String userId = user.getUid();

        mDatabase.child("allusers").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        // Get user value
                        UserData userData = dataSnapshot.getValue(UserData.class);

                        String category = userData.getCategory();

                        if (category.equals("company"))
                        {
                            Fragment profile = new ProfileFragment();
                            loadFragment(profile);
                        } else
                            {
                                Fragment profile = new TouristProfileFragment();
                                loadFragment(profile);
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Toast.makeText(getApplicationContext(), "can\'t fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void loadFragment(Fragment fragment)
    {
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);

        getFragmentManager().popBackStack();
        // Commit the transaction
        fragmentTransaction.commit();
    }

    private long exitTime = 0;

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finishAffinity();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed()
    {
        doExitApp();
    }
}
