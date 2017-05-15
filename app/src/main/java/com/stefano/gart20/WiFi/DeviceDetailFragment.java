/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stefano.gart20.WiFi;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.stefano.gart20.R;
import com.stefano.gart20.SearchPeersActivity;
import com.stefano.gart20.WiFi.DeviceListFragment.DeviceActionListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;

    //String clientIPAddress = null;
    public static List<String> clientListIP = new ArrayList<String>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();

                        mContentView.findViewById(R.id.btn_send_ip).setEnabled(true);
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });


        mContentView.findViewById(R.id.btn_send_ip).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //Send IP Address
                        byte[] myIPAddressByte = getLocalIPAddress();
                        String myIPAddress = getDottedDecimalIP(myIPAddressByte);

                        sendIP(myIPAddress);

                        mContentView.findViewById(R.id.btn_send_ip).setEnabled(false);
                    }
                });

        mContentView.findViewById(R.id.btn_receive_ip).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //String msg_received;

                        ServerSocket socket = null;
                        try {
                            socket = new ServerSocket(1755);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Socket clientSocket = null;       //This is blocking. It will wait.
                        try {
                            clientSocket = socket.accept();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DataInputStream DIS = null;
                        try {
                            DIS = new DataInputStream(clientSocket.getInputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            //clientIPAddress = DIS.readUTF();
                            clientListIP.add(DIS.readUTF());
                            Toast.makeText(getActivity(), "IP ricevuto: " + clientListIP.get(clientListIP.size() - 1), Toast.LENGTH_LONG).show();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return mContentView;
    }


    public void sendIP(String ipAddress){
        Socket socket = null;
        try {
            socket = new Socket(info.groupOwnerAddress.getHostAddress(),1755);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataOutputStream DOS = null;
        try {
            DOS = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            DOS.writeUTF(ipAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);

        Log.d(SearchPeersActivity.TAG, "Intent----------- " + uri);
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);

     //   String clientAddress = getMacFromArpCache(device.deviceAddress);

        if(!(info.isGroupOwner == true)) {
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
            getActivity().startService(serviceIntent);
        }
        if (info.isGroupOwner == true) {
            for (int i = 0; i < clientListIP.size(); i++) {
                serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                        clientListIP.get(i));
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
                getActivity().startService(serviceIntent);
            }
        }
    }


    /*private String getLocalIpAddress() {
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }*/

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }


    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server.
        if (info.groupFormed && info.isGroupOwner) {
            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text), 1755)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text), 8988)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


            //Abilito bottone LaunchGallery anche al GroupOwner
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            //ReceiveIP
            //       mContentView.findViewById(R.id.btn_receive_ip).setVisibility(View.VISIBLE);

            //mContentView.findViewById(R.id.start_service).setVisibility(View.VISIBLE);

        } else if (info.groupFormed) {
            // Others devices act as the client. In this case, we enable the
            // get file button.
            if(clientListIP.size()<1)
                   clientListIP.add(0,info.groupOwnerAddress.getHostAddress());

            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));

            //Abilito bottone send IP Address
            mContentView.findViewById(R.id.btn_send_ip).setVisibility(View.VISIBLE);

            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text), 8988)
                    .execute();


            //Context context = getActivity();
            //SharedPreferences sharedPref = context.getSharedPreferences("P2Pinfo", Context.MODE_PRIVATE);
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            String clientIP = null;
            for(int i = 0; i < clientListIP.size(); i++) {
                clientIP.concat(clientListIP.get(i));
                if(i != clientListIP.size()-1) {
                    clientIP.concat(",");
                }
            }
            editor.putString("P2Pinfo", clientIP);
            editor.commit();

            // mContentView.findViewById(R.id.btn_send_ip).setEnabled(false);

            /*byte[] myIPAddressByte = getLocalIPAddress();
            String myIPAddress = getDottedDecimalIP(myIPAddressByte);
            sendIP(myIPAddress);*/

        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);


        //   mContentView.findViewById(R.id.btn_send_ip).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());
    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);


        mContentView.findViewById(R.id.btn_send_ip).setVisibility(View.GONE);
  //      mContentView.findViewById(R.id.btn_receive_ip).setVisibility(View.GONE);

        mContentView.findViewById(R.id.btn_send_ip).setEnabled(true);

        clientListIP.clear();

        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        private int port;

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText, int i) {
            this.context = context;
            this.statusText = (TextView) statusText;
            this.port = i;
        }

        @Override
        protected String doInBackground(Void... params) {

            if (port == 8988) {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);
                    Log.d(SearchPeersActivity.TAG, "Server: Socket opened");
                    Socket client = serverSocket.accept();
                    Log.d(SearchPeersActivity.TAG, "Server: connection done");

      // int size = client.getReceiveBufferSize();

                    final File f = new File(Environment.getExternalStorageDirectory() + "/"
                            + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                            + ".jpg");

                    File dirs = new File(f.getParent());
                    if (!dirs.exists())
                        dirs.mkdirs();
                    f.createNewFile();

                    Log.d(SearchPeersActivity.TAG, "server: copying files " + f.toString());
                    InputStream inputstream = client.getInputStream();
                    copyFile(inputstream, new FileOutputStream(f));
                    serverSocket.close();
                    return f.getAbsolutePath();
                } catch (IOException e) {
                    Log.e(SearchPeersActivity.TAG, e.getMessage());
                    return null;
                }
            }


            else {
                try {
                    ServerSocket serverSocket_ip = new ServerSocket(port);
                    Log.d(SearchPeersActivity.TAG, "Server: Socket opened");
                    Socket client = serverSocket_ip.accept();
                    Log.d(SearchPeersActivity.TAG, "Server: connection done");

                    DataInputStream DIS = null;
                    DIS = new DataInputStream(client.getInputStream());
                    clientListIP.add(DIS.readUTF());
                   // Toast.makeText(context, "IP ricevuto: " + clientListIP.get(clientListIP.size()-1), Toast.LENGTH_LONG).show();

                    serverSocket_ip.close();
                    return null;
                } catch (IOException e) {
                    Log.e(SearchPeersActivity.TAG, e.getMessage());
                    return null;
                }


            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                context.startActivity(intent);
            }

            else {
                statusText.setText("IP address received");

                Context context = getActivity();
                SharedPreferences sharedPref = context.getSharedPreferences("P2Pinfo", Context.MODE_PRIVATE);
                //SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                String clientIP = "";
                for(int i = 0; i < clientListIP.size(); i++) {
                    String ipTemp = clientListIP.get(i);
                    clientIP += ipTemp;
                    if(i != clientListIP.size()-1) {
                        clientIP.concat(",");
                    }
                }
                editor.putString("P2Pinfo", clientIP);
                editor.commit();
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }


    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        long startTime= System.currentTimeMillis();
        
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
            long endTime= System.currentTimeMillis()-startTime;
            Log.v("", "Time taken to transfer all bytes is : " + endTime);
            
        } catch (IOException e) {
            Log.d(SearchPeersActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

}
