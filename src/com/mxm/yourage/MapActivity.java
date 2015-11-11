package com.mxm.yourage;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.ProgressCallback;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.mxm.yourage.bean.Person;
import com.mxm.yourage.tools.FileTools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
/***
 * ��ʾ�û��������ͼ�ڵ�ͼ��
 * @author HM
 *
 */
public class MapActivity extends BaseActivity {
	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	//����
	private List<Person> list = new ArrayList<>();
	private LocationClient locationClient;
	//����
	private double lat;
	private double longt;
	private float radius;
	//��λ�¼�
	public MyLocationListenner myListener = new MyLocationListenner();
	//�Ƿ��һ�ζ�λ
	private boolean isFirstLoc = true;
	//�Ŵ���ͼƬ
	private Bitmap bmp;
	//��ʾ�ڵ�ͼ�ϵ�ͼƬ
	private BitmapDescriptor bitmap;
	private boolean isActive;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_map);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		//��ȡ�ϸ�ҳ�洫��������
		lat = getIntent().getDoubleExtra("lat", 0);
		longt = getIntent().getDoubleExtra("longt", 0);
		radius = getIntent().getFloatExtra("radius", 0);
		//���ƴ洢��ȡ����
		AVQuery<Person> query = new AVQuery<>("MFaceUser");
		query.findInBackground(new FindCallback<Person>() {

			@Override
			public void done(List<Person> avos, AVException arg1) {
				// TODO Auto-generated method stub
				if (avos != null) {
					list = avos;
					//�ɹ���ȡ����֮����ʾ�ڵ�ͼ��
					showMap();
				} else {
					arg1.printStackTrace();
					showToast(arg1.getMessage());
				}
			}
		});
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				LocationMode.NORMAL, true, null));
		// ��ͼ��ʼ��
		// ������λͼ��
		//mBaiduMap.setMyLocationEnabled(true);
		
		if (lat == 0 && longt == 0) {
			// ��λ��ʼ��
			locationClient = new LocationClient(this);
			locationClient.registerLocationListener(myListener);
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);// ��gps
			option.setCoorType("bd09ll"); // ������������
			option.setScanSpan(1000);
			locationClient.setLocOption(option);
			locationClient.start();
		} else {
			location(radius, lat, longt);
		}
		mBaiduMap.setOnMarkerClickListener(listener);
	}

	OnMarkerClickListener listener = new OnMarkerClickListener() {

		@Override
		public boolean onMarkerClick(Marker marker) {
			int inx = Integer.valueOf(marker.getTitle());
			final View v=getLayoutInflater().inflate(R.layout.view_dialog_show, null);
			TextView tv=(TextView) v.findViewById(R.id.textView);
			tv.setText("�������:"+list.get(inx).getMsg());
 			final ImageView iv=(ImageView) v.findViewById(R.id.imageView);
 			iv.setImageResource(R.anim.progress_round); 
			final AnimationDrawable animationDrawable = (AnimationDrawable) iv.getDrawable();  
			Timer timer = new Timer();  
		     timer.schedule(new TimerTask()  
		     {
		         public void run()  
		         {  
		        	 animationDrawable.start(); 
		         }
		     },300); 
 			
			 
			 
			final LatLng ll = marker.getPosition();
			Point p = mBaiduMap.getProjection().toScreenLocation(ll);
			p.y -= 47;
			LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
			final InfoWindow mInfoWindow = new InfoWindow(v, llInfo, new OnInfoWindowClickListener() {
				
				@Override
				public void onInfoWindowClick() {
					// TODO Auto-generated method stub
					mBaiduMap.hideInfoWindow();
					if(bmp!=null){
						bmp.recycle();
					}
				}
			});
			mBaiduMap.showInfoWindow(mInfoWindow);
			list.get(inx).getBit().getDataInBackground(new GetDataCallback() {
				
				@Override
				public void done(byte[] data, AVException arg1) {
					Bitmap tmp=FileTools.Bytes2Bimap(data);
					if(tmp!=null){
						int hight=320;
						int bw;
						if(tmp.getHeight()<=hight){
							hight=tmp.getHeight();
							bw=tmp.getWidth();
							bmp=tmp;
						}else{
							float f= tmp.getHeight()/hight;
							bw=(int) (tmp.getWidth()/f);
							bmp = FileTools.zoomBitmap(tmp, bw, hight);
							tmp.recycle();
						}
		  				iv.setImageBitmap(bmp);
		  				mBaiduMap.hideInfoWindow();
		  				mBaiduMap.showInfoWindow(mInfoWindow);
					}
				}
			}, null);
			return true;
		}
	};
	////��ȡ������ʾ������
	private void showMap() {
		new Thread() {
			public void run() {
				for (int i = 0; i < list.size(); i++) {
					if(!isActive){
						return;
					}
					final Person p = list.get(i);
					AVFile avf = p.getBit();
					String url=avf.getThumbnailUrl(false, 40, 40);
					
					Bitmap bmptemp = FileTools.getBitmap(url);
					if(bmptemp!=null){
					bitmap=BitmapDescriptorFactory.fromBitmap(bmptemp);
					bmptemp.recycle();
					}else{
						bitmap = BitmapDescriptorFactory
								.fromResource(R.drawable.icons);
					}
					
//					byte[] bytes = null;
//					try {
//						bytes = avf.getData();
//					} catch (AVException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					if (bytes != null) {
//
//						Bitmap bmptemp = FileTools.Bytes2Bimap(bytes);
//						float f= bmptemp.getHeight()/40;
//						int bw=(int) (bmptemp.getWidth()/f);
//						bmptemp = FileTools.zoomBitmap(bmptemp, bw, 40);
//						if (bmptemp != null) {
//							bitmap = BitmapDescriptorFactory
//									.fromBitmap(bmptemp);
//							bmptemp.recycle();
//						} else {
//							bitmap = BitmapDescriptorFactory
//									.fromResource(R.drawable.icons);
//						}
//
//					} else {
//						bitmap = BitmapDescriptorFactory
//								.fromResource(R.drawable.icons);
//					}
					// ����Maker�����
					LatLng point = new LatLng(p.getLat(), p.getLongit());
					// ����Markerͼ��

					// ����MarkerOption�������ڵ�ͼ�����Marker
					final OverlayOptions option = new MarkerOptions()
							.position(point).title(i + "").icon(bitmap);
					MapActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(mBaiduMap!=null&&option!=null){
								// �ڵ�ͼ�����Marker������ʾ
							mBaiduMap.addOverlay(option);
							}
						}
					});
				}
			};
		}.start();
		 
	}

	/**
	 * ��λSDK��������
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			System.out.println("��λ��ĵ�ַ:===" + location.getLatitude() + "  "
					+ location.getLongitude());
			// map view ���ٺ��ڴ����½��յ�λ��
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				locationClient.unRegisterLocationListener(myListener);
				locationClient.stop();

			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	private void location(float radius, double lat, double longt) {
		if (lat == 0 || mMapView == null)
			return;
		MyLocationData locData = new MyLocationData.Builder().accuracy(radius)
		// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
				.direction(100).latitude(lat).longitude(longt).build();
		mBaiduMap.setMyLocationData(locData);
		LatLng ll = new LatLng(lat, longt);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActive=false;
		// activity ��ͣʱͬʱ��ͣ��ͼ�ؼ�
		mMapView.onPause();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		// activity �ָ�ʱͬʱ�ָ���ͼ�ؼ�
		mMapView.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(bitmap!=null){
			bitmap.recycle();
		}
		// activity ����ʱͬʱ���ٵ�ͼ�ؼ�
		mMapView.onDestroy();
		
	}

	@Override
	protected void onStart() {
		isActive=true;
		// TODO Auto-generated method stub
		super.onStart();
	}
}
