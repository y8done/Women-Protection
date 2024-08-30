package com.example.womenprotection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity
{

    private EditText emailtext;
    private ProgressBar progress;
    private TextView textView;
    private EditText pass;
    private Button registerbtn;;
//    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
//    private DatabaseReference myref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);
        progress = findViewById(R.id.progressbar);
        mAuth=FirebaseAuth.getInstance();
        emailtext=findViewById(R.id.email);
        pass=findViewById(R.id.password);
        registerbtn=findViewById(R.id.signupButton);
        textView=findViewById(R.id.loginText);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), login_activity.class);
                startActivity(intent);
                finish();
            }
        });
        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setVisibility(View.VISIBLE);
                String email,password;
                email=String.valueOf(emailtext.getText());
                password=String.valueOf(pass.getText());

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(SignupActivity.this, "Emter the email first!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(SignupActivity.this, "Enter a valid Password!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progress.setVisibility(View.GONE);
                                if (task.isSuccessful()) {

                                    Toast.makeText(SignupActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), login_activity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignupActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
    }
