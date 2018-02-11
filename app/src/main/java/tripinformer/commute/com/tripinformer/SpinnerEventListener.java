package tripinformer.commute.com.tripinformer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.concurrent.atomic.AtomicBoolean;

import Server.NetworkFragment;

/**
 * Created by anildonmez on 10.2.2018.
 */

public class SpinnerEventListener implements AdapterView.OnItemSelectedListener {

    private AtomicBoolean isInitialized;

    private int siteId;
    private int journeyDirection;

    private NetworkFragment mNetworkFragment;

    public SpinnerEventListener(int siteId, NetworkFragment mNetworkFragment) {
        this.siteId = siteId;
        this.mNetworkFragment = mNetworkFragment;
        isInitialized = new AtomicBoolean(false);
        journeyDirection = 2;

    }

    public int getJourneyDirection () { return journeyDirection; }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String itemAtPosition = (String) adapterView.getItemAtPosition(i);
        if (itemAtPosition.equalsIgnoreCase("Rissne")) {
            journeyDirection = 2;
            siteId = 9323;
        } else if (itemAtPosition.equalsIgnoreCase("Stadshagen")) {
            // towards hjulsta
            journeyDirection = 1;
            siteId = 9307;
        } else {
            siteId = -1;
            return;
        }
        if (isInitialized.get()) {
            mNetworkFragment.cancelDownload();
            Bundle args = mNetworkFragment.getArguments();
            String url = args.getString ("url_key");
            url = url.replace ("siteid=[0-9]{4}", "siteid=" + siteId);
            mNetworkFragment.getArguments().putString("url_key", url);
            mNetworkFragment.startDownload();
        } else {
            isInitialized.set(true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // do nothing
    }
}
