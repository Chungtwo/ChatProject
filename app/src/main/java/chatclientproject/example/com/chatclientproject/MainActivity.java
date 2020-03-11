package chatclientproject.example.com.chatclientproject;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements Runnable{

    private TextView txtshow;
    private EditText editsend;
    private Button btnsend;
    private static final String HOST="192.168.5.220";
    private static final int PORT=12345;
    private Socket socket=null;
    private BufferedReader in =null;
    private PrintWriter out=null;
    private String content="";
    private StringBuilder sb=null;


    //定义一个handler对象，用来刷新界面
    public Handler handler=new Handler(){
        public void handleMessage(Message msg){
            if(msg.what==0x123){
                sb.append(content);
                txtshow.setText(sb.toString());
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new Thread(MainActivity.this).start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sb=new StringBuilder();
        txtshow=(TextView)findViewById(R.id.txtshow);
        editsend=(EditText)findViewById(R.id.editsend);
        btnsend=(Button)findViewById(R.id.btnsend);

        //当程序一开始运行的时候就实例化Socket对象，与服务器进行连接，获取输入输出流
        //android4.0以后不能在主线程中进行网络操作，所以需要另外开辟一个线程

        new Thread(){
            public void  run(){
                try
                {
                    socket=new Socket(HOST,PORT);
                    in=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                    out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        //为发送按钮设置点击事件
        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=editsend.getText().toString();

                if(socket.isConnected()){
                    if(!socket.isOutputShutdown()){
                        out.println(msg);
                    }
                }
            }
        });

    }
    //重写run方法，在该方法中读取输入流
    @Override
    public void run() {
        try{
            socket=new Socket(HOST ,PORT);
            while (true){
                if(socket.isConnected()){
                    if (!socket.isInputShutdown()){
                        if ((content=in.readLine())!=null){
                            content+="\n";
                            handler.sendEmptyMessage(0x123);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
