package com.led.led;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.content.Context;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.*;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class ledControl extends ActionBarActivity {

    //BT HANDLER
    Handler bluetoothIn;
    final int handlerState = 0;
    private StringBuilder recDataString = new StringBuilder();
    TextView txtString;
    InputStream myInputStream;
    volatile boolean stopWorker;
    boolean cambio;
    byte[] readBuffer;
    int readBufferPosition,o;
    Thread workerThread;
    //
    Button btnDis, btnEmp;
    ImageView imgView;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    public BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        cambio=false;
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //recibimos la mac address obtenida en la actividad anterior
        setContentView(R.layout.activity_led_control);
        txtString = (TextView) findViewById(R.id.textView3);
        btnDis = (Button)findViewById(R.id.button4);
        btnEmp = (Button)findViewById(R.id.empezar);
        imgView = (ImageView) findViewById(R.id.imageView);
        new ConnectBT().execute(); //Call the class to connect

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect();
            }
        });
        btnEmp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                beginListenForData();

            }
        });
    }


    private void Disconnect()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish();

    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Conectando...", "Por favor espere");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                 myBluetooth = BluetoothAdapter.getDefaultAdapter();
                 BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//conectamos al dispositivo y chequeamos si esta disponible
                 btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                 BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                 btSocket.connect();
                 myInputStream = btSocket.getInputStream();
                }

            }
            catch (IOException e)
            {
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Conexión Fallida");
                finish();
            }
            else
            {
                msg("Conectado");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    public void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = myInputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            myInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            String[]datos=data.split("a");
                                            //txtString.setText(data);
                                            //crearPunto(100,200);
                                            crearPunto(datos[0].trim(), datos[1].trim());
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }



    private void crearPunto(String y, String yend) {
        if(cambio) {
            Double yd = Double.parseDouble(String.valueOf(y));
            int y1 = yd.intValue();
            Double yi = Double.parseDouble(String.valueOf(yend));
            int y2 = yi.intValue();
            txtString.setText(String.valueOf(y1) + " - " + String.valueOf(y2));
            Bitmap bmp = Bitmap.createBitmap(imgView.getWidth(), imgView.getHeight(), Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            imgView.draw(c);
            Paint p = new Paint();
            p.setStrokeWidth(15);
            y1*=(imgView.getHeight()/220);
            y2*=(imgView.getHeight()/220);
            c.drawLine(200, y1, 800, y2, p);
            imgView.setImageBitmap(bmp);
            cambio=false;
        }else {
            Bitmap bmp = Bitmap.createBitmap(imgView.getWidth(), imgView.getHeight(), Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            imgView.draw(c);
            Paint p = new Paint();
            p.setStrokeWidth(15);
            c.drawRGB(250, 250, 250);
            imgView.setImageBitmap(bmp);
            cambio=true;
        }
    }

}
