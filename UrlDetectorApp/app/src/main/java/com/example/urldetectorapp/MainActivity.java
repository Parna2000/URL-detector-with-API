package com.example.urldetectorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    EditText url;
    Button detect;
    TextView result;
    String domain = "https://url-detector-app.herokuapp.com/__contains__";
    BloomFilter<String> spam = BloomFilter.create(Funnels.stringFunnel(Charset.forName("UTF-8")),500,0.01);

    public void bloom(){
        try {
            InputStream is= getAssets().open("only_malicious.csv");
            Scanner s= new Scanner(is).useDelimiter(",");

            while (s.hasNext()){
                String result = s.next();
                spam.put(result);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url = findViewById(R.id.url);
        detect = findViewById(R.id.detect);
        result = findViewById(R.id.result);

        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hit the API --->Volley
                StringRequest stringRequest = new StringRequest(Request.Method.POST, domain,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String data = jsonObject.getString("malicious");
                                    if(data.equals("true")){
                                        result.setText("URL may be Malicious");
                                    }else{
                                        result.setText("Benign URL");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, error.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }){

                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String,String>();
                        params.put("url",url.getText().toString());
                        return params;
                    }
                };
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(stringRequest);
            }
        });
    }
}