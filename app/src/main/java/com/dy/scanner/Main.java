package com.dy.scanner;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.BroadcastReceiver;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Main extends AppCompatActivity {

    public TextView textbox;
    public TextView check;
    public TextView incoming;
    public Spinner jobchoose;
    private Button button;
    public Receiver receiver = new Receiver();

    public int state = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getSupportActionBar().hide();
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //check = (TextView) findViewById(R.id.check);
        textbox = (TextView) findViewById(R.id.textView);
        incoming = (TextView) findViewById(R.id.incoming);
        jobchoose = (Spinner) findViewById(R.id.jobchooser);
        IntentFilter filter = new IntentFilter("android.intent.DECODE");
        registerReceiver(receiver, filter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context contest, Intent intent) {
            if(jobchoose.getSelectedItemPosition()!=0) {
                String name = intent.getExtras().getString("barcode");
                Log.w("Recevier", "接收到:" + name);
                //Toast.makeText(contest, "消息的内容是：" + name, Toast.LENGTH_SHORT).show();
                textbox.setText(name);
                int pos = jobchoose.getSelectedItemPosition();
                //check.setText(String.valueOf(pos));
                String incomingstr = post(name, String.valueOf(pos), "1");
                if(incomingstr.equals("off")&&state==1) {
                    Toast.makeText(contest, "信息提交成功，"+incoming.getText().toString()+"已下岗", Toast.LENGTH_SHORT).show();
                    incoming.setText("未授权");
                    textbox.setText("未授权");
                    state = 0;
                }
                else
                {
                    incoming.setText(incomingstr);
                    Toast.makeText(contest, "信息提交成功，"+incomingstr+"已上岗", Toast.LENGTH_SHORT).show();
                    state = 1;
                }
            }
            else
                Toast.makeText(contest, "请选择工位", Toast.LENGTH_SHORT).show();
        }
    }

    public String post(String id,String position,String button){
        String output = "error";
        try {
            StringBuilder buf = new StringBuilder();
            buf.append("id=" + URLEncoder.encode(id, "UTF-8") + "&");
            buf.append("station=" + URLEncoder.encode(position, "UTF-8") + "&");
            buf.append("button=" + URLEncoder.encode(button, "UTF-8"));
            Log.w("Post", "Send:" + buf.toString());
            byte[] data = buf.toString().getBytes("UTF-8");
            URL url = new URL("http://deng-kowalski.cn/funiture/scan.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.w("Post", "Connection opened!");
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true); //如果要输出，则必须加上此句
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            OutputStream out = conn.getOutputStream();
            Log.w("Post", "Method confirmed!");
            out.write(data);
            Log.w("Post", "已发送");
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
                output = reader.readLine();
                Log.w("Post", "Return:"+output);
            }
            else
                Toast.makeText(this, "信息提交失败，请检查网络", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Log.e("Post", e.getMessage());
            output = "error1";
        }
        return output;
    }

}