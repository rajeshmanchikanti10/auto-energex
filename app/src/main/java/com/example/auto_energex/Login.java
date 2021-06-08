package com.example.auto_energex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Users.User;

public class Login extends AppCompatActivity {
    Button signin;
    EditText pn,ps;
    TextView vendortext_signin,notvendortext_signin;
    boolean isvendor=false;
    FirebaseDatabase userdatabase;
    private ProgressDialog dialogbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dialogbar=new ProgressDialog(Login.this);
        pn=findViewById(R.id.signin_PhoneNumber);
        ps=findViewById(R.id.signin_Password);
        signin=findViewById(R.id.signin_btn);
        vendortext_signin=findViewById(R.id.vendor_text_signin);
        notvendortext_signin=findViewById(R.id.not_vendor_text_signin);
        notvendortext_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vendortext_signin.setVisibility(View.VISIBLE);
                notvendortext_signin.setVisibility(View.INVISIBLE);
                signin.setText("Sign In");
                isvendor=false;
            }
        });
        vendortext_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notvendortext_signin.setVisibility(View.VISIBLE);
                vendortext_signin.setVisibility(View.INVISIBLE);
                signin.setText("Vendor Login");
                isvendor=true;
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAndLogin();
            }
        });

    }
    void ValidateAndLogin()
    {
        String PhoneNumber=pn.getText().toString();
        String password=ps.getText().toString();
        if(TextUtils.isEmpty(PhoneNumber))
            pn.setError("This field is required!");
        if(TextUtils.isEmpty(password))
            ps.setError("This filed is required!");
        else {
            dialogbar.setTitle("Login");
            dialogbar.setMessage("Please Wait!");
            dialogbar.show();
            dialogbar.setCanceledOnTouchOutside(false);
            Log.d("1","before logging");
            Login(PhoneNumber,password);
            Log.d("2","After logging");


        }
    }
    void Login(String PhoneNumber,String password)
    {
        final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        if(isvendor) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if((snapshot.child("Vendor").child(PhoneNumber).exists()))
                    {
                        //Toast.makeText(Login.this,"Iam inside if!",Toast.LENGTH_SHORT).show();
                        try {
                            //Toast.makeText(Login.this,"Iam inside the try!",Toast.LENGTH_SHORT).show();
                            User user = snapshot.child("Vendor").child(PhoneNumber).getValue(User.class);
                            String pn = user.getPhoneNumber();
                            String ps = user.getPassword();
                            //Toast.makeText(Login.this,""+pn+" "+ps,Toast.LENGTH_SHORT).show();
                            Log.d("1","Iam before validating!");
                            if (user.getPhoneNumber().equals(PhoneNumber)) {
                                if(user.getPassword().equals(password)) {
                                    //Toast.makeText(Login.this,"After Validating!",Toast.LENGTH_SHORT).show();
                                    dialogbar.dismiss();
                                    Toast.makeText(Login.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Login.this, Vendor_HomePage.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                dialogbar.dismiss();
                                Toast.makeText(Login.this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (NullPointerException exception)
                        {   dialogbar.dismiss();
                            Toast.makeText(Login.this,"Vendor with this phone number doesn't exists!",Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dialogbar.dismiss();
                    Toast.makeText(Login.this,"Database Error!",Toast.LENGTH_SHORT).show();
                }
            });

            //Toast.makeText(Login.this,"Working for vendor!",Toast.LENGTH_SHORT).show();
        }

        else{
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if((snapshot.child("user").child(PhoneNumber).exists()))
                    {
                        try {
                            User user = snapshot.child("user").child(PhoneNumber).getValue(User.class);
                            String pn = user.getPhoneNumber();
                            String ps = user.getPassword();
                            if (pn.equals(PhoneNumber) && ps.equals(password)) {
                                dialogbar.dismiss();
                                Toast.makeText(Login.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();

                            } else {
                                dialogbar.dismiss();
                                Toast.makeText(Login.this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (NullPointerException nullPointerException)
                        {   dialogbar.dismiss();
                            Toast.makeText(Login.this,"User with this phonenumber doesn't exists!",Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dialogbar.dismiss();
                    Toast.makeText(Login.this,"Database Error!",Toast.LENGTH_SHORT).show();
                }
            });
            Toast.makeText(Login.this,"Working for user!",Toast.LENGTH_SHORT).show();

        }
        //Toast.makeText(Login.this,"Working for user !",Toast.LENGTH_SHORT).show();
    }
}