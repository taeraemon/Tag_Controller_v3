package com.example.tag;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    AlertDialog.Builder builder;
    Set<BluetoothDevice> pairedDevices;
    String[] paired_devices_name;
    String[] paired_devices_mac;
    String target_device_mac;
    int target_selected = 0;

    private enum Connected { False, Pending, True }
    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private String newline = TextUtil.newline_crlf;
    private SerialService service;
    private String receive_string = "\n";

    TextView tv_tx;
    TextView tv_rx;
    Button bt_device;
    Button bt_connect_on;
    Button bt_connect_off;
    Button bt_ctl_scan_on;
    Button bt_ctl_scan_off;
    Button bt_ctl_ssid_ram;
    Button bt_ctl_ssid_rom;
    Button bt_ctl_interval;
    Button bt_ctl_bat;
    Button bt_ctl_rtc_update;
    Button bt_ctl_reset;
    Button bt_ctl_server;
    EditText edit_ssid;
    EditText edit_interval;
    EditText edit_server;



    @SuppressLint({"MissingPermission", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_tx = (TextView)findViewById(R.id.tv_tx);
        tv_rx = (TextView)findViewById(R.id.tv_rx);

        bt_device = (Button)findViewById(R.id.bt_device);
        bt_connect_on = (Button)findViewById(R.id.bt_connect_on);
        bt_connect_off = (Button)findViewById(R.id.bt_connect_off);

        bt_ctl_scan_on = (Button)findViewById(R.id.bt_ctl_scan_on);
        bt_ctl_scan_off = (Button)findViewById(R.id.bt_ctl_scan_off);
        bt_ctl_ssid_ram = (Button)findViewById(R.id.bt_ctl_ssid_ram);
        bt_ctl_ssid_rom = (Button)findViewById(R.id.bt_ctl_ssid_rom);
        bt_ctl_interval = (Button)findViewById(R.id.bt_ctl_interval);
        bt_ctl_bat = (Button)findViewById(R.id.bt_ctl_bat);
        bt_ctl_rtc_update = (Button)findViewById(R.id.bt_ctl_rtc_update);
        bt_ctl_reset = (Button)findViewById(R.id.bt_ctl_reset);
        bt_ctl_server = (Button)findViewById(R.id.bt_ctl_server);
        edit_ssid = (EditText)findViewById(R.id.edit_ssid);
        edit_interval = (EditText)findViewById(R.id.edit_interval);
        edit_server = (EditText)findViewById(R.id.edit_server);



        String[] permission_list = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
        };
        for(String permission : permission_list){
            int chk = checkCallingOrSelfPermission(permission);
            if(chk == PackageManager.PERMISSION_DENIED){
                requestPermissions(permission_list,0);
            }
        }



        bt_device.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    paired_devices_name = new String[pairedDevices.size()];
                    paired_devices_mac = new String[pairedDevices.size()];
                    int i=0;
                    for (BluetoothDevice device : pairedDevices) {
                        paired_devices_name[i] = device.getName();
                        paired_devices_mac[i] = device.getAddress();
                        Log.d("DEBUG", device.getName() + " " + device.getAddress());
                        i++;
                    }
                }
                showDevices();
            }
        });
        bt_connect_on.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (target_selected == 0) {
                    Toast.makeText(getApplicationContext(), "먼저 기기를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
                else{
                    starter();
                }
            }
        });
        bt_connect_off.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                stopper();
            }
        });

        bt_ctl_scan_on.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(connected == Connected.True){
                    send("11");
                    tv_tx.setText("11");
                }
            }
        });
        bt_ctl_scan_off.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(connected == Connected.True){
                    send("10");
                    tv_tx.setText("10");
                }
            }
        });
        bt_ctl_ssid_ram.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(connected == Connected.True){
                    String tmp = "20";
                    tmp += edit_ssid.getText().toString();
                    send(tmp);
                    tv_tx.setText(tmp);
                }
            }
        });
        bt_ctl_ssid_rom.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(connected == Connected.True){
                    String tmp = "21";
                    tmp += edit_ssid.getText().toString();
                    send(tmp);
                    tv_tx.setText(tmp);
                }
            }
        });
        bt_ctl_interval.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String tmp = "31";
                tmp += edit_interval.getText().toString();
                send(tmp);
                tv_tx.setText(tmp);
            }
        });
        bt_ctl_bat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                send("4");
                tv_tx.setText("4");
            }
        });
        bt_ctl_rtc_update.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String tmp = "5" + (System.currentTimeMillis()/1000);
                send(tmp);
                tv_tx.setText(tmp);
            }
        });
        bt_ctl_reset.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                send("6");
                tv_tx.setText("6");
            }
        });
        bt_ctl_server.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String tmp = "7";
                tmp += edit_server.getText().toString();
                send(tmp);
                tv_tx.setText(tmp);
            }
        });
    }





    public void showDevices(){
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("기기 선택");
        builder.setItems(paired_devices_name, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int where){
                Toast.makeText(getApplicationContext(), paired_devices_name[where] + " 가 선택되었습니다.", Toast.LENGTH_SHORT).show();
                target_device_mac = paired_devices_mac[where];

                target_selected = 1;
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void starter(){
        Toast.makeText(getApplicationContext(), "연결 시도중..", Toast.LENGTH_SHORT).show();
        bindService(new Intent(getApplicationContext(), SerialService.class), connection, Context.BIND_AUTO_CREATE);
        service = null;
        startService(new Intent(getApplicationContext(), SerialService.class));
        if(initialStart && service != null){
            initialStart = false;
            connect();
        }
    }

    private void stopper(){
        Toast.makeText(getApplicationContext(), "연결 종료", Toast.LENGTH_SHORT).show();
        if(service != null){
            service.detach();
        }
        if(connected != Connected.False){
            disconnect();
        }
        stopService(new Intent(getApplicationContext(), SerialService.class));
        try{
            unbindService(connection);
        }
        catch(Exception ignored){
        }

        receive_string = "";
        tv_rx.setText(receive_string);
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(target_device_mac);
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "연결 실패", Toast.LENGTH_SHORT).show();
            disconnect();
            Log.d("ty", "onSerialConnectError");
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Log.d("tyty", "not connected");
            return;
        }
        try {
            String msg;
            byte[] data;
            msg = str;
            data = (str + newline).getBytes();
            service.write(data);
            Log.d("tyty Sent Data", msg);
        } catch (Exception e) {
            disconnect();
            Log.d("ty", "onSerialIoError");
        }
    }

    private void receive(byte[] data) {
        String msg = new String(data);
        if(newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
            msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
        }

        Log.d("tyty Received Data", msg);
        receive_string += msg;
        tv_rx.setText(receive_string);
    }

    private ServiceConnection connection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Log.d("ty", "onServiceConnected Start");
            service = ((SerialService.SerialBinder) binder).getService();
            service.attach(new SerialListener() {
                @Override
                public void onSerialConnect() {
                    connected = Connected.True;
                    Toast.makeText(getApplicationContext(), "연결 성공", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSerialConnectError(Exception e) {
                    disconnect();
                    Toast.makeText(getApplicationContext(), "연결 실패", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSerialRead(byte[] data) {
                    receive(data);
                }

                @Override
                public void onSerialIoError(Exception e) {
                    disconnect();
                    Toast.makeText(getApplicationContext(), "연결 실패", Toast.LENGTH_SHORT).show();
                }
            });
            if(initialStart) {
                initialStart = false;
                connect();
                Log.d("ty", "here");
            }
            Log.d("ty", "onServiceConnected End");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("ty", "onServiceDisconnected Start");
            service = null;
            Log.d("ty", "onServiceDisconnected End");
        }
    };

}
