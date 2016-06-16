package kindnewspaper.a20110548.ac.kr.kindnewspaper;

/**
 * Created by subin on 2016-06-05.
 */
        import android.content.Intent;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.design.widget.FloatingActionButton;
        import android.support.v7.app.AppCompatActivity;
        import android.view.View;
        import android.webkit.WebView;
        import android.webkit.WebViewClient;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.android.volley.Cache;
        import com.android.volley.RequestQueue;
        import com.android.volley.toolbox.BasicNetwork;
        import com.android.volley.toolbox.DiskBasedCache;
        import com.android.volley.toolbox.HurlStack;
        import com.android.volley.toolbox.ImageLoader;

        import java.io.BufferedReader;
        import java.io.InputStreamReader;
        import java.io.OutputStreamWriter;
        import java.net.URL;
        import java.net.URLConnection;
        import java.net.URLEncoder;

public class UserJoinActivity extends AppCompatActivity {

    EditText nametv;
    EditText idtv;
    EditText pwtv;
    EditText ponetv;

    Button joinBtn;

    String user_name;
    String user_id;
    String user_pw;
    String user_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_join);

        nametv = (EditText)findViewById(R.id.inputName);
        idtv = (EditText)findViewById(R.id.inputId);
        pwtv = (EditText)findViewById(R.id.inputPw);
        ponetv = (EditText)findViewById(R.id.inputPhone);

        joinBtn = (Button)findViewById(R.id.joinBtn);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinIntent = new Intent();

                user_name = nametv.getText().toString();
                user_id = idtv.getText().toString();
                user_pw = pwtv.getText().toString();
                user_phone = ponetv.getText().toString();

                //Toast.makeText(UserJoinActivity.this,user_name,Toast.LENGTH_SHORT).show();

                joinIntent.putExtra("UserName",user_name);
                joinIntent.putExtra("UserID",user_id);
                joinIntent.putExtra("UserPW",user_pw);
                joinIntent.putExtra("UserPhone",user_phone);
                setResult(RESULT_OK,joinIntent);
                finish();
            }
        });
    }


}