package kindnewspaper.a20110548.ac.kr.kindnewspaper;

/**
 * Created by subin on 2016-06-05.
 */
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class NewsViewActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_SURVEY = 1001;

    private RequestQueue mQueue = null;
    private ImageLoader mImageLoader = null;

    private String title = "";
    private String content = "";
    private String author = "";
    private String link = "";
    private String answerScore = "";
String newsman_num = "";
    private JSONArray author_numJSON = null;
    WebView newsView;
    TextView textView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_main);
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB
        com.android.volley.Network network = new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        mQueue.start();

        intent = new Intent(this.getIntent());

        title = intent.getStringExtra("title");
        link = intent.getStringExtra("link");
        content = intent.getStringExtra("content");
        author = intent.getStringExtra("author");

        newsView = (WebView)findViewById(R.id.newsView);
        newsView.setWebViewClient(new WebViewClient());
        newsView.getSettings().setJavaScriptEnabled(true);
        newsView.loadUrl(link);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sIntent = new Intent(NewsViewActivity.this, SurveyActivity.class);

                startActivityForResult(sIntent,REQUEST_CODE_SURVEY);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_SURVEY){
        }
        if(resultCode == RESULT_OK ){
            String[] answer = {"","","","",""};
            answer[0] = data.getExtras().getString("Answer1");
            answer[1] = data.getExtras().getString("Answer2");
            answer[2] = data.getExtras().getString("Answer3");
            answer[3] = data.getExtras().getString("Answer4");
            answer[4] = data.getExtras().getString("Answer5");
            answerScore = Integer.toString(getAnswerTotal(answer));

            if(author != "")
            insertToAuthor(author);
            if(newsman_num != "")
            insertToDatabase(newsman_num,answer[0], answer[1] ,answer[2], answer[3], answer[4],answerScore);
            Toast.makeText(NewsViewActivity.this, author+"의 선호도가 입력되었습니다.", Toast.LENGTH_SHORT).show();

        }
    }

    //점수계산하는함수
    protected int getAnswerTotal(String[] answer){
        int tmp = 0;
        for(int i=0;i<5;i++){
            tmp += Integer.valueOf(answer[i]);
        }
        return  tmp;

    }

    protected void insertToDatabase(String newsman_num,String answer0, String answer1, String answer2, String answer3, String answer4,String total) {
        class InsertData extends AsyncTask<String, Void, String> {

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
               // Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String link = "http://192.168.43.166:80/insert_favor.php";
                    String data = URLEncoder.encode("newsman_num", "UTF-8") + "=" + Integer.valueOf(params[0]);
                    data += "&" + URLEncoder.encode("title", "UTF-8") + "=" + Integer.valueOf(params[1]);
                    data += "&" + URLEncoder.encode("title_content", "UTF-8") + "=" + Integer.valueOf(params[2]);
                    data += "&" + URLEncoder.encode("slander", "UTF-8") + "=" + Integer.valueOf(params[3]);
                    data += "&" + URLEncoder.encode("proper", "UTF-8") + "=" + Integer.valueOf(params[4]);
                    data += "&" + URLEncoder.encode("objective", "UTF-8") + "=" + Integer.valueOf(params[5]);
                    data += "&" + URLEncoder.encode("total", "UTF-8") + "=" + Integer.valueOf(params[6]);

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(newsman_num,answer0,answer1,answer2, answer3, answer4,total);
    }

    protected void getAuthorNum(String s) {
        try {
            if(s.equals("fail")){
                Toast.makeText(NewsViewActivity.this, "기자로드 실패", Toast.LENGTH_SHORT).show();
            }
            else {
                JSONObject jsonObj = new JSONObject(s);
                author_numJSON = jsonObj.getJSONArray("newsman_info");
                for(int i = 0; i < author_numJSON.length(); i++) {
                    JSONObject c = author_numJSON.getJSONObject(i);
                    newsman_num = c.getString("newsnam_num");
                    String df = c.getString("name");

                }

            }
        }catch (Exception e){

        }
    }

    protected void insertToAuthor(String author) {
        class InsertData extends AsyncTask<String, Void, String> {

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                getAuthorNum(s);
             //   Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String link = "http://192.168.43.166:80/insert_newsman.php";
                   String data = URLEncoder.encode("name", "UTF-8") + "=" + params[0];
                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(author);
    }
}