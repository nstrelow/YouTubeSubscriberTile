package de.nstrelow.subscriber_count_tile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

    private TextInputLayout inputLayoutKey, inputLayoutChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
    }

    private void setupUI() {
        inputLayoutKey = (TextInputLayout) findViewById(R.id.input_layout_api_key);
        inputLayoutChannel = (TextInputLayout) findViewById(R.id.input_layout_channel);
        Button saveKeyButton = (Button) findViewById(R.id.bt_save_api_key);
        final EditText apiKeyEditText = (EditText) findViewById(R.id.editText);
        apiKeyEditText.setText(getApiKey());
        saveKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apiKey = apiKeyEditText.getText().toString();
                if (apiKey.isEmpty()) {
                    inputLayoutKey.setError(getString(R.string.err_msg_api_key));
                    return;
                }
                inputLayoutKey.setErrorEnabled(false);
                SharedPreferences prefs = getApplicationContext()
                        .getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);

                prefs.edit().putString(API_KEY_PREF, apiKey).apply();
                Snackbar.make(findViewById(R.id.mainLayout),
                        R.string.snack_api_key_saved, Snackbar.LENGTH_SHORT).show();
            }
        });

        Button apiInfoButton = (Button) findViewById(R.id.bt_info_api_key);
        apiInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showApiInfoDialog();
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
                    inputLayoutChannel.setError(getString(R.string.err_msg_channel_name));
                    return;
                }
                inputLayoutChannel.setErrorEnabled(false);
                getChannelId(username);
            }
        });
    }

    private void showApiInfoDialog() {
        final String youtubeDataUrl = "https://developers.google.com/youtube/v3/getting-started";
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder
                .setTitle(R.string.api_info_dialog_title)
                .setIcon(R.drawable.ic_info_outline)
                .setMessage(R.string.api_info_dialog_msg)
                .setPositiveButton(R.string.api_info_dialog_open_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(youtubeDataUrl));
                        startActivity(i);
                    }
                })
                .setNeutralButton(R.string.api_info_dialog_send_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, youtubeDataUrl);
                        startActivity(Intent.createChooser(sharingIntent, getString(R.string.api_info_dialog_send_button)));
                    }
                });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
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
        if (apiKey.isEmpty()) {
            inputLayoutChannel.setError(getString(R.string.err_api_key_first));
            return;
        }
        inputLayoutChannel.setErrorEnabled(false);
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

                            saveChannel(id, username);
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
