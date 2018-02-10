package tripinformer.commute.com.tripinformer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Model.SLData;
import Model.SLDataFieldNames;
import Server.DownloadCallback;
import Server.DownloadTask;
import Server.NetworkFragment;

public class MainActivity extends FragmentActivity implements DownloadCallback {

    private TextView minLeftTextView, arrivalTimeTextView, toStationTextView;

    private int minsLeft = -1;

    private TextView mTextMessage;
    private boolean mDownloading = false;
    private NetworkFragment mNetworkFragment;

    private List<SLData> metros;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNetworkFragment = NetworkFragment.getInstance(getFragmentManager(), AppConstants.CONNECTION_URL);
        mNetworkFragment.setMCallback(this);
        startDownload();

        minLeftTextView = findViewById(R.id.minLeftText);
        arrivalTimeTextView = findViewById(R.id.arrivalTimeText);
        toStationTextView = findViewById(R.id.toStationText);

        /*mTextMessage = (TextView) findViewById(R.id.editText);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Chronometer ch = (Chronometer) findViewById(R.id.chronometer3);
        ch.setCountDown(true);
        ch.setBase(SystemClock.elapsedRealtime() + 35*60*1000);
        ch.start();*/
    }

    private void startDownload () {
        if (!mDownloading && mNetworkFragment != null) {
            mNetworkFragment.startDownload();
            mDownloading = true;
        }
    }

    @Override
    public void updateFromDownload(Object result) {
        // Should updated UI
        try {
            JSONObject jsonObject = new JSONObject((String)result);
            if (jsonObject.getInt("StatusCode") == 0) {
                JSONObject responseData = jsonObject.getJSONObject("ResponseData");
                metros = createSLDataFromJsonObject(responseData, "Kungsträdgården");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (metros.size() > 0) {
            updateActivityLabels(metros.get(0));
        }
    }

    /**
     * creates a @{@link SLData} from the given @{@link JSONObject}  and the station name
     * @param jsonObject the object to convert
     * @param stationName the station name. We only parse the data that has this atation name
     *                    when we get a response
     * @return The parsed data
     */
    private List<SLData> createSLDataFromJsonObject (JSONObject jsonObject, String stationName)
            throws JSONException {
        List<SLData> objects = new ArrayList<>();
        JSONArray metroJson = jsonObject.getJSONArray("Metros");
        if (metroJson != null) {
            for (int i = 0; i < metroJson.length(); i++) {
                JSONObject o = (JSONObject) metroJson.get(i);
                if (o.getString(SLDataFieldNames.DESTINATION).equalsIgnoreCase(stationName)) {
                    String dateObject = o.getString (SLDataFieldNames.EXPECTED_DATE_TIME);
                    // add Z so that we can parse the string. Otherwise an exception will be thrown
                    // TODO: make this better somehow. Should check if the string is already in the
                    // TODO: good form
                    dateObject += "Z";
                    LocalDateTime localDateTime =
                            LocalDateTime.ofInstant(Instant.parse(dateObject), ZoneId.systemDefault());

                    SLData data = new SLData(o.getString (SLDataFieldNames.DISPLAY_TIME),
                            o.getString (SLDataFieldNames.DESTINATION), localDateTime);
                    objects.add(data);
                }
            }
        }
        return objects;
    }

    /**
     * updates the labes in the view. So far we have {@link #minLeftTextView}, {@link #arrivalTimeTextView}
     * {@link #toStationTextView} labels that are updated.
     *
     * @param data the data to use and update the labels with
     */
    private void updateActivityLabels (SLData data) {
        LocalDateTime timeTabledDateTime = data.getTimeTabledDateTime();
        LocalDateTime curDate = LocalDateTime.now();

        minsLeft = timeTabledDateTime.getMinute() - curDate.getMinute();
        startCounter(minsLeft);
        minLeftTextView.setText("" + minsLeft);

        arrivalTimeTextView.setText(timeTabledDateTime.toString());
        toStationTextView.setText(data.getDestination());
    }

    private void startCounter (int minsLeft) {
        new CountDownTimer(minsLeft*60*1000, 1000) {

            @Override public void onTick(long l) {
                String cs = "" + l / (60*1000);
                minLeftTextView.setText(cs);
            }

            @Override public void onFinish() {
                // TODO: restart the download
            }
        }.start();
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch (progressCode) {
            // TODO: remove them maybe ? Or find a use. Would be useful when more interactions are added
            case Progress.ERROR:
                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

    @Override
    public void finishDownloading() {
        mDownloading = false;
    }
}
