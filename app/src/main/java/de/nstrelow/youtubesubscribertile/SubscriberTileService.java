package de.nstrelow.youtubesubscribertile;

import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

public class SubscriberTileService extends TileService {

    private String LOG_TAG = SubscriberTileService.class.getSimpleName();

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        getSubscribersCount();
    }

    private String getApiKey() {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(MainActivity.PREFERENCES_KEY, MODE_PRIVATE);
        return prefs.getString(MainActivity.API_KEY_PREF, "");
    }

    private String getChannelId() {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(MainActivity.PREFERENCES_KEY, MODE_PRIVATE);
        return prefs.getString(MainActivity.CHANNEL_ID_PREF, "");
    }

    private void getSubscribersCount() {

        String apiKey = getApiKey();
        if (apiKey.isEmpty())
            return;

        String channelId = getChannelId();
        if (channelId.isEmpty())
            return;

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.googleapis.com/youtube/v3/channels?part=statistics" +
                "&id=" + channelId +
                "&key=" + apiKey;

        final Tile tile = this.getQsTile();

        JsonObjectRequest stringRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String subscriberCount = response
                                    .getJSONArray("items")
                                    .getJSONObject(0)
                                    .getJSONObject("statistics")
                                    .getString("subscriberCount");
                            Log.d("SUBSCRIBERS", subscriberCount);
                            tile.setLabel(NumberFormat.getInstance().format(Integer.valueOf(subscriberCount)) + " subscriber");
                            tile.updateTile();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.toString());
            }
        });
        queue.add(stringRequest);
    }
}
