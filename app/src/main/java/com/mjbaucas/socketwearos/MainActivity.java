package com.mjbaucas.socketwearos;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mjbaucas.socketwearos.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private ActivityMainBinding binding;

    private static int TCP_SERVER_PORT = -1;
    private static String TCP_SERVER_HOST = null;

    private String data128 = generateData(128);
    private String data256 = generateData(256);
    private String data512 = generateData(512);
    private String data1024 = generateData(1024);
    private String data = "";
    private static final String[] sizes = {"128", "256", "512", "1024"};

    private double AVERAGE_TIME = 0.0;
    private int COUNTER = 0;
    private int TIME = 60000;
    private boolean CONNECTED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sizes);
        binding.selectSize.setAdapter(adapter);
        binding.selectSize.setOnItemSelectedListener(this);

        binding.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CONNECTED == false) {
                    COUNTER = 0;
                    AVERAGE_TIME = 0.0;
                    long start = SystemClock.elapsedRealtime();

                    try {
                        TCP_SERVER_HOST = binding.hostname.getText().toString();
                        TCP_SERVER_PORT = Integer.parseInt(binding.port.getText().toString());

                        if (TCP_SERVER_HOST != null && TCP_SERVER_PORT != -1) {
                            long current = SystemClock.elapsedRealtime();
                            while (current - start < TIME){
                                current = SystemClock.elapsedRealtime();
                                COUNTER++;
                                runTCPClient();
                            }

                            String tempString = "Average Time: " + AVERAGE_TIME / COUNTER;
                            binding.averageValue.setText(tempString);

                            binding.connectButton.setText("Disconnect");
                            CONNECTED = true;
                        } else {
                            finish();
                        }
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                } else {
                    binding.connectButton.setText("Connect");
                    CONNECTED = false;
                    finish();
                }
            }
        });
    }

    private void runTCPClient(){
        try {
            long start = SystemClock.elapsedRealtime();
            Socket s = new Socket(TCP_SERVER_HOST, TCP_SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            //String outMsg = "TCP connecting to " + TCP_SERVER_HOST + ":" + TCP_SERVER_PORT + System.getProperty("line.separator");
            String outMsg = data + System.getProperty("line.separator");
            out.write(outMsg);
            out.flush();
            Log.i("TCPClient", "sent: " + outMsg);
            String inMsg = in.readLine() + System.getProperty("line.separator");
            Log.i("TCPClient", "received: " + inMsg);
            long end = SystemClock.elapsedRealtime();
            AVERAGE_TIME = AVERAGE_TIME + (end - start);
            s.close();
        } catch (Exception e) {
            String tempString = "Error: " + e.toString();
            binding.averageValue.setText(tempString);
            e.printStackTrace();
        }
    }


    private String generateData(int size) {
        String temp_string = "";
        for (int i = 0; i <= size; i++) {
            temp_string = temp_string + "x";
        }
        return temp_string;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        switch (position) {
            case 0:
                data = data128;
                break;
            case 1:
                data = data256;
                break;
            case 2:
                data = data512;
                break;
            case 3:
                data = data1024;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}