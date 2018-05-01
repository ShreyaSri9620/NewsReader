package com.example.dell.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> contentList = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    SQLiteDatabase articlesDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.list);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(),ArticleActivity.class);
                intent.putExtra("content",contentList.get(i));
                startActivity(intent);
            }
        });

        articlesDB = this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);
        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR, content VARCHAR)");

        updateListView();

        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void updateListView(){

        Cursor c = articlesDB.rawQuery("SELECT * FROM articles",null);

        int contentIndex = c.getColumnIndex("content");
        int titleIndex = c.getColumnIndex("title");

        if(c.moveToFirst()){

            titles.clear();
            contentList.clear();

            do{
                titles.add(c.getString(titleIndex));//adds title to listView
                contentList.add(c.getString(contentIndex));
            }while (c.moveToNext());

            arrayAdapter.notifyDataSetChanged();//telling the array adapter that the dataset has been changed
        }
    }
        //https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1){

                    char current = (char)data;
                    result += current;

                }
                //this will be a new JSON Array based on the result
                JSONArray jsonArray = new JSONArray(result);

                int numberOfItems = 20;
                if(jsonArray.length() < 20){

                    numberOfItems = jsonArray.length();
                }

                articlesDB.execSQL("DELETE FROM articles");//this will clear everything in our database and then add new data to it

                for (int i=0 ; i<numberOfItems; i++){

                    String articleId = jsonArray.getString(i);

                    url = new URL("https://hacker-news.firebaseio.com/v0/item/"+ articleId +".json?print=pretty");
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    in = httpURLConnection.getInputStream();
                    reader = new InputStreamReader(in);
                    data = reader.read();
                    String articleInfo = "";

                    while( data != -1 ){

                        char current = (char) data ;//casting to character from a data variable
                        articleInfo += current;
                        data = reader.read();
                    }

                    JSONObject jsonObject = new JSONObject(articleInfo);

                    if( !jsonObject.isNull("title") && !jsonObject.isNull("url")) {
                        String articleTitle = jsonObject.getString("title");
                        String articleUrl = jsonObject.getString("url");

                        url = new URL(articleUrl);
                        httpURLConnection = (HttpURLConnection) url.openConnection();
                        in = httpURLConnection.getInputStream();
                        reader = new InputStreamReader(in);
                        data = reader.read();
                        String articleContent = "";

                        while( data != -1 ){

                            char current = (char) data ;//casting to character from a data variable
                            articleContent += current;
                            data = reader.read();
                        }

                        String sql = "INSERT INTO articles table (articleId,title,content) VALUES (?, ?, ?)";
                        SQLiteStatement statement = articlesDB.compileStatement(sql);
                        statement.bindString(1,articleId);
                        statement.bindString(2,articleTitle);
                        statement.bindString(3,articleInfo);
                        statement.execute();
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        //this method is run when the process in the download task is completed
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateListView();
        }
    }

}
