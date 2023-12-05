package br.delt.acess;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ListView usersListView;

    private Dialog dialog;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice esp32;

    BluetoothSocket socket;

    ConnectThread connectThread;

    ConnectedThread connectedThread;
    public Handler handler; // handler that gets info from Bluetooth service


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersListView = findViewById(R.id.usersListView);
        setUserAdapter();
        setOnClickListener();
        loadFromDBToMemory();
        setUpBluetooth();
    }

    private void setUpBluetooth()
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    1);
        }

        checkPermissions();

        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        bluetoothAdapter.enable();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                Log.e("ESP33",device.getName());
                if(device.getName().toString().compareTo("ESP32-BT-Slave") ==0)
                {
                    esp32 = device;
                }
            }
        }

        handler=new Handler();
        connectThread = new ConnectThread(esp32);
        connectThread.start();
    }

    private void checkPermissions(){
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission2 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                    },
                    1
            );
        }
    }

    private void loadFromDBToMemory()
    {
        SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        sqLiteManager.populateUserListArray();
    }

    private void setUserAdapter()
    {
        UsersAdapter usersAdapter = new UsersAdapter(getApplicationContext(),User.userArrayList);
        usersListView.setAdapter(usersAdapter);
    }


    private void setOnClickListener()
    {
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = (User) usersListView.getItemAtPosition(position);
                Intent editUserIntent = new Intent(getApplicationContext(), DetailActivity.class);
                editUserIntent.putExtra(User.USER_EDIT_EXTRA,selectedUser.getId());
                startActivity(editUserIntent);
            }
        });
    }


    public void newUser(View view)
    {
        //Intent newUserIntent = new Intent(this, DetailActivity.class);
        //startActivity(newUserIntent);
//        dialog = new Dialog(MainActivity.this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.dialog_new);
//
//        dialog.show();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setUserAdapter();
    }

    public void rfidFound(String rfid)
    {
        String refidKey = rfid.substring(rfid.length()-8).toString();
        Log.e("MESSAGE",refidKey);

        //SQLiteManager sqLiteManager = SQLiteManager.instanceOfDatabase(this);
        if(dialog!=null) return;
        User user = User.userArrayList.stream().filter(o -> o.getRfidKey().compareTo(refidKey) ==0).findFirst().orElse(null);
        if(user==null)
        {
            dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_new);

            TextView rfidTextView = dialog.findViewById(R.id.dialogNewRFIDTextView);
            Button addButton = dialog.findViewById(R.id.dialogNewADDButton);
            Button rejectButton = dialog.findViewById(R.id.dialogNewRejectButton);

            rfidTextView.setText(new StringBuilder().append("RFID: ").append(refidKey));


            addButton.setOnClickListener( view -> {
                Intent newUserIntent = new Intent(this, DetailActivity.class);
                newUserIntent.putExtra(User.USER_RFID_EXTRA,refidKey);
                dialog.cancel();
                dialog=null;
                startActivity(newUserIntent);
            });

            rejectButton.setOnClickListener( view -> {
                dialog.cancel();
                dialog=null;
            });


            dialog.show();



        }
        else {
            dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_user);

            //Inserir a procura das views dos fields
            ImageView image = dialog.findViewById( R.id.dialogUserImage);
            TextView nome = dialog.findViewById( R.id.dialogUserName);
            TextView cargo = dialog.findViewById( R.id.dialogUserCargo);
            TextView  lastAcess = dialog.findViewById( R.id.dialogUserLastAcess);

            Button authorizeButton = dialog.findViewById(R.id.dialogUserAcceptButton);
            Button rejectButton = dialog.findViewById(R.id.dialogUserRejectButton);

            authorizeButton.setOnClickListener(view -> {
                user.setLastAcess(SQLiteManager.getDateFromString(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date())));
                SQLiteManager sqLiteManager = new SQLiteManager(getApplicationContext());
                sqLiteManager.updateUserInDB(user);
                setUserAdapter();
                dialog.cancel();
                dialog=null;


            });

            rejectButton.setOnClickListener( view ->   {dialog.cancel();dialog=null;} );


            //Inserir o preenchimento das views dos fields
            try {
                int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                dialog.getContext().getContentResolver().takePersistableUriPermission(Uri.parse(user.getPictureURI()), flag);
                image.setImageBitmap(MediaStore.Images.Media.getBitmap(dialog.getContext().getContentResolver(), Uri.parse(user.getPictureURI())));
            } catch (IOException e) {

            }
            catch (Exception e) {}
            nome.setText(user.getNome());
            cargo.setText(user.getCargo());
            lastAcess.setText(SQLiteManager.getStringFromDate(user.getLastAcess()));


            dialog.show();

        }


    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.e("asfdsa", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("TAG", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("TAG", "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e("TAG", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("TAG", "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run() {
            mmBuffer = new byte[12];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    String id= new String(mmBuffer, StandardCharsets.UTF_8).trim();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            rfidFound(id);
                        }
                    });
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d("TAG", "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MESSAGE_WRITE, -1, -1, bytes);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e("TAG", "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("TAG", "Could not close the connect socket", e);
            }
        }
    }

}