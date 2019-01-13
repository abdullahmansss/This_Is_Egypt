package ali.abdullahmansour.egypt.CompanyApp;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.victor.loading.rotate.RotateLoading;

import ali.abdullahmansour.egypt.CompanyApp.Fragments.ProfileFragment;
import ali.abdullahmansour.egypt.MainActivity;
import ali.abdullahmansour.egypt.Models.UserData;
import ali.abdullahmansour.egypt.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity
{
    CircleImageView edit_image;
    EditText title,email,hotline,specialty,address,facebooklink;
    Button save_changes;
    RotateLoading rotateLoading;

    String company_title,compant_email,company_hotline,company_specialty,company_address,company_facebooklink,company_imageurl;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    String selected_placeimaeURL = "";
    Uri photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        init();
        rotateLoading.start();

        returndata();

        save_changes.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v)
            {
                String selected_title = title.getText().toString();
                String selected_email = email.getText().toString();
                String selected_hotline = hotline.getText().toString();
                String selected_specialty = specialty.getText().toString();
                String selected_address = address.getText().toString();
                String selected_fb = facebooklink.getText().toString();

                if (selected_title.length() == 0
                        || selected_email.length() == 0
                        || selected_hotline.length() == 0
                        || selected_specialty.length() == 0
                        || selected_address.length() == 0
                        || selected_fb.length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "please enter a valid data", Toast.LENGTH_SHORT).show();
                } else
                    {
                        if (company_imageurl.length() == 0 || selected_placeimaeURL.length() != 0)
                        {
                            uploadImage(selected_title,selected_email,selected_hotline,selected_specialty,"company",selected_address,selected_fb);
                        } else
                            {
                                StoreNewUser(selected_title,selected_email,selected_hotline,selected_specialty,"company",selected_address,company_imageurl,selected_fb);
                            }
                    }
            }
        });
    }

    public void init()
    {
        edit_image = findViewById(R.id.edit_image);
        title = findViewById(R.id.edit_title);
        email = findViewById(R.id.edit_email);
        hotline = findViewById(R.id.edit_hotline);
        specialty = findViewById(R.id.edit_specialty);
        address = findViewById(R.id.edit_address);
        facebooklink = findViewById(R.id.edit_facebboklink);
        save_changes = findViewById(R.id.save_changes);
        rotateLoading = findViewById(R.id.editprofilerotateloading);

        edit_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1,1)
                        .start(EditProfileActivity.this);
            }
        });
    }

    public void returndata()
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

                        company_title = userData.getTitle();
                        compant_email = userData.getEmail();
                        company_specialty = userData.getSpecialty();
                        company_hotline = userData.getHotline();

                        company_imageurl = userData.getImage_url();
                        company_address = userData.getAddress();
                        company_facebooklink = userData.getFacebook_link();

                        title.setText(company_title);
                        email.setText(compant_email);
                        specialty.setText(company_specialty);
                        hotline.setText(company_hotline);
                        address.setText(company_address);
                        facebooklink.setText(company_facebooklink);

                        if (company_imageurl.length() == 0)
                        {
                            edit_image.setImageResource(R.drawable.travel);
                        } else
                            {
                                Picasso.get()
                                        .load(company_imageurl)
                                        .placeholder(R.drawable.me)
                                        .error(R.drawable.me)
                                        .into(edit_image);
                            }
                        rotateLoading.stop();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Toast.makeText(getApplicationContext(), "can\'t fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });
    }

    public void StoreNewUser(String title,String email,String hotline,String specialty,String category,String address,String image_url,String fb)
    {
        rotateLoading.start();

        UserData userData = new UserData(title,email,hotline,specialty,category,address,image_url,fb);

        databaseReference.child("allusers").child(getUid()).setValue(userData);
        databaseReference.child(category).child(getUid()).setValue(userData);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            user.updateEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                //Log.d(TAG, "User email address updated.");
                                rotateLoading.stop();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("TAG", 1);
                                startActivity(intent,
                                        ActivityOptions.makeSceneTransitionAnimation(EditProfileActivity.this).toBundle());
                            }
                        }
                    });
        }
    }

    private void uploadImage(final String title,final String email,final String hotline,final String specialty,final String category,final String address,final String Fb)
    {
        rotateLoading.start();

        UploadTask uploadTask;

        final StorageReference ref = storageReference.child("images/" + photoPath.getLastPathSegment());

        uploadTask = ref.putFile(photoPath);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUri = task.getResult();

                rotateLoading.stop();

                selected_placeimaeURL = downloadUri.toString();

                StoreNewUser(title,email,hotline,specialty,category,address,selected_placeimaeURL,Fb);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("TAG", 1);
                startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(EditProfileActivity.this).toBundle());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                // Handle unsuccessful uploads
                Toast.makeText(getApplicationContext(), "Can't Upload Photo", Toast.LENGTH_SHORT).show();
            }
        });
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
                            .into(edit_image);

                    selected_placeimaeURL = photoPath.toString();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    public String getUid()
    {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
