package com.example.user.project3;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;

public class CamActivity extends AppCompatActivity {
    private Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        UsbCommunicationManager ucm = new UsbCommunicationManager(getApplicationContext());
        ucm.connect();

        try {
            socket = IO.socket("http://13.125.60.120:3000/appsocket");
            Log.i("socketconnect", "before");
            socket.connect();
            socket.on("direction", onMessageReceived);
            Log.i("socketconnect", "after");
        } catch (URISyntaxException e) {
            Log.i("socketconnect", "fail");
            e.printStackTrace();
        }
        Log.i("portport", "usbmanager");

        //UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbManager manager = ucm.usbManager;

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }


// Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }

// Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            byte buffer[] = new byte[16];
            int numBytesRead = port.read(buffer, 1000);
            Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();

            //Log.i("TAG", "Read " + numBytesRead + " bytes.");
        } catch (IOException e) {
            // Deal with error.
        } finally {
            try {
                port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // 전달받은 데이터는 아래와 같이 추출할 수 있습니다.
            JSONObject receivedData = (JSONObject) args[0];
            try {
                Log.i("direction", receivedData.getString("direction"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

}