package tripinformer.commute.com.tripinformer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Model.SLData;
import Model.SLDataFieldNames;
import Server.DownloadCallback;
import Server.DownloadTask;
import Server.NetworkFragment;

public class MainActivity extends FragmentActivity implements DownloadCallback {

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

        mTextMessage = (TextView) findViewById(R.id.editText);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Chronometer ch = (Chronometer) findViewById(R.id.chronometer3);
        ch.setCountDown(true);
        ch.setBase(SystemClock.elapsedRealtime() + 35*60*1000);
        ch.start();
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
                JSONArray metroJson = responseData.getJSONArray("Metros");
                if (metroJson != null) {
                    metros = new ArrayList<>(metroJson.length());
                    for (int i = 0; i < metroJson.length(); i++) {
                        JSONObject o = (JSONObject) metroJson.get(i);
                        String dateTime = o.getString(SLDataFieldNames.EXPECTED_DATE_TIME);
                        SLData data = new SLData(o.getString (SLDataFieldNames.DISPLAY_TIME),
                                o.getString (SLDataFieldNames.DESTINATION), null);
                        metros.add(data);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTextMessage.setText(metros.get(0).getDisplayData());
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
            case Progress.ERROR:
                Log.e("MyApp", "NOT CONNECFTED");
                break;
            case Progress.CONNECT_SUCCESS:
                Log.d("MyApp","Connnected");
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
