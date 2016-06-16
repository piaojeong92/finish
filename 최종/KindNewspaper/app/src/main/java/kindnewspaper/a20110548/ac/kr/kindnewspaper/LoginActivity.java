package kindnewspaper.a20110548.ac.kr.kindnewspaper;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by HeeRam on 2016-06-15.
 */
public class LoginActivity extends Activity {

    private EditText editTextID;
    private EditText editTextPW;

    private JSONArray user = null;

    public static final int REQUEST_CODE_JOIN = 1001;

    private static final String TAG_INFO = "user_info";
    private static final String TAG_UNUM = "user_num";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_PNUM = "phone_num";

    String user_num = "";
    String id = "";
    String name = "";
    String phone_num = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextID = (EditText) findViewById(R.id.id);
        editTextPW = (EditText) findViewById(R.id.password);
        Button joinbtn = (Button)findViewById(R.id.join);

        joinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent jintent = new Intent(LoginActivity.this,UserJoinActivity.class);
                startActivityForResult(jintent,REQUEST_CODE_JOIN);
            }
        });
    }

    public void login_insert(View view) {
        String id = editTextID.getText().toString();
        String password = editTextPW.getText().toString();
        login_insertToDatabase(id, password);
    }

    protected void showUserInfo(String s) {
        try {
            if (s.equals("fail")) {
                Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
            } else {
                JSONObject jsonObj = new JSONObject(s);
                user = jsonObj.getJSONArray(TAG_INFO);
                for (int i = 0; i < user.length(); i++) {
                    JSONObject c = user.getJSONObject(i);
                    user_num = c.getString(TAG_UNUM);
                    id = c.getString(TAG_ID);
                    name = c.getString(TAG_NAME);
                    phone_num = c.getString(TAG_PNUM);

                }
                Intent resultIntent = new Intent();
                resultIntent.putExtra("user_num", user_num);
                resultIntent.putExtra("id", id);
                resultIntent.putExtra("name", name);
                resultIntent.putExtra("phone_num", phone_num);
                setResult(RESULT_OK,resultIntent);
                finish();
                //Intent intent = new Intent(LoginActivity.this, MainActivity.class);


                //startActivity(intent);
            }
        } catch (Exception e) {

        }
    }

    public void login_insertToDatabase(final String id, final String password) {
        class login_insertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(LoginActivity.this, "잠시 기다려주세요.", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                showUserInfo(s);
            }

            @Override
            protected String doInBackground(String... params) {
                try {

                    String id = (String) params[0];
                    String password = (String) params[1];

                    String link = "http://192.168.43.166:80/kind_login.php";
                    String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    //conn.connect();
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader((new InputStreamReader(conn.getInputStream())));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

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
        login_insertData task = new login_insertData();
        task.execute(id, password);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_JOIN){
        }
        if(resultCode == RESULT_OK ){
            String id = data.getExtras().getString("UserID");
            String password = data.getExtras().getString("UserPW");
            String name = data.getExtras().getString("UserName");
            String phone_num = data.getExtras().getString("UserPhone");

            //Toast.makeText(MainActivity.this,id,Toast.LENGTH_SHORT).show();

            insertToDatabase(id,password,name,phone_num);

        }
    }

    protected void insertToDatabase(String id, String password, String name, String phone_num) {
        class InsertData extends AsyncTask<String, Void, String> {

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String link = "http://192.168.43.166:80/kind_join.php";
                    String data = URLEncoder.encode("id", "UTF-8") + "=" + params[0];
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + params[1];
                    data += "&" + URLEncoder.encode("name", "UTF-8") + "=" + params[2];
                    data += "&" + URLEncoder.encode("phone_num", "UTF-8") + "=" + params[3];


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
        task.execute(id,password,name, phone_num);
    }
}