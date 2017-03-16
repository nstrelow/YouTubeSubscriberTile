package de.nstrelow.youtubesubscribertile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String API_KEY_PREF = "apiKey";
    public static final String CHANNEL_ID_PREF = "channelId";
    public static final String USERNAME_PREF = "username";
    public static final String PREFERENCES_KEY = MainActivity.class.getPackage().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
    }

    private void setupUI() {
        Button saveKeyButton = (Button) findViewById(R.id.bt_save_api_key);
        final EditText apiKeyEditText = (EditText) findViewById(R.id.editText);
        apiKeyEditText.setText(getApiKey());
        saveKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apiKey = apiKeyEditText.getText().toString();
                if (apiKey.isEmpty()) {
                    Snackbar.make(findViewById(R.id.mainLayout),
                            "Please insert an API key!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences prefs = getApplicationContext()
                        .getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);

                prefs.edit().putString(API_KEY_PREF, apiKey).apply();
                Snackbar.make(findViewById(R.id.mainLayout),
                        "API Key saved!", Snackbar.LENGTH_SHORT).show();
            }
        });

        Button saveChannelButton = (Button) findViewById(R.id.bt_save_channel);
        final EditText channelEditText = (EditText) findViewById(R.id.editText2);
        channelEditText.setText(getUsername());
        saveChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = channelEditText.getText().toString();
                if (username.isEmpty()) {
                    Snackbar.make(findViewById(R.id.mainLayout),
                            "Please insert a channel name!", Snackbar.LENGTH_LONG).show();
                    return;
                }

                getChannelId(username);
            }
        });
    }

    private String getApiKey() {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(MainActivity.PREFERENCES_KEY, MODE_PRIVATE);
        return prefs.getString(MainActivity.API_KEY_PREF, "");
    }

    private String getUsername() {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(MainActivity.PREFERENCES_KEY, MODE_PRIVATE);
        return prefs.getString(MainActivity.USERNAME_PREF, "");
    }

    private void getChannelId(final String username) {
        String apiKey = getApiKey();
        if(apiKey.isEmpty()) {
            Snackbar.make(findViewById(R.id.mainLayout),
                    "You need to enter the API Key before", Snackbar.LENGTH_SHORT).show();
            return;
        }

        final String[] channelId = {""};
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.googleapis.com/youtube/v3/channels?part=id" +
                "&forUsername=" + username +
                "&key=" + apiKey;

        JsonObjectRequest stringRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String id = response
                                    .getJSONArray("items")
                                    .getJSONObject(0)
                                    .getString("id");

                            saveChannel(id,username);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(findViewById(R.id.mainLayout),
                        "Something went wrong while requesting the channel id",
                        Snackbar.LENGTH_LONG).show();
            }
        });
        queue.add(stringRequest);
    }

    private void saveChannel(String channelId, String username) {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);

        prefs.edit().putString(CHANNEL_ID_PREF, channelId).apply();
        prefs.edit().putString(USERNAME_PREF, username).apply();
        Snackbar.make(findViewById(R.id.mainLayout),
                "Channel saved!", Snackbar.LENGTH_SHORT).show();
    }
}
