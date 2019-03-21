package e.pc.aplication;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static e.pc.aplication.MainActivity.MY_UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private Button button, but, secbutton, rbut, lbut, cbut;
    public ArrayList<String> BTdevices = new ArrayList<String>();
    public ArrayAdapter<String> deviceadapter;
    public UUID myUUID;
    public BluetoothDevice mydevice;
    public connectedthread mconnect;
    public connectthread mconnected;
    ProgressDialog mprogressdialog;
    public BluetoothSocket mmsocket;
    public static final UUID MY_UUID =  UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static final String SERVICE_ADDRESS = "00:18:E5:03:F9:1A";
    public ArrayList<BluetoothDevice> tdevices;
    ListView lview;


    public BluetoothAdapter mbluetoothadapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        but = (Button) findViewById(R.id.button2);
        secbutton = (Button) findViewById(R.id.button3);
        mbluetoothadapt = BluetoothAdapter.getDefaultAdapter();
        rbut = (Button) findViewById(R.id.right);
        lbut = (Button) findViewById(R.id.left);
        cbut = (Button) findViewById(R.id.connect);
        lview = (ListView) findViewById(R.id.listviewforapp);
        deviceadapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, BTdevices);
        lview.setAdapter(deviceadapter);
        lview.setOnItemClickListener(MainActivity.this);
        tdevices = new ArrayList<BluetoothDevice>();


        cbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mconnected = new connectthread(mydevice, mmsocket);
                mconnected.start();
                mmsocket = mconnected.getThissocket();
            }
        });
        lbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String best = "left";
                byte[] bit;
                bit = best.getBytes();
                startconnection();
                mconnect.write(bit);
            }
        });
        rbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String best = "right";
                byte[] bit;
                bit = best.getBytes();
                startconnection();
                mconnect.write(bit);
            }
        });

        secbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Looking for unpaired devices.");
                if (mbluetoothadapt.isDiscovering()) {
                    mbluetoothadapt.cancelDiscovery();
                    Log.d(TAG, "Cancelling discovery");
                    checkBTPermission();
                    mbluetoothadapt.startDiscovery();
                    IntentFilter file = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mbrea, file);
                }
                if (!mbluetoothadapt.isDiscovering()) {
                    checkBTPermission();
                    mbluetoothadapt.startDiscovery();
                    IntentFilter file = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mbrea, file);
                }
            }
        });
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverable);

                IntentFilter inter = new IntentFilter(mbluetoothadapt.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(mbroadcast, inter);

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Enabling/diabling bluetooth");
                enabledisablebt();
            }
        });
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }



    public final BroadcastReceiver mbr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mbluetoothadapt.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mbluetoothadapt.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "State OFF");
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "State TURNING OFF");
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "State ON");
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "State TURNING ON");
                }

            }
        }
    };
    public final BroadcastReceiver mbrea = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "ACTION FOUND");
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BTdevices.add(dev.getName());
                tdevices.add(dev);
                deviceadapter.notifyDataSetChanged();
            }
        }
    };


    public final BroadcastReceiver mbroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mbluetoothadapt.ACTION_SCAN_MODE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mbluetoothadapt.ERROR);
                switch (state) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "State OFF");
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "State TURNING OFF");
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "State ON");
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "State TURNING ON");
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "State TURNING ON");
                }

            }
        }
    };
    private final BroadcastReceiver mbroadcastreceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String Action = intent.getAction();
            if (Action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mdevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mdevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    mydevice = mdevice;
                }
            }
        }
    };

    public void checkBTPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permisiioncheck = this.checkSelfPermission("MAnifest.permission.ACCESS_FINE_LOCATION");
            permisiioncheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permisiioncheck != 0)
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);

        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "called");
        super.onDestroy();
        unregisterReceiver(mbr);
        unregisterReceiver(mbrea);
        unregisterReceiver(mbroadcast);
        unregisterReceiver(mbroadcastreceiver4);
    }

    public void enabledisablebt() {
        if (mbluetoothadapt == null)
            Log.d(TAG, "This device dosen't have bluetooth capabilities");
        if (!mbluetoothadapt.isEnabled()) {
            Log.d(TAG, "Enabling Bluetooth");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            IntentFilter enableBT = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mbr, enableBT);
        }
        if (mbluetoothadapt.isEnabled()) {
            Log.d(TAG, "Disabling");
            mbluetoothadapt.disable();
            IntentFilter enableBT = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mbr, enableBT);

        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mbluetoothadapt.cancelDiscovery();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            tdevices.get(i).createBond();
            mydevice = tdevices.get(i);
        }
    }

    class connectthread extends Thread {
        private final BluetoothSocket thissocket;
        private final BluetoothDevice thisdevice;

        public connectthread(BluetoothDevice device,BluetoothSocket socket) {
            thisdevice = device;


            try {
                socket = thisdevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {

            }
            thissocket = socket;
        }

        public void run() {
            mbluetoothadapt.cancelDiscovery();
            try {
                thissocket.connect();
                Log.d(TAG, "connecting to socket");
            } catch (IOException e) {
                Log.d(TAG,"could not connect;");
                try {
                    thissocket.close();
                } catch (IOException e1) {

                }
                return;
            }

        }

        public BluetoothSocket getThissocket() {
            return thissocket;
        }

        public void cancel() {
            try {
                thissocket.close();
            } catch (IOException e) {

            }
        }
    }
    public class connectedthread extends Thread{
        private final BluetoothSocket msocket;
        private final InputStream minputstream;
        private final OutputStream moutputstream;
        public connectedthread(BluetoothSocket socket){

            InputStream tmp1 = null;
            OutputStream tmp2 = null;


            try {
                tmp1 = socket.getInputStream();
                tmp2 = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            minputstream = tmp1;
            moutputstream = tmp2;
            msocket = socket;
        }
       public void run(){
            byte []buffer = new byte [1024];
            int bytes;
            while(true){
                try{
                    bytes = minputstream.read(buffer);
                    String incomingmessage = new String(buffer,0,bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        public void write(byte [] bytes){

            try{
                moutputstream.write(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void cancel(){
            try{
                msocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
    public void write(String bytes){
        byte [] bit;
        bit  = bytes.getBytes();
        mconnect.write(bit);
    }

    public void startconnection(){
        mconnect = new connectedthread(mmsocket);
        mconnect.start();
    }

    public void start(){
        if(mconnect != null){
            mconnect.cancel();
            mconnect = null;
        }
        if(mconnected != null){
            mconnected.cancel();
            mconnected = null;
        }
    }

}