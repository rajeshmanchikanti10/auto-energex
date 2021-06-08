package com.example.auto_energex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import Users.User;

public class signup extends AppCompatActivity {
    Button Signup;
    EditText Name_view,phonenumber_view,password_view;
    TextView VendorText_signup,notVendorText_signup;
    private ProgressDialog progressbar;
    boolean isVendor=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        progressbar=new ProgressDialog(this);
        Name_view=findViewById(R.id.signup_Name);
        phonenumber_view=findViewById(R.id.signup_PhoneNumber);
        password_view=findViewById(R.id.signup_Password);
        Signup=findViewById(R.id.signup_btn);
        VendorText_signup=findViewById(R.id.vendor_text_signup);
        notVendorText_signup=findViewById(R.id.not_vendor_text_signup);
        notVendorText_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VendorText_signup.setVisibility(View.VISIBLE);
                notVendorText_signup.setVisibility(View.INVISIBLE);
                Signup.setText("Sign Up");
                isVendor=false;

            }
        });
        VendorText_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notVendorText_signup.setVisibility(View.VISIBLE);
                VendorText_signup.setVisibility(View.INVISIBLE);
                Signup.setText("Vendor Sign Up");
                isVendor=true;
            }
        });
        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }
    void createAccount(){
        String Name=Name_view.getText().toString();
        String phonenumber=phonenumber_view.getText().toString();
        String password=password_view.getText().toString();
        if(TextUtils.isEmpty(Name))
            Name_view.setError("Name is required!");
        if(TextUtils.isEmpty(phonenumber))
            phonenumber_view.setError("Phone Number is Required!");
        if(TextUtils.isEmpty(password))
            password_view.setError("Password is required!");
        else {
            progressbar.setTitle("Sign Up");
            progressbar.setMessage("Please Wait!");
            progressbar.setCanceledOnTouchOutside(false);
            progressbar.show();

            validateAndAddUser(Name, phonenumber, password);
        }

    }
    void validateAndAddUser(String Name,String phonenumber,String password){



        DatabaseReference userdatabase;
        userdatabase=FirebaseDatabase.getInstance().getReference();
        if(isVendor){
            userdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!(snapshot.child("Vendor").child(phonenumber).exists()))
                    {
                        HashMap<String,Object> hs=new HashMap<String,Object>();
                        hs.put("Name",Name);
                        hs.put("PhoneNumber",phonenumber);
                        hs.put("password",password);
                        userdatabase.child("Vendor").child(phonenumber).setValue(hs).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    progressbar.dismiss();
                                    Toast.makeText(signup.this,"Registered Successfully!",Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(signup.this,Login.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }
                    else{
                        progressbar.dismiss();
                        Intent intent=new Intent(signup.this,Login.class);
                        startActivity(intent);
                        Toast.makeText(signup.this,"Vendor with this phonenumber already exists!",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressbar.dismiss();
                    Toast.makeText(signup.this,"DatabaseError! please try again later",Toast.LENGTH_SHORT).show();

                }
            });

        }
        else{
            userdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!(snapshot.child("user").child(phonenumber).exists())){
                        HashMap<String,Object> hs=new HashMap<String,Object>();
                        hs.put("Name",Name);
                        hs.put("PhoneNumber",phonenumber);
                        hs.put("password",password);
                        userdatabase.child("user").child(phonenumber).setValue(hs).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    progressbar.dismiss();
                                    Toast.makeText(signup.this,"Registered Successfully!",Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(signup.this,Login.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });

                    }
                    else{
                        progressbar.dismiss();
                        Toast.makeText(signup.this,"user with this phone number already exists!",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(signup.this,Login.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressbar.dismiss();
                        Toast.makeText(signup.this,"DataBase error!,Please tryagain !",Toast.LENGTH_SHORT).show();
                }
            });



        }
    }

}