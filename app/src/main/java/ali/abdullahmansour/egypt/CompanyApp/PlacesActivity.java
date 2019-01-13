package ali.abdullahmansour.egypt.CompanyApp;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import ali.abdullahmansour.egypt.CompanyApp.Fragments.ProfileFragment;
import ali.abdullahmansour.egypt.Models.PlaceModel;
import ali.abdullahmansour.egypt.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlacesActivity extends AppCompatActivity
{
    FloatingActionButton add_new_place_fab;
    MaterialRippleLayout fab_layout;

    EditText place_title,place_description;
    LinearLayout add_photo_btn;
    ImageView place_image;

    Button cancel,add;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    String selectedtitle,selectedescription;
    String selected_placeimaeURL = "";

    Uri photoPath;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<PlaceModel, PlacesViewHolder> firebaseRecyclerAdapter;

    RotateLoading rotateLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        add_new_place_fab = findViewById(R.id.add_new_place_fab);
        fab_layout = findViewById(R.id.fab_layout);
        recyclerView = findViewById(R.id.place_recyclerview);
        rotateLoading = findViewById(R.id.rotateloading);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            if (user.getUid().equals("1gMb3IE1LBdDNd3bkdrgGjRXqZd2"))
            {
                fab_layout.setVisibility(View.VISIBLE);

                add_new_place_fab.setOnClickListener(new View.OnClickListener() {
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

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        rotateLoading.start();

        Displayallplaces();
    }

    private void showCustomDialog()
    {
        final Dialog dialog = new Dialog(PlacesActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.add_place_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        add = dialog.findViewById(R.id.place_add_btn);
        cancel = dialog.findViewById(R.id.place_cancel_btn);

        place_title = dialog.findViewById(R.id.place_title_field);
        place_description = dialog.findViewById(R.id.place_description_field);

        add_photo_btn = dialog.findViewById(R.id.add_post_photo_btn);
        place_image = dialog.findViewById(R.id.post_image);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedtitle = place_title.getText().toString();
                selectedescription = place_description.getText().toString();

                if (selectedtitle.length() == 0 || selectedescription.length() == 0 || selected_placeimaeURL.length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Add a Place ...", Toast.LENGTH_SHORT).show();
                } else
                {
                    uploadImage(selectedtitle,selectedescription);

                    dialog.dismiss();
                }
            }
        });

        add_photo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1,1)
                        .start(PlacesActivity.this);
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
                    place_image.setVisibility(View.VISIBLE);

                    photoPath = result.getUri();
                    Picasso.get()
                            .load(photoPath)
                            .placeholder(R.drawable.me)
                            .error(R.drawable.me)
                            .into(place_image);

                    selected_placeimaeURL = photoPath.toString();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImage(final String title,final String desc)
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

                    AddNewPlace(selected_placeimaeURL,title,desc);
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

    public void AddNewPlace(String imageurl,String title,String description)
    {
        String key = databaseReference.child("allplaces").push().getKey();
        String key2 = databaseReference.child("allphotos").child(key).push().getKey();

        PlaceModel placeModel = new PlaceModel(imageurl,title,description);

        databaseReference.child("allplaces").child(key).setValue(placeModel);
        databaseReference.child("allphotos").child(key).child(key2).setValue(placeModel.getImageurl());
    }

    private void Displayallplaces()
    {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("allplaces")
                .limitToLast(50);

        FirebaseRecyclerOptions<PlaceModel> options =
                new FirebaseRecyclerOptions.Builder<PlaceModel>()
                        .setQuery(query, PlaceModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PlaceModel, PlacesViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull PlacesViewHolder holder, int position, @NonNull PlaceModel model)
            {
                rotateLoading.stop();

                final String placeKey = getRef(position).getKey();

                holder.BindPlaces(model);
                holder.SetImagesCount(placeKey);

                holder.place_image.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(getApplicationContext(), LuxuryActivity.class);
                        intent.putExtra(LuxuryActivity.EXTRA_PLACE_KEY, placeKey);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.place_item, parent, false);
                return new PlacesViewHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PlacesViewHolder extends RecyclerView.ViewHolder
    {
        ImageView place_image,remove_place;
        TextView place_title,place_images_count;

        DatabaseReference databaseReference;

        PlacesViewHolder(View itemView)
        {
            super(itemView);

            place_image = itemView.findViewById(R.id.placeimage);
            place_title = itemView.findViewById(R.id.placename);
            place_images_count = itemView.findViewById(R.id.placeimagescount);
            remove_place = itemView.findViewById(R.id.remove_place);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null)
            {
                if (user.getUid().equals("1gMb3IE1LBdDNd3bkdrgGjRXqZd2"))
                {
                    remove_place.setVisibility(View.VISIBLE);
                } else
                    {
                        remove_place.setVisibility(View.GONE);
                    }
            } else
            {
                remove_place.setVisibility(View.GONE);
            }

            databaseReference = FirebaseDatabase.getInstance().getReference().child("allphotos");
            databaseReference.keepSynced(true);
        }

        void BindPlaces(final PlaceModel placeModel)
        {
            place_title.setText(placeModel.getPlacetitle());
            place_images_count.setText(placeModel.getPlacetitle());

            Picasso.get()
                    .load(placeModel.getImageurl())
                    .placeholder(R.drawable.travel)
                    .error(R.drawable.travel)
                    .into(place_image);
        }

        void SetImagesCount(final String placekey)
        {
            databaseReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    int imagescount = (int) dataSnapshot.child(placekey).getChildrenCount();
                    place_images_count.setText(imagescount + " Photos");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
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
