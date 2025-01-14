package com.example.minsal_ecosf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;

//import menuLateral.NewAdapter;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
 
import android.os.Environment;

import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import android.content.Context;
import android.database.Cursor;
//import android.location.LocationListener;
import android.location.LocationManager;

import android.widget.ImageButton;

public class MainActivity extends Activity {
 
	private MapView mapView;
	private TileCache tileCache;
	private TileRendererLayer tileRendererLayer;
	
	//---------------------Menu lateral-------------------------
	private DrawerLayout drawerLayout;
	private ExpandableListView drawerList;
	private ActionBarDrawerToggle toggle;
	ArrayList<String> groupItem = new ArrayList<String>();
	ArrayList<Object> childItem = new ArrayList<Object>();
	ArrayList<String> child;
	//Para el men� usando BD
	private DBHelper BD;
	//Para nivel 1
	private Cursor c;
	//Para sub�tems
	private Cursor c2;	

	//----------------------------------------------------------
	
	private static final String MAPFILE = "/maps/elsalvador.map";
 
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidGraphicFactory.createInstance(getApplication());
		setContentView(R.layout.activity_main);
		
		
		mapView = (MapView)findViewById(R.id.mapView);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
 
		mapView.setClickable(true);
 
		// create a tile cache of suitable size
		tileCache = AndroidUtil.createTileCache(this, "mapcache",
				mapView.getModel().displayModel.getTileSize(), 1f, 
				this.mapView.getModel().frameBufferModel.getOverdrawFactor());
 
		mapView.getModel().mapViewPosition.setZoomLevel((byte) 15);
		mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
		mapView.getMapZoomControls().setZoomLevelMax((byte) 30);
 
		// tile renderer layer using internal render theme
		tileRendererLayer = new TileRendererLayer(tileCache, this.mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
		tileRendererLayer.setMapFile(getMapFile());
		tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
 
		// only once a layer is associated with a mapView the rendering starts
		mapView.getLayerManager().getLayers().add(tileRendererLayer);

		mapView.setBuiltInZoomControls(false);
		mapView.getMapScaleBar().setVisible(true);
 
		mapView.getModel().mapViewPosition.setCenter(new LatLong(13.6801783,-89.231388));//punto inical del mapa
 
		MyMarker marker = new MyMarker(this, new LatLong(13.6801783,-89.231388), AndroidGraphicFactory.convertToBitmap(getResources().getDrawable(R.drawable.pointer)), 0, 0, mapView, "Posicion actual del equipo", true);
		mapView.getLayerManager().getLayers().add(marker);
		
		//------------------------------------------------------------------------------------
		
		//Instancio la clase del manejador de la BASE DE DATOS.
		Handler_sqlite manejador = new Handler_sqlite(this, mapView);
        
		manejador.abrir();
		manejador.insertarFicha(1,13.6801783,-89.136031);
		
		
		String[] x = manejador.leer();
		Toast.makeText(this, "Datos:" + x[0],
				Toast.LENGTH_SHORT).show();
		manejador.cerrar();

		
		
		
		//-------------------------------------Menu lateral-----------------------------------
		// Rescatamos el Action Bar y activamos el boton Home
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// Declarar e inicializar componentes para el Navigation Drawer
		//setGroupData();
		creaMenu();
		crearSubmenu();
		//setChildGroupData();	
				
		//drawer = (ListView) findViewById(R.id.drawer);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		
		drawerList = (ExpandableListView) findViewById(R.id.drawer);
		drawerList.setAdapter(new NewAdapter(this, groupItem, childItem));
		
		//Esto queda pendiente porque me est� dando problemas cuando se da click ��
		
		//drawerList.setOnChildClickListener(this);
	
		
		// Declarar adapter y eventos al hacer click
		//drawer.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, opciones));

		/*drawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			//	Toast.makeText(MainActivity.this, "Seleccion�: " + opciones[arg2], Toast.LENGTH_SHORT).show();
				drawerLayout.closeDrawers();

			}
		});*/

		// Sombra del panel Navigation Drawer
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		// Integracion boton oficial
		toggle = new ActionBarDrawerToggle(
				this, // Activity
				drawerLayout, // Panel del Navigation Drawer
				R.drawable.ic_drawer, // Icono que va a utilizar
				R.string.app_name, // Descripcion al abrir el drawer
				R.string.hello_world // Descripcion al cerrar el drawer
				){
			
					public void onDrawerClosed(View view) {
						// Drawer cerrado
						getActionBar().setTitle("Bienvenido");					
						invalidateOptionsMenu();
					}
		
					public void onDrawerOpened(View drawerView) {
						// Drawer abierto
						getActionBar().setTitle("Menu");
						invalidateOptionsMenu(); 
					}
				};

		drawerLayout.setDrawerListener(toggle);
		//---------------------------------GPS----------------------------------
		/* usando la clase LocationManager para obtener informacion GPS*/
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		final MyLocationListener mlocListener = new MyLocationListener(this, marker, mapView, mlocManager);
		//mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		//----------------------------------------------------------------------
		 if(!mlocListener.canGetLocation()){
			 mlocListener.showSettingsAlert();
		 }
		//para click del boton FAB
		ImageButton fabImageButton = (ImageButton) findViewById(R.id.fab_image_button);
		
	    fabImageButton.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	//Toast.makeText(MainActivity.this, "Click en el boton de GPS", Toast.LENGTH_SHORT).show();
	        	mlocListener.onFocusMapPosition ();
	        }
	    });
	}
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (toggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
 
	// Activamos el toggle con el icono
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		toggle.syncState();
	}
	
	private File getMapFile() {
        File file = new File(Environment.getExternalStorageDirectory(), MAPFILE);
        return file;
    }
	
	//---------------------------Menu lateral---------------------------
	
	//Para el men�
	public void creaMenu(){
		BD=new DBHelper(this);
		BD.open();		
		
		c = BD.getReadableDatabase().rawQuery("SELECT descripciondescriptor,iddescriptor " +
				"FROM descriptor, variable " +
				"WHERE variable.tipo_referente ='c' " +
				"AND descriptor.iddescriptor = variable.id_descriptor " +
				"ORDER BY descriptor.descripciondescriptor ASC", null);			
						
		try{
			if(c.moveToFirst()){
				do{
					//Armar el men�
					
					//Ac� se colocan las primeras opciones (nivel 1)
					
					if(!(c.getString(0).equals("Valores Si y No"))&&!(c.getString(0).equals("Valores Si,No y No aplica")) && !(c.getString(0).equals("Valores Si,No y No dato"))){
						groupItem.add(c.getString(0));				
					}	
					
				}while (c.moveToNext());
			
			}
		}
		catch(Exception e){		
			
		}
			
}
	
	
public void crearSubmenu(){
	BD=new DBHelper(this);
	BD.open();
	
	try{
		if(c.moveToFirst()){
			do{
							
				if(!(c.getString(0).equals("Valores Si y No"))&&!(c.getString(0).equals("Valores Si,No y No aplica")) && !(c.getString(0).equals("Valores Si,No y No dato"))){

					//Para agregar los submen�
					c2= BD.getReadableDatabase().rawQuery("SELECT descripcion " +
							"FROM descriptor, valordescriptor, variable " +
							"WHERE descriptor.iddescriptor = valordescriptor.id_descriptor " +
							"AND variable.tipo_referente ='c' " +
							"AND descriptor.iddescriptor = variable.id_descriptor " +
							"AND descriptor.iddescriptor = " + c.getString(1) +
							" ORDER BY descriptor.descripciondescriptor ASC", null);
					try{
						if(c2.moveToFirst()){
							child = new ArrayList<String>();
							do{
								child.add(c2.getString(0));
							}while(c2.moveToNext());
							childItem.add(child);
						}
					}
					catch (Exception e){
						
					}
		
				}	
				
			}while (c.moveToNext());
		
		}
	}
	catch(Exception e){		
		
	}
	
	
	
	
}
	
	
	/*public void setGroupData() {
		groupItem.add("Familia");
		groupItem.add("Miembros");
	}
	*/

// public void setChildGroupData() {
		/**
		 * Sub �tems para Familia
		 */
		/*child = new ArrayList<String>();
		child.add("Generalidades");
		child.add("Vivienda");
		child.add("Patrimonio");
		child.add("Amenazas");
		child.add("Servicios basicos");
		child.add("Desechos");
		child.add("Vectores");
		child.add("Mascotas");
		child.add("Riesgo familiar");
		childItem.add(child); */

		/**
		 * Sub �yems para Miembro
		 */
	/*
		child = new ArrayList<String>();
		child.add("Generalidades");
		child.add("Educacion");
		child.add("Economia");
		child.add("Salud");
		child.add("Habitos");
		child.add("Salud sexual");
		child.add("Otras variables");
		childItem.add(child);
	}
	*/
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
				Toast.makeText(this, "Seleccion�:" + v.getTag(),
				Toast.LENGTH_SHORT).show();
		return true;
	}
	


}
