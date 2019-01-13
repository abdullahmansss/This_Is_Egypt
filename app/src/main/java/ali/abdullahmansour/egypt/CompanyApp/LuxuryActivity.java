package ali.abdullahmansour.egypt.CompanyApp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.victor.loading.rotate.RotateLoading;

import ali.abdullahmansour.egypt.Models.PlaceModel;
import ali.abdullahmansour.egypt.R;

public class LuxuryActivity extends AppCompatActivity
{
    ImageView placeimage;
    TextView placetitle,placecounter,placedescription;
    RecyclerView recyclerView;
    FloatingActionButton add_photo;
    MaterialRippleLayout fab_layout;

    public static final String EXTRA_PLACE_KEY = "place_key";
    String placekey;

    DatabaseReference mDatabase;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    ImageView photo;
    String selected_placeimaeURL = "";

    Uri photoPath;

    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<String, PhotosViewHolder> firebaseRecyclerAdapter;

    RotateLoading rotateLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_luxury);

        placeimage = findViewById(R.id.big_place_image);
        placetitle = findViewById(R.id.big_place_title);
        placecounter = findViewById(R.id.big_place_count);
        placedescription = findViewById(R.id.bid_place_description);

        recyclerView = findViewById(R.id.big_place_images_recyclerview);
        add_photo = findViewById(R.id.big_place_add_image);

        fab_layout = findViewById(R.id.fab_layout2);

        rotateLoading = findViewById(R.id.rotateloading3);

        placekey = getIntent().getStringExtra(EXTRA_PLACE_KEY);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        Getplacedata(placekey);
        SetImagesCount(placekey);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            if (user.getUid().equals("Q1rut3qvJ6PUCD5PwyNqafZVDQ03"))
            {
                fab_layout.setVisibility(View.VISIBLE);

                add_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showCustomDialog();
                    }
                });
            } else
            {
                fab_layout.setVisibility(View.GONE);
            }
        } else
            {
                fab_layout.setVisibility(View.GONE);
            }


        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        rotateLoading.start();

        Displayallplaces();
    }

    public void Getplacedata(String key)
    {
        mDatabase.child("allplaces").child(key).addListenerForSingleValueEvent(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        // Get user value
                        PlaceModel placeModel = dataSnapshot.getValue(PlaceModel.class);

                        placetitle.setText(placeModel.getPlacetitle());
                        placedescription.setText(placeModel.getPlacedescription());

                        Picasso.get()
                                .load(placeModel.getImageurl())
                                .placeholder(R.drawable.travel)
                                .into(placeimage);

                        rotateLoading.stop();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Toast.makeText(getApplicationContext(), "can\'t fetch data", Toast.LENGTH_SHORT).show();
                        //progressDialog.dismiss();
                        rotateLoading.stop();
                    }
                });
    }

    void SetImagesCount(final String placekey)
    {
        DatabaseReference databaseReference;

        databaseReference = FirebaseDatabase.getInstance().getReference().child("allphotos");

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                int imagescount = (int) dataSnapshot.child(placekey).getChildrenCount();
                placecounter.setText(imagescount + " Photos");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void showCustomDialog()
    {
        final Dialog dialog = new Dialog(LuxuryActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.add_place_photos_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Button add = dialog.findViewById(R.id.photos_add_btn);
        Button cancel = dialog.findViewById(R.id.photos_cancel_btn);

        photo = dialog.findViewById(R.id.photo);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected_placeimaeURL.length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Add a photo ...", Toast.LENGTH_SHORT).show();
                } else
                {
                    uploadImage();

                    dialog.dismiss();
                }
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1,1)
                        .start(LuxuryActivity.this);
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
                            .into(photo);

                    selected_placeimaeURL = photoPath.toString();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImage()
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
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUri = task.getResult();

                rotateLoading.stop();

                selected_placeimaeURL = downloadUri.toString();

                AddNewPhoto(selected_placeimaeURL);
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

    public void AddNewPhoto(String imageurl)
    {
        String key = databaseReference.child("allphotos").child(placekey).push().getKey();

        databaseReference.child("allphotos").child(placekey).child(key).setValue(imageurl);
    }

    private void Displayallplaces()
    {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("allphotos").child(placekey)
                .limitToLast(50);

        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(query, String.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<String, PhotosViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull PhotosViewHolder holder, int position, @NonNull String model)
            {
                rotateLoading.stop();

                final String photoKey = getRef(position).getKey();

                holder.BindPhotos(model);
            }

            @NonNull
            @Override
            public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.place_photo, parent, false);
                return new PhotosViewHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PhotosViewHolder extends RecyclerView.ViewHolder
    {
        ImageView place_image,remove;

        PhotosViewHolder(View itemView)
        {
            super(itemView);

            place_image = itemView.findViewById(R.id.place_imageeee);
            remove = itemView.findViewById(R.id.remove_photo);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null)
            {
                if (user.getUid().equals("Q1rut3qvJ6PUCD5PwyNqafZVDQ03"))
                {
                    remove.setVisibility(View.VISIBLE);
                } else
                {
                    remove.setVisibility(View.GONE);
                }
            } else
                {
                    remove.setVisibility(View.GONE);
                }

        }

        void BindPhotos(final String imageurl)
        {
            Picasso.get()
                    .load(imageurl)
                    .placeholder(R.drawable.travel)
                    .error(R.drawable.travel)
                    .into(place_image);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (firebaseRecyclerAdapter != null)
        {
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (firebaseRecyclerAdapter != null)
        {
            firebaseRecyclerAdapter.stopListening();
        }
    }
}
