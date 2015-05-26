package com.example.minsal_ecosf;

import org.mapsforge.core.model.LatLong;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

/* Clase My Location Listener */
public class MyLocationListener implements LocationListener{
	
	private Context ctx;
	private MyMarker mrk;
	 
	public MyLocationListener(Context ctx, MyMarker mrk) {
		this.ctx = ctx;
		this.mrk = mrk;
	}

	@Override
	public void onLocationChanged(Location loc){
		loc.getLatitude();
		loc.getLongitude();
		mrk.setLatLong(new LatLong(loc.getLatitude(), loc.getLongitude()));
		Toast.makeText(ctx, "Lat: "+ loc.getLatitude() +"|| Long: "+ loc.getLongitude(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider){
		Toast.makeText( ctx, "Gps Disabled", Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onProviderEnabled(String provider){
		Toast.makeText( ctx, "Gps Enabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{

	}
}