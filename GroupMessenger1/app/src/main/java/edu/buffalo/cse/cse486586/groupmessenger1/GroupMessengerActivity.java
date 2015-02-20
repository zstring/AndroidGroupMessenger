package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    private static final String REMOTE_PORT0 = "11108";
    private static final String REMOTE_PORT1 = "11112";
    private static final String REMOTE_PORT2 = "11116";
    private static final String REMOTE_PORT3 = "11120";
    private static final String REMOTE_PORT4 = "11124";
    private static final int SERVER_PORT = 10000;
    private static final int TOTAL_AVDS = 5;
    private static int msgCount = -1;
    ServerTask serverTask;
    private Uri mUri;
    private GroupMessengerProvider gmProvider;
    public GroupMessengerActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        mUri = Uri.fromParts("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider", "");
        gmProvider = new GroupMessengerProvider();
        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            serverTask = new ServerTask();
            serverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException ex) {
            Log.e("Error: ", ex.getMessage());
        }

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        final EditText editText = (EditText) findViewById(R.id.editText1);
        findViewById(R.id.button4).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String msg = editText.getText().toString();
                        editText.setText("");
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg);
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            while (true) {
                try {
                    Socket clientS = serverSocket.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            clientS.getInputStream()));
                    String msg = br.readLine();
                    msgCount++;
                    ContentValues cv = new ContentValues();
                    cv.put(KEY_FIELD, msgCount + "");
                    cv.put(VALUE_FIELD, msg);
                    gmProvider.insert(mUri, cv);
                    publishProgress(msg);
                    br.close();
                    clientS.close();
                } catch (IOException ex) {
                    Log.v("Error: ", ex.getMessage());
                }
            }
//            return null;
        }

        protected void onProgressUpdate(String... strings) {
            String msg = strings[0].trim();
            TextView textView = (TextView) findViewById(R.id.textView1);
            textView.append(msgCount + "\t" + msg + "\n");
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                String msgToSend = params[0];
                for (int i = 0; i < TOTAL_AVDS; i++) {
                    String remotePort = REMOTE_PORT0;
                    if (i == 0)
                        remotePort = REMOTE_PORT0;
                    else if (i == 1)
                        remotePort = REMOTE_PORT1;
                    else if (i == 2)
                        remotePort = REMOTE_PORT2;
                    else if (i == 3)
                        remotePort = REMOTE_PORT3;
                    else if (i == 4)
                        remotePort = REMOTE_PORT4;
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os, true);
                    pw.println(msgToSend);
                    pw.close();
                    os.close();
                    socket.close();
                }
            } catch (UnknownHostException ex) {
                Log.e("Error: ", ex.getMessage());
            } catch (IOException ex) {
                Log.e("Error: ", ex.getMessage());
            }
            return null;
        }
    }
}
