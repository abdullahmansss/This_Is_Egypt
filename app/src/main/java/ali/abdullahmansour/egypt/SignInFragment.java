package ali.abdullahmansour.egypt;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.victor.loading.rotate.RotateLoading;

import static android.content.Context.MODE_PRIVATE;

public class SignInFragment extends Fragment
{
    View view;

    FirebaseAuth auth;

    EditText email,password;
    Button sign_in;
    CheckBox checkBox;
    TextView forgot;

    RotateLoading rotateLoading;

    SharedPreferences loginPreferences;
    SharedPreferences.Editor loginPrefsEditor;
    Boolean saveLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        view = inflater.inflate(R.layout.sign_in_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        email = view.findViewById(R.id.email_sign_in_field);
        password = view.findViewById(R.id.password_sign_in_field);
        sign_in = view.findViewById(R.id.sign_in_btn);
        rotateLoading = view.findViewById(R.id.signinrotateloading);
        checkBox = view.findViewById(R.id.remember_me_checkbox);
        forgot = view.findViewById(R.id.forgot_password_txt);

        loginPreferences = getActivity().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);

        if (saveLogin)
        {
            email.setText(loginPreferences.getString("username", ""));
            password.setText(loginPreferences.getString("password", ""));
            checkBox.setChecked(true);
        }

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String email_txt = email.getText().toString().trim();

                if (email_txt.length() == 0)
                {
                    Toast.makeText(getContext(), "please enter email first", Toast.LENGTH_SHORT).show();
                } else
                {
                    rotateLoading.start();

                    sentpasswordresetemail(email_txt);
                }
            }
        });

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedemail = email.getText().toString();
                String selectedpassword = password.getText().toString();

                if (selectedemail.length() == 0 || selectedpassword.length() == 0)
                {
                    Toast.makeText(getContext(), "please enter a valid data", Toast.LENGTH_SHORT).show();
                } else
                    {
                        rotateLoading.start();

                        SignIn(selectedemail,selectedpassword);

                        if (checkBox.isChecked())
                        {
                            loginPrefsEditor.putBoolean("saveLogin", true);
                            loginPrefsEditor.putString("username", selectedemail);
                            loginPrefsEditor.putString("password", selectedpassword);
                            loginPrefsEditor.apply();
                        } else {
                            loginPrefsEditor.clear();
                            loginPrefsEditor.apply();
                        }
                    }
            }
        });
    }

    public void sentpasswordresetemail(final String email)
    {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(getContext(), "password reset email sent to : " + email, Toast.LENGTH_SHORT).show();
                            rotateLoading.stop();
                        }
                    }
                });
    }

    public void SignIn(final String email, String password)
    {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.putExtra("TAG", 1);
                            startActivity(intent,
                                    ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());

                            rotateLoading.stop();
                        } else
                        {
                            Toast.makeText(getContext(), "wrong email or password", Toast.LENGTH_SHORT).show();
                            rotateLoading.stop();
                        }
                    }
                });
    }
}
