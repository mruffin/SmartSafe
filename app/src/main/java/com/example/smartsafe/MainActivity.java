package com.example.smartsafe;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;


import com.google.android.material.navigation.NavigationView;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartsafe.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;


    //Bluetooth permissions and adapter to discovery mode
    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int READ_SIGNAL_PERMISSION_REQUEST = 123;
    public static final String[] BLUETOOTH_PERMISSIONS_S = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    public List<Integer> rssi_values = new ArrayList<Integer>();
    public ArrayList<BluetoothDevice> bdevices_values = new ArrayList<BluetoothDevice>();
    public List<String> full_values = new ArrayList<String>();

    //Used to measure how long the processes took
    public long startTime;
    public long endTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

 /*       StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build());*/

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());
        setContentView(R.layout.fragment_home);
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_history, R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        //----------------------------------------------------------------------------------------------

        //Setup Bluetooth Intent filters for all actions
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        //Resigster Receiver
        registerReceiver(receiver, filter);

        //Enalble Button Process... Starts Discovery
        Button enable_button = findViewById(R.id.alert);
        enable_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTime = System.currentTimeMillis();

                //This is where we would check for heartrate spikes
                Log.d("push", "There has been a spike in heart rate, enable alarm");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if ((ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) &&
                            (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

                    ) {
                        //if (!EasyPermissions.hasPermissions(MainActivity.this, BLUETOOTH_PERMISSIONS_S)) {
                        EasyPermissions.requestPermissions(MainActivity.this, "Our App Requires a permission to access your Bluetooth", READ_SIGNAL_PERMISSION_REQUEST, BLUETOOTH_PERMISSIONS_S);
                        return;
                    }
                }
                //Log.d("push", "The permission is granted");

                //cancel any prior BT device discover
                if (BTAdapter.isDiscovering()) {
                    BTAdapter.cancelDiscovery();
                }
                //re-start discovery
                BTAdapter.startDiscovery();
            }
        });

        //Button to disable alarm and quit application... Stops discovery and app altogether
        Button disable_button = findViewById(R.id.disable_button);
        disable_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //stop all activity from the previous methods
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if ((ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) &&
                            (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    ) {
                        //if (!EasyPermissions.hasPermissions(MainActivity.this, BLUETOOTH_PERMISSIONS_S)) {
                        EasyPermissions.requestPermissions(MainActivity.this, "Our App Requires a permission to access your Bluetooth", READ_SIGNAL_PERMISSION_REQUEST, BLUETOOTH_PERMISSIONS_S);
                        return;
                    }
                }
                //stop looking for devices
                if (BTAdapter.isDiscovering()) {
                    BTAdapter.cancelDiscovery();
                }
                Log.d("push", "Disabling the alarm");
                System.exit(0);
                //call disable_alarm function
                //disable_alarm(music);

            }
        });
    }


    //Reviecer gets signals from the broacaster of other devices in the area
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            int min = 0;

            //Bluetooth Permission
            if (action.equals(BluetoothDevice.ACTION_FOUND) || action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        {
                            //if (!EasyPermissions.hasPermissions(MainActivity.this, BLUETOOTH_PERMISSIONS_S)) {
                            EasyPermissions.requestPermissions(MainActivity.this, "Our App Requires a permission to access your Bluetooth", READ_SIGNAL_PERMISSION_REQUEST, BLUETOOTH_PERMISSIONS_S);
                            return;
                        }
                    }
                }

                //looking for iHome iBT810
                //B8:B3:DC:55:5E:79
                //B8:B3:DC:55:40:F0

                //Get device RSSI and create value
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String derp = device.getName() + "=>" + rssi + "dBm\n" + "=>" + device.getAddress();

                //only add the speakers with a certian name to the DeviceArray
                if (device.getName() != null && device.getName().equals("iHome iBT810")) {
                    //device list
                    bdevices_values.add(device);
                    //rssi value list
                    rssi_values.add(rssi);
                    //all values
                    full_values.add(derp);

                    //Log.d("push", String.valueOf(rssi));
                    Log.d("push", derp);
                }

                if (rssi_values.size() == 2) {
                    Log.d("push", "We have two devices");
                    //See whats in the list
                    int n = rssi_values.size();
                    for (int i = 0; i < n; i++) {
                        int rssi_value = rssi_values.get(i);
                        Log.d("push", String.valueOf(rssi_value));
                    }
                    //sort the list and grab the value closest to 0
                    Collections.sort(rssi_values);
                    min = rssi_values.get(1);
                    Log.d("push", "Min RSSI" + min);

                    //test to see if the array value has the min if so, call connectBluetooth
                    if (full_values.get(0).contains(String.valueOf(min))) {

                        //ask for the right permissions
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                {
                                    //if (!EasyPermissions.hasPermissions(MainActivity.this, BLUETOOTH_PERMISSIONS_S)) {
                                    EasyPermissions.requestPermissions(MainActivity.this, "Our App Requires a permission to access your Bluetooth", READ_SIGNAL_PERMISSION_REQUEST, BLUETOOTH_PERMISSIONS_S);
                                    return;
                                }
                            }
                        }
                        connetBluetooth(bdevices_values.get(0));
                        connetBluetooth(bdevices_values.get(1));

                    }else if (full_values.get(1).contains(String.valueOf(min))){

                        //ask for the right permissions
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                {
                                    //if (!EasyPermissions.hasPermissions(MainActivity.this, BLUETOOTH_PERMISSIONS_S)) {
                                    EasyPermissions.requestPermissions(MainActivity.this, "Our App Requires a permission to access your Bluetooth", READ_SIGNAL_PERMISSION_REQUEST, BLUETOOTH_PERMISSIONS_S);
                                    return;
                                }
                            }
                        }
                        connetBluetooth(bdevices_values.get(1));
                        connetBluetooth(bdevices_values.get(0));
                    }

                    //connetBluetooth(bdevices_values, full_values, rssi_values, min);
                    //Log.d("push", "paired");


                    if (bdevices_values.get(0).getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d("push", "true");

                    } else if (bdevices_values.get(1).getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d("push", "false");
                    } else {
                        Log.d("push", "this aint it");
                    }

                }
                //Don't forget to take out
                endTime = System.currentTimeMillis();
                Log.d("push","That took " + (endTime - startTime) + " milliseconds");
            }
        }
    };

    //public void connetBluetooth(ArrayList<BluetoothDevice> bdevices_values, List<String> full_values, List<Integer> rssi_values, Integer min) {
    public void connetBluetooth(BluetoothDevice device) {
//        AtomicReference<BluetoothDevice> first_pair = new AtomicReference<>();
//        AtomicReference<BluetoothDevice> second_pair = new AtomicReference<>();
//        if (full_values.get(0).contains(String.valueOf(min))) {
//

        // ask for the right permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    {
                        //if (!EasyPermissions.hasPermissions(MainActivity.this, BLUETOOTH_PERMISSIONS_S)) {
                        EasyPermissions.requestPermissions(MainActivity.this, "Our App Requires a permission to access your Bluetooth", READ_SIGNAL_PERMISSION_REQUEST, BLUETOOTH_PERMISSIONS_S);
                        return;
                    }
                }
            }
        device.createBond();

//            first_pair.set(bdevices_values.get(0));
//            Log.d("push", "Creating Bond between devices 1");
//            first_pair.get().createBond();

//            Log.d("push", "Creating Bond between devices 2");
//            second_pair.set(bdevices_values.get(1));
//            second_pair.get().createBond();
//            //return first_pair;
//        }else{
//
//            //ask for the right permissions
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    {
//                        //if (!EasyPermissions.hasPermissions(MainActivity.this, BLUETOOTH_PERMISSIONS_S)) {
//                        EasyPermissions.requestPermissions(MainActivity.this, "Our App Requires a permission to access your Bluetooth", READ_SIGNAL_PERMISSION_REQUEST, BLUETOOTH_PERMISSIONS_S);
//                        return;
//                    }
//                }
//            }
//
//            first_pair.set(bdevices_values.get(1));
//            first_pair.get().createBond();
//            second_pair.set(bdevices_values.get(0));
//            second_pair.get().createBond();
//            //return first_pair;
//        }
//
        MediaPlayer music = MediaPlayer.create(getApplicationContext(), R.raw.public_alarm2);
        music.start();
//
////        for (int i = 0; i < n; i++) {
////            if (full_values.get(i).contains(String.valueOf(min))) {
////
////                //ask for the right permissions
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
////                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
////                        {
////                            //if (!EasyPermissions.hasPermissions(MainActivity.this, BLUETOOTH_PERMISSIONS_S)) {
////                            EasyPermissions.requestPermissions(MainActivity.this, "Our App Requires a permission to access your Bluetooth", READ_SIGNAL_PERMISSION_REQUEST, BLUETOOTH_PERMISSIONS_S);
////                            return;
////                        }
////                    }
////                }
////
////                first_pair.set(bdevices_values.get(i));
////                first_pair.get().createBond();
////                //return first_pair;
////            }
////        }
//
        return;
    }


//    public void playAlarm(ArrayList<BluetoothDevice> bdevices_values, BluetoothDevice first_pair) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                {
//                    //if (!EasyPermissions.hasPermissions(MainActivity.this, BLUETOOTH_PERMISSIONS_S)) {
//                    EasyPermissions.requestPermissions(MainActivity.this, "Our App Requires a permission to access your Bluetooth", READ_SIGNAL_PERMISSION_REQUEST, BLUETOOTH_PERMISSIONS_S);
//                    return;
//                }
//            }
//        }
//        if (first_pair.getBondState() == BluetoothDevice.BOND_BONDED) {
//            Log.d("push", "true");
//            MediaPlayer music = MediaPlayer.create(getApplicationContext(), R.raw.public_alarm2);
//            music.start();
//        }

//    }


    //Unapplied Functions...
    public void playAlarm(MediaPlayer music){
        Log.d("push", "entersound method");
        //MediaPlayer music = MediaPlayer.create(getApplicationContext(), R.raw.public_alarm2);
        music.start();
        endTime = System.currentTimeMillis();
        Log.d("push","That took " + (endTime - startTime) + " milliseconds");

    }
    //Unapplied Functions...
    public void disable_alarm(MediaPlayer music){
        if(music.isPlaying()){
            music.stop();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {

            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(receiver);
        super.onDestroy();
        binding = null;
    }

}