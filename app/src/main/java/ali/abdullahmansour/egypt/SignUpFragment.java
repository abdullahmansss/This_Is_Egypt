package ali.abdullahmansour.egypt;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.victor.loading.rotate.RotateLoading;

import ali.abdullahmansour.egypt.Models.UserData;

public class SignUpFragment extends Fragment
{
    View view;
    MaterialRippleLayout company_sign_up,tourist_sign_up;

    FirebaseAuth auth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.sign_up_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        company_sign_up = view.findViewById(R.id.company_sign_up_card);
        tourist_sign_up = view.findViewById(R.id.tourist_sign_up_card);

        auth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        company_sign_up.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /*Intent intent = new Intent(getContext(), CompanySignUpActivity.class);
                startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());*/
                showCompanyDialog();
            }
        });

        tourist_sign_up.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showTouristDialog();
            }
        });
    }

    public void StoreNewUser(String title,String email,String hotline,String specialty,String category,String address,String image_url)
    {
        UserData userData = new UserData(title,email,hotline,specialty,category,address,image_url);

        databaseReference.child("allusers").child(getUid()).setValue(userData);
        databaseReference.child(category).child(getUid()).setValue(userData);
    }

    public String getUid()
    {
        return auth.getCurrentUser().getUid();
    }

    public void SignUp(final String email, String password, final String title, final String hotline, final String specialty, final String category, final String address, final String image_url)
    {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            StoreNewUser(title,email,hotline,specialty,category,address,image_url);

                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.putExtra("TAG", 1);
                            startActivity(intent,
                                    ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());

                            progressDialog.dismiss();
                        } else
                            {
                                Toast.makeText(getContext(), "this email is already signed up", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                    }
                });
    }

    public void TouristSignUp(final String email, String password, final String title, final String hotline, final String specialty, final String category, final String address, final String image_url, final String fb)
    {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            StoreNewUser(title,email,hotline,specialty,category,address,image_url);

                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.putExtra("TAG", 2);
                            startActivity(intent,
                                    ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());

                            progressDialog.dismiss();
                        } else
                        {
                            Toast.makeText(getContext(), "this email is already signed up", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void showCompanyDialog()
    {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.company_sign_up_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Button cancel = dialog.findViewById(R.id.company_sign_up_cancel_btn);
        Button signup = dialog.findViewById(R.id.company_sign_up_signup_btn);

        final EditText title = dialog.findViewById(R.id.company_title_field);
        final EditText email = dialog.findViewById(R.id.company_email_field);
        final EditText password = dialog.findViewById(R.id.company_password_field);
        final EditText hotline = dialog.findViewById(R.id.company_hotline_field);
        final EditText specialty = dialog.findViewById(R.id.company_specialty_field);

        signup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Toast.makeText(getContext(), "sign up successfully", Toast.LENGTH_SHORT).show();
                //dialog.dismiss();
                String selectedtitle = title.getText().toString();
                String selectedemail = email.getText().toString();
                String selectedpassword = password.getText().toString();
                String selectedhotline = hotline.getText().toString();
                String selectedspecialty = specialty.getText().toString();

                if (selectedtitle.length() == 0
                        || selectedemail.length() == 0
                        || selectedpassword.length() == 0
                        || selectedhotline.length() == 0
                        || selectedspecialty.length() == 0)
                {
                    Toast.makeText(getContext(), "please enter a valid data", Toast.LENGTH_SHORT).show();
                } else
                    {
                        if (selectedspecialty.length() >= 25)
                        {
                            Toast.makeText(getContext(), "please enter a short specialty", Toast.LENGTH_SHORT).show();
                        } else
                            {
                                progressDialog = new ProgressDialog(getActivity());
                                progressDialog.setMessage("Creating account ...");
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.show();
                                progressDialog.setCancelable(false);

                                SignUp(selectedemail,selectedpassword,selectedtitle,selectedhotline,selectedspecialty,"company","","");
                            }
                      }
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

    private void showTouristDialog()
    {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.tourist_sign_up_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Button cancel = dialog.findViewById(R.id.tourist_sign_up_cancel_btn);
        Button signup = dialog.findViewById(R.id.tourist_sign_up_signup_btn);

        final EditText title = dialog.findViewById(R.id.tourist_name_field);
        final EditText email = dialog.findViewById(R.id.tourist_email_field);
        final EditText password = dialog.findViewById(R.id.tourist_password_field);
        final EditText hotline = dialog.findViewById(R.id.tourist_phone_field);
        final EditText specialty = dialog.findViewById(R.id.tourist_specialty_field);

        signup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Toast.makeText(getContext(), "sign up successfully", Toast.LENGTH_SHORT).show();
                //dialog.dismiss();
                String selectedtitle = title.getText().toString();
                String selectedemail = email.getText().toString();
                String selectedpassword = password.getText().toString();
                String selectedhotline = hotline.getText().toString();
                String selectedspecialty = specialty.getText().toString();

                if (selectedtitle.length() == 0
                        || selectedemail.length() == 0
                        || selectedpassword.length() == 0
                        || selectedhotline.length() == 0
                        || selectedspecialty.length() == 0)
                {
                    Toast.makeText(getContext(), "please enter a valid data", Toast.LENGTH_SHORT).show();
                } else
                {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage("Creating account ...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    progressDialog.setCancelable(false);

                    TouristSignUp(selectedemail,selectedpassword,selectedtitle,selectedhotline,selectedspecialty,"tourist","","","");
                }
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
