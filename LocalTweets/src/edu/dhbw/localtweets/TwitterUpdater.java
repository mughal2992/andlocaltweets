package edu.dhbw.localtweets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Formatter;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

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
		//while(true) {
			GeoPoint center = mapView.getMapCenter();
			DecimalFormat df =   new DecimalFormat  ( "0.00" );
		    double minLat = (double)(center.getLatitudeE6()-(mapView.getLatitudeSpan()/2l)) / 1E6;
		    double maxLat = (double)(center.getLatitudeE6()+(mapView.getLatitudeSpan()/2l)) / 1E6;
 		    double minLng = (double)(center.getLongitudeE6()- (mapView.getLongitudeSpan()/2l) ) / 1E6;
		    double maxLng = (double)(center.getLongitudeE6()+ (mapView.getLongitudeSpan()/2l)) / 1E6;
			String location = df.format(minLat)
			+","+df.format(minLng)+","+df.format(maxLat)+","+df.format(maxLng);//twitter api: longitude/latitude pairs, separated by commas.  The first pair specifies the southwest corner of the box.
			//doRequest(location);
			doRequest("-122.75,36.8,-121.75,37.8,-74,40,-73,41");
			try {
				sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		//}
	}
	
	private void doRequest(String request) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
		    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
		        AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
		        
		        HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		        
		        if (authState.getAuthScheme() == null) {
		            AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
		            UsernamePasswordCredentials creds = new UsernamePasswordCredentials("andlocaltweets", "blablubberblubb");
		            if (creds != null) {
		                authState.setAuthScheme(new BasicScheme());
		                authState.setCredentials(creds);
		            }
		        }
		    }    
		};
		httpClient.addRequestInterceptor(preemptiveAuth, 0);
		
		HttpPost httpRequest = new HttpPost("http://stream.twitter.com/1/statuses/filter.json");
		//httpRequest.getParams().setParameter("locations",
		//		request);	
		
		try {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair("locations", request));
			UrlEncodedFormEntity reqentity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httpRequest.setEntity(reqentity);
			
			HttpResponse response;
	
		
			response = httpClient.execute(httpRequest);
		
			// TODO: HTTP-Status (z.B. 404) in eigener Anwendung verarbeiten.
		
			int status = response.getStatusLine().getStatusCode();
		
			HttpEntity entity = response.getEntity();
		
			if (entity != null) {
		
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
				//StringBuilder sb = new StringBuilder();
			
				String line = null;
				while ((line = reader.readLine()) != null) {
					//sb.append(line + "\n");
				
					//String result=sb.toString();
					try {
						JSONObject currentResult = new JSONObject(line);
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
		}catch (ClientProtocolException e) {
			
		} catch (IOException e) {
			
		} finally{
			httpRequest.abort();
		}
	}

}
