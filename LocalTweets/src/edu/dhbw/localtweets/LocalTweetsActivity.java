/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.dhbw.localtweets;


import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;


/**
 * 
 * @author tobi
 *
 */
public class LocalTweetsActivity extends MapActivity {
    public LocalTweetsActivity() {
    }
    
    private TwitterOverlay itemizedoverlay;
    private TwitterUpdater updater = null;
    private MapView mapView;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.android);
        itemizedoverlay = new TwitterOverlay(drawable, this);
        mapOverlays.add(itemizedoverlay);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	//creating this thread in oncreate will cause getlat/longspan to return zero
    	if(updater == null && mapView != null && itemizedoverlay != null) { 
    		updater = new TwitterUpdater(mapView, itemizedoverlay);
    		updater.start();
    	}
    }
    

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}

