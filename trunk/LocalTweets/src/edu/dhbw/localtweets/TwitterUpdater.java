package edu.dhbw.localtweets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
/**
 * 
 * @author hudel
 *
 */
public class TwitterUpdater extends Thread {
	MapView mapView;
	TwitterOverlay twitOverlay;
	public TwitterUpdater(MapView mapview, TwitterOverlay twitOverlay) {
		this.mapView = mapview;
		this.twitOverlay = twitOverlay;
	}
	
	@Override
	public void run() {
		super.run();
		while(true) {
			GeoPoint center = mapView.getMapCenter();
			int maxSpan = Math.max(mapView.getLongitudeSpan(),mapView.getLatitudeSpan());
			//int radius = Math.sin
			String requestString = "geocode="+
				center.getLatitudeE6()*0.000001+","+
				center.getLongitudeE6()*0.000001+",1000km&rpp=100&result_type=recent";
			doRequest(requestString);
			try {
				sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void doRequest(String parameters) {
	
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://search.twitter.com/search.json?"+parameters);
		HttpResponse response;
	
		try {
			response = httpClient.execute(httpGet);
		
			// TODO: HTTP-Status (z.B. 404) in eigener Anwendung verarbeiten.
		
			int status = response.getStatusLine().getStatusCode();
		
			HttpEntity entity = response.getEntity();
		
			if (entity != null) {
		
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
				StringBuilder sb = new StringBuilder();
			
				String line = null;
				while ((line = reader.readLine()) != null)
				sb.append(line + "\n");
			
				String result=sb.toString();
				JSONObject resultSet=new JSONObject(result);  
				if(!resultSet.isNull("results")) {
					JSONArray results = resultSet.getJSONArray("results");
					
					for (int i = 0; i < results.length(); i++) {
						try {
							JSONObject currentResult = results.getJSONObject(i);
							String text = currentResult.getString("text");
							if(!currentResult.isNull("geo")) {
								JSONObject  location = currentResult.getJSONObject("geo");
								JSONArray coords = location.getJSONArray("coordinates");
								int latitude  = (int)(coords.getDouble(0)*1000000);
								int longtitude  = (int)(coords.getDouble(1)*1000000);
								GeoPoint point = new GeoPoint(latitude,longtitude);
								OverlayItem overlayitem = new OverlayItem(point, "new tweet", text);
								twitOverlay.addOverlay(overlayitem);
							}
						} catch(JSONException e) {
							
						}
					}
				}
			}
		}catch (ClientProtocolException e) {
			
		} catch (IOException e) {
			
		} catch (JSONException e) {
			e.printStackTrace();
		} finally{
			httpGet.abort();
		}
	}

}
