package com.mobileproto.lab5;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by evan on 9/25/13.
 */
public class FeedFragment extends Fragment {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        List <FeedItem> all_feeds = new ArrayList<FeedItem>();
        final View v = inflater.inflate(R.layout.feed_fragment, null);

        FeedListAdapter feedListAdapter = new FeedListAdapter(getActivity(), all_feeds);
        ListView feedList = (ListView) v.findViewById(R.id.feedList);
        feedList.setAdapter(feedListAdapter);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                new AsyncTask<Void, Void, ArrayList<FeedItem>>() {
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response;
                    InputStream inputStream = null;
                    String result = null;


                    @Override
                    protected void onPreExecute() {
                        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                    }

                    protected ArrayList<FeedItem> doInBackground(Void... voids) {
                        ArrayList<FeedItem> feeds = new ArrayList<FeedItem>();
                        try {
                            URI website = new URI("twitterproto.herokuapp.com/tweets");
                            HttpGet all_tweets = new HttpGet();
                            all_tweets.setURI(website);
                            all_tweets.setHeader("Content-type","application/json");

                            response = client.execute(all_tweets);
                            response.getStatusLine().getStatusCode();
                            HttpEntity entity = response.getEntity();

                            inputStream = entity.getContent();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),8);
                            StringBuilder sb = new StringBuilder();

                            String line;
                            String nl = System.getProperty("line.separator");
                            while ((line = reader.readLine())!= null){
                                sb.append(line + nl);
                            }
                            result = sb.toString();
                        }
                        catch (Exception e) {e.printStackTrace(); Log.e("Server", "Cannot Establish Connection");
                        }
                        finally{
                            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}}

                        try {JSONArray jArray = new JSONArray(result);
                            for (int i = 0; i < jArray.length(); i++){
                                JSONObject single_feed = jArray.getJSONObject(i);
                                FeedItem feeditem = new FeedItem(single_feed.getString("username"),single_feed.getString("tweet"));
                                feeds.add(feeditem);}
                        }catch (JSONException e){e.printStackTrace();}
                        return feeds;
                    }

                    protected void onPostExecute(ArrayList<FeedItem> all_feeds){
                        FeedListAdapter feedListAdapter = new FeedListAdapter(getActivity(), all_feeds);
                        ListView feedList = (ListView) v.findViewById(R.id.feedList);
                        feedList.setAdapter(feedListAdapter);
                    }
                }.execute();
            }
        }, 0, 10000);

        return v;

    }
}