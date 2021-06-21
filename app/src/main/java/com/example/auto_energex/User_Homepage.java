package com.example.auto_energex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import Prevalent.Prevalent;
import Users.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class User_Homepage extends AppCompatActivity {
    EditText view_power,view_hours;
    Button detailssubmission;
    BluetoothAdapter adapter;
    BluetoothDevice device;
    BluetoothSocket socket;
    public Handler messageHandler;
    user_ConnectedThread connectedThread = null;
    user_ConnectThread connectThread=null;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__homepage);
        view_power=findViewById(R.id.Ed_powerinput);
        view_hours=findViewById(R.id.Ed_hours);
        detailssubmission=findViewById(R.id.submit_InputDetails);

        /*creating adapter */
        adapter=BluetoothAdapter.getDefaultAdapter();
        /*checking if it is enabled*/
        if(!adapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        /*get device*/
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() >0)
        {
            for (BluetoothDevice mdevice : pairedDevices)
                {
            device = mdevice;
            }
        }
        progressDialog=new ProgressDialog(this);
        detailssubmission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String power=view_power.getText().toString();
                String hours=view_hours.getText().toString();
                if(TextUtils.isEmpty(power))
                    view_power.setError("Please provide power needed!");
                if(TextUtils.isEmpty(hours))
                    view_hours.setError("Please provide duration");
                else {
                    progressDialog.setTitle("Requesting Vendor");
                    progressDialog.setMessage("please wait!");
                    progressDialog.show();
                    writeOnToCloud(power, hours);
                    connectedThread.write(power.getBytes());
                    connectedThread.write(hours.getBytes());
                }
            }
        });
        messageHandler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                /*if(user_ConnectedThread.RESPONSE_MESSAGE==msg.what)
                {
                    String txt = (String)msg.obj;
                    Toast.makeText(User_Homepage.this,txt,Toast.LENGTH_SHORT).show();
                }*/
            }
        };
         connectThread=new user_ConnectThread(device,adapter);
        connectThread.start();
        socket=connectThread.pass();
    }


    void  writeOnToCloud(String power,String hours)
    {
        databaseReference= FirebaseDatabase.getInstance().getReference();
        User user= Prevalent.CurrentUser;
        HashMap<String,String> hs=new HashMap<String,String>();
        hs.put("power",power);
        hs.put("hours",hours);
        databaseReference.child("user").child(user.getName()).child(UUID.randomUUID().toString()).setValue(hs).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {   progressDialog.dismiss();
                    Toast.makeText(User_Homepage.this, "Successfully stored in the cloud", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }



            /*connect thread*/
    private class user_ConnectThread extends Thread{
        private  BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private  BluetoothAdapter adapter;
        private  final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        public user_ConnectThread(BluetoothDevice device, BluetoothAdapter madapter)
        {     BluetoothSocket tmp = null;
            mmDevice=device;
            adapter=madapter;
            try{
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch(IOException e) {
                //Toast.makeText(user_ConnectThread.this,"socket creation error!",Toast.LENGTH_SHORT).show();
            }
            mmSocket = tmp;
        }
        public BluetoothSocket pass()
        {
            return mmSocket;
        }

        @Override
        public void run() {
            super.run();
            adapter.cancelDiscovery();
            try{
                mmSocket.connect();
            }
            catch(IOException ioException){

            }
            try{
                mmSocket.close();
            }
            catch (IOException closeException)
            {

            }
            connectedThread = new user_ConnectedThread(mmSocket);
            connectedThread.start();
        }

        public void cancel() {
            try{
                mmSocket.close();
            }
            catch(IOException e) { }
        }
    }


                /*data transfer thread*/


    private   class user_ConnectedThread extends Thread{
        private BluetoothSocket mSocket;
        private InputStream mmInStream;
        private  OutputStream mmOutStream;
        public user_ConnectedThread(BluetoothSocket socket)
        {
            mSocket=socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e)
            { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            super.run();
        }

        public void write(byte []bytes)
        {
            try {
                mmOutStream.write(bytes);
                Toast.makeText(User_Homepage.this,"successfully sent ardunio",Toast.LENGTH_SHORT).show();
            }
            catch (IOException ioException){

            }
        }
    }


}