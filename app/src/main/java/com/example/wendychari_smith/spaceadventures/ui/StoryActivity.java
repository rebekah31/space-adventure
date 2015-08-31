package com.example.wendychari_smith.spaceadventures.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wendychari_smith.spaceadventures.R;
import com.example.wendychari_smith.spaceadventures.model.Page;
import com.example.wendychari_smith.spaceadventures.model.Story;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class StoryActivity extends Activity {


    public static final String TAG = StoryActivity.class.getSimpleName();

    private Story mStory = new Story();
    private ImageView mImageView;
    private TextView mTextView;
    private Button mChoice1;
    private Button mChoice2;
    private String mName;
    private Page mCurrentpage;
    private Timer t;
    private int TimeCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        // Increment the timer every second
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TimeCounter++;
                    }
                });
            }
        }, 1000, 1000);

        Intent intent = getIntent();
        mName = intent.getStringExtra(getString(R.string.key_name));

        if (mName == null) {
            mName = "Friend";
        }
        Log.d(TAG, mName);

        mImageView = (ImageView) findViewById(R.id.storyImageView);
        mTextView = (TextView) findViewById(R.id.storyTextView);
        mChoice1 = (Button) findViewById(R.id.choiceButton1);
        mChoice2 = (Button) findViewById(R.id.choiceButton2);

        loadPage(0);
    }

    private void loadPage(int choice) {
        mCurrentpage = mStory.getPage(choice);

        Drawable drawable = getResources().getDrawable(mCurrentpage.getImageId());
        //calling the image dynamically
        mImageView.setImageDrawable(drawable);

        String pageText = mCurrentpage.getText();
        pageText = String.format(pageText, mName);
        //this will add a name if place holder is included, it wont add if there is no placeholder

        mTextView.setText(pageText);

        if (mCurrentpage.isFinal()) {
            mChoice1.setVisibility(View.INVISIBLE);
            mChoice2.setText("PLAY AGAIN");
            mChoice2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Make call to Leader Board API url
                    new RestfulAPITask().execute(new String[] { "http://leaderboard.peelingpixels.com/new" });
                    // Finish action happens here
                    finish();
                }
            });
        } else {
            mChoice1.setText(mCurrentpage.getChoice1().getText());
            mChoice2.setText(mCurrentpage.getChoice2().getText());

            mChoice1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int nextPage = mCurrentpage.getChoice1().getNextPage();
                    loadPage(nextPage);
                }
            });

            mChoice2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int nextPage = mCurrentpage.getChoice2().getNextPage();
                    loadPage(nextPage);
                }
            });
        }
    }

    private class RestfulAPITask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // Cancel timer
            t.cancel();

            // Create a Key Value pair array
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // Add name, current story and time taken
            nameValuePairs.add(new BasicNameValuePair("name", mName));
            nameValuePairs.add(new BasicNameValuePair("text", mCurrentpage.getText()));
            nameValuePairs.add(new BasicNameValuePair("timeTaken", String.valueOf(TimeCounter)));

            // Create http client and set url to post to
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(params[0]);

            try {
                // Encode my post data
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Send request to the server with my post data
                HttpResponse response = client.execute(post);

                HttpEntity entity = response.getEntity();
            } catch (Exception e) {
                Log.e("ClientProtocol", "Error this was not sent to server.");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            String msg = "Leader Board submission was successful.";
            Toast.makeText(StoryActivity.this, msg, Toast.LENGTH_SHORT).show();
            Toast.makeText(StoryActivity.this, String.format("The time taken was %s seconds.", TimeCounter), Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
