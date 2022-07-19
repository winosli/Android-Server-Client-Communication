package com.demo.client;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    Thread connectionThread = null;
    EditText etIP, etPort;
    TextView tvMessages;
    EditText etMessage;
    Button btnSend;
    String SERVER_IP;
    int SERVER_PORT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIP = findViewById(R.id.etIP);
        etIP.setText("192.168.88.194"); // Change the IP
        etPort = findViewById(R.id.etPort);
        etPort.setText("8080");
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessages.setText("");
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                connectionThread = new Thread(new ConnectionThread());
                connectionThread.start();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    new Thread(new SenderThread(message)).start();
                }
            }
        });
    }
    Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;
    // This thread is for the connection only
    class ConnectionThread implements Runnable {
        @Override
        public void run() {
            try {
                clientSocket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(clientSocket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Connected\n");
                    }
                });
                new Thread(new Thread2()).start();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                // This while loop is just "waiting for data" all the time
                try {
                    if(Thread.currentThread().isInterrupted() == false) { // Check if our thread is ok
                        //    input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Take the data that come from the socket
                        final String message = input.readLine();
                        if (message != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvMessages.append("server: " + message + "\n");
                                }
                            });
                        } else {
                            connectionThread = new Thread(new ConnectionThread());
                            connectionThread.start();
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // This thread is sending messages
    class SenderThread implements Runnable {
        private String message;
        SenderThread(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            // Send the message
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream())),
                        true);
                out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

//            output.write(message);
//            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("client: " + message + "\n");
                    etMessage.setText("");
                }
            });
        }
    }
}