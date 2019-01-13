package ali.abdullahmansour.egypt.CompanyApp.Fragments;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.List;

import ali.abdullahmansour.egypt.CompanyApp.EditProfileActivity;
import ali.abdullahmansour.egypt.MainActivity;
import ali.abdullahmansour.egypt.Models.Review;
import ali.abdullahmansour.egypt.Models.UserData;
import ali.abdullahmansour.egypt.R;
import ali.abdullahmansour.egypt.SignInFragment;
import ali.abdullahmansour.egypt.SignUpFragment;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment
{
    View view;

    CircleImageView profilepic;
    ImageView facebooklink,phonenumber;
    TextView title,specialty;
    Button view_profile;

    FragmentPagerAdapter fragmentPagerAdapter;
    ViewPager viewPager;
    TabLayout tabLayout;

    RatingBar ratingBar;

    Uri photoPath;

    String company_title,company_specialty,company_hotline,company_imageurl,company_address;

    RotateLoading rotateLoading;
    float rate = 0.0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        view = inflater.inflate(R.layout.profile_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        viewPager =view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);

        profilepic = view.findViewById(R.id.company_profile_picture);
        phonenumber = view.findViewById(R.id.phonenumber);
        facebooklink = view.findViewById(R.id.facebooklink_btn);
        title = view.findViewById(R.id.company_title);
        specialty = view.findViewById(R.id.company_specialty);
        view_profile = view.findViewById(R.id.view_profile_btn);
        ratingBar = view.findViewById(R.id.company_ratingbar);

        rotateLoading = view.findViewById(R.id.profilerotateloading);

        view_profile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        fragmentPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager())
        {
            private final Fragment[] mFragments = new Fragment[]
                    {
                            new CompanyToursFragment(),
                            new CompanyReviewsFragment()
                    };
            private final String[] mFragmentNames = new String[]
                    {
                            "TOURS",
                            "REVIEWS",
                    };

            @Override
            public Fragment getItem(int position)
            {
                return mFragments[position];
            }

            @Override
            public int getCount()
            {
                return mFragments.length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position)
            {
                return mFragmentNames[position];
            }
        };

        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        rotateLoading.start();

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                /*CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1,1)
                        .start(getContext(), ProfileFragment.this);*/
            }
        });

        phonenumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialContactPhone(company_hotline);
            }
        });

        facebooklink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /*FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);*/
            }
        });

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

                        company_title = userData.getTitle();
                        company_specialty = userData.getSpecialty();
                        company_hotline = userData.getHotline();

                        company_imageurl = userData.getImage_url();
                        company_address = userData.getAddress();

                        title.setText(company_title);
                        specialty.setText(company_specialty);



                        rotateLoading.stop();

                        if (company_imageurl.length() == 0 || company_address.length() == 0)
                        {
                            showCustomDialog();
                        } else
                            {
                                Picasso.get()
                                        .load(company_imageurl)
                                        .placeholder(R.drawable.travel)
                                        .error(R.drawable.travel)
                                        .into(profilepic);
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Toast.makeText(getContext(), "can\'t fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.keepSynced(true);

        databaseReference.child("reviews").child(userId).addValueEventListener(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        rate = 0;
                        // Get user value
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        {
                            Review review = snapshot.getValue(Review.class);
                            rate = rate + Float.valueOf(review.getReviewrate());
                        }
                        float rate2 = rate / dataSnapshot.getChildrenCount();
                        ratingBar.setRating(rate2);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Toast.makeText(getContext(), "can\'t fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });
    }

    private void dialContactPhone(final String phoneNumber)
    {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                if (result != null)
                {
                    photoPath = result.getUri();
                    Picasso.get()
                            .load(photoPath)
                            .placeholder(R.drawable.me)
                            .error(R.drawable.me)
                            .into(profilepic);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    private void showCustomDialog()
    {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.complete_profile_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Button cancel = dialog.findViewById(R.id.cancel_completeprofile_btn);
        Button complete = dialog.findViewById(R.id.completeprofile_btn);

        complete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
}
