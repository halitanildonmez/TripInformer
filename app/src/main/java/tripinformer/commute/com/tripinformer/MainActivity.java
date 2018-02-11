package tripinformer.commute.com.tripinformer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import Model.SLData;
import Model.SLDataFieldNames;
import Server.DownloadCallback;
import Server.NetworkFragment;

public class MainActivity extends FragmentActivity implements DownloadCallback {

    private TextView minLeftTextView, arrivalTimeTextView, toStationTextView, currentDateTimeTextView, secondsTextField;

    private boolean mDownloading = false;
    // for debugging
    private static final String TAG_NAME = "MYAPP";

    private NetworkFragment mNetworkFragment;
    private SpinnerEventListener spinnerEventListener;

    private CountDownTimer timer;
    private List<SLData> metros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String firstPart = getString(R.string.call_first_part);

        // default site is Rissne
        firstPart += "&siteid=9323&timewindow=40&bus=false&ship=false&tram=false";

        mNetworkFragment = NetworkFragment.getInstance(getFragmentManager(), firstPart);
        mNetworkFragment.setMCallback(this);

        spinnerEventListener = new SpinnerEventListener(9340, mNetworkFragment);

        startDownload();

        minLeftTextView = findViewById(R.id.minLeftText);
        arrivalTimeTextView = findViewById(R.id.arrivalTimeText);
        toStationTextView = findViewById(R.id.toStationText);

        currentDateTimeTextView = findViewById(R.id.currentDateTimeText);
        LocalDateTime curDateInstance = LocalDateTime.now().withNano(0);
        String currentDateTimeText = curDateInstance.toLocalDate() + " " +
                curDateInstance.toLocalTime();
        currentDateTimeTextView.setText(currentDateTimeText);

        secondsTextField = findViewById(R.id.secondTextField);

        Spinner stationNameSpinner = findViewById(R.id.stationSelectionSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.station_name_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        stationNameSpinner.setAdapter(adapter);
        stationNameSpinner.setOnItemSelectedListener(spinnerEventListener);
    }

    private void startDownload () {
        if (!mDownloading && mNetworkFragment != null) {
            mNetworkFragment.startDownload();
            mDownloading = true;
        }
    }

    @Override
    public void updateFromDownload(Object result) {
        //TODO: ugly, must fix
        if (result != null && result.toString().equals("C")) {
            clearAll();
        }
        // Should updated UI
        try {
            JSONObject jsonObject = new JSONObject((String)result);
            if (jsonObject.getInt("StatusCode") == 0) {
                JSONObject responseData = jsonObject.getJSONObject("ResponseData");
                metros = createSLDataFromJsonObject(responseData);
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
     * @return The parsed data
     */
    private List<SLData> createSLDataFromJsonObject (JSONObject jsonObject)
            throws JSONException {
        List<SLData> objects = new ArrayList<>();
        JSONArray metroJson = jsonObject.getJSONArray("Metros");
        if (metroJson != null) {
            for (int i = 0; i < metroJson.length(); i++) {
                JSONObject o = (JSONObject) metroJson.get(i);
                // hardcoded for blue line
                if (o.getInt("LineNumber") == 10 &&
                        spinnerEventListener.getJourneyDirection() == o.getInt("JourneyDirection")) {
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

        int minsLeft = timeTabledDateTime.getMinute() - curDate.getMinute();
        Log.d (TAG_NAME, "FROM THE CALL " + minsLeft);
        startCounter(minsLeft);

        String arrivalTimeText = timeTabledDateTime.toLocalDate().toString() + " " +
                timeTabledDateTime.toLocalTime().toString();
        arrivalTimeTextView.setText(arrivalTimeText);
        toStationTextView.setText(data.getDestination());
    }

    private void startCounter (int minsLeft) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new CountDownTimer(minsLeft*60*1000, 1000) {

            @Override public void onTick(long l) {
                String cs = "" + l / (60*1000);
                minLeftTextView.setText(cs);
                LocalDateTime nowTime = LocalDateTime.now().withNano(0);
                String updatedCurrentDateTime = nowTime.toLocalDate().toString() + " " +
                        nowTime.toLocalTime().toString();
                currentDateTimeTextView.setText(updatedCurrentDateTime);
                int seconds = (int)(l / 1000) % 60;
                String seconsTextFieldValue;
                if (seconds < 10)
                    seconsTextFieldValue = "0" + seconds;
                else
                    seconsTextFieldValue = "" + seconds;
                secondsTextField.setText(seconsTextFieldValue);
            }

            @Override public void onFinish() {
                // TODO: restart the download
                Log.d(TAG_NAME, "Ended");
            }
        };
        timer.start();
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            return connManager.getActiveNetworkInfo();
        } catch (Exception e) {
            Log.d(TAG_NAME, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
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

    public void clearAll () {
        minLeftTextView.setText("");
        arrivalTimeTextView.setText("");
        toStationTextView.setText("");
        secondsTextField.setText("");
        metros.clear();
        timer.cancel();
        timer = null;
    }
}
