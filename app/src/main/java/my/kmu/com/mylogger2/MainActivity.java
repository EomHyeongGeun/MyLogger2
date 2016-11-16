package my.kmu.com.mylogger2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button button01, button02, resetBtn, mapBtn, statsBtn;
    MyDB mydb;
    SQLiteDatabase sqlite;
    TextView textId, textLatitude, textLongitude, textActivity;
    Intent i;
    RadioGroup radioGroup;
    RadioButton radioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button01       = (Button)findViewById(R.id.button01);
        button02       = (Button)findViewById(R.id.button02);
        resetBtn       = (Button)findViewById(R.id.resetBtn);
        mapBtn         = (Button)findViewById(R.id.mapBtn);
        statsBtn       = (Button)findViewById(R.id.statsBtn);
        textId         = (TextView)findViewById(R.id.textId);
        textLatitude   = (TextView)findViewById(R.id.textLatitude);
        textLongitude  = (TextView)findViewById(R.id.textLongitude);
        textActivity   = (TextView)findViewById(R.id.textActivity);
        radioGroup     = (RadioGroup)findViewById(R.id.radioGroup);


        // 데이터베이스 연결
        mydb = new MyDB(this);

        // 초기화
        resetBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                sqlite = mydb.getWritableDatabase();
                mydb.onUpgrade(sqlite, 1, 2);   // 1번 버전 지우고 2번 버전 만들겠다.
                sqlite.close();
            }
        });

        // 입력
        button01.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startLocationService();
            }
        });

        // 조회
        button02.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                sqlite = mydb.getReadableDatabase();
                String sql = "SELECT * FROM location";

                // 테이블을 돌아다니면서 데이터를 읽어 올 Cursor
                Cursor cursor;
                cursor = sqlite.rawQuery(sql, null);

                String _idStr         = "번호\r\n";
                String latitudeStr    = "위도\r\n";
                String longitudeStr   = "경도\r\n";
                String activityStr    = "활동\r\n";

                while(cursor.moveToNext()){ // cursor가 이동하며 각 column의 내용 읽어옴.

                    _idStr += cursor.getString(0) + "\r\n";

                    latitudeStr += cursor.getString(1) + "\r\n";

                    longitudeStr += cursor.getString(2) + "\r\n";

                    activityStr += cursor.getString(3) + "\r\n";

                }
                // Text문 변환.
                textId.setText(_idStr);
                textLatitude.setText(latitudeStr);
                textLongitude.setText(longitudeStr);
                textActivity.setText(activityStr);

                cursor.close();
                sqlite.close();
            }
        });

        // 맵 띄우기
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(i);
            }
        });

        // 통계 토스트 띄우기

        statsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sqlite = mydb.getReadableDatabase();
                String sql = "SELECT * FROM location";

                Cursor cursor;
                cursor = sqlite.rawQuery(sql, null);

                // 각 활동들의 갯수를 받아오기 위한
                int act1=0, act2=0, act3=0, act4=0, act5=0, act6=0;

                String activityCheck;

                while(cursor.moveToNext()){

                    activityCheck = cursor.getString(3);
                    if(activityCheck=="Cafe"){
                        act1++;
                    }
                    else if(activityCheck=="Exercise"){
                        act2++;
                    }
                    else if(activityCheck=="Study"){
                        act3++;
                    }
                    else if(activityCheck=="Eat"){
                        act4++;
                    }
                    else if(activityCheck=="Rest"){
                        act5++;
                    }
                    else if(activityCheck=="Etc"){
                        act6++;
                    }
                }
                Toast.makeText(getApplicationContext(), "카페:"+act1+"회, 운동:"+act2+"회, 공부:"+act3+"회\n" +
                        "식사:"+act4+"회, 휴식:"+act5+"회, 기타:"+act6+"회", Toast.LENGTH_LONG).show();
            }
        });


        checkDangerousPermissions();
    }


    private void checkDangerousPermissions(){
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for(int i=0; i<permissions.length; i++){
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                break;
            }
        }

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "권한 있음.", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "권한 없음.", Toast.LENGTH_LONG).show();

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])){
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            }
            else{
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 1){
            for(int i=0; i<permissions.length; i++){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, permissions[i] + "권한이 승인됨.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, permissions[i] + "권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class GPSListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude: " + latitude + "\nLongitude: " + longitude;
            Log.i("GPSListener", msg);

            radioButton = (RadioButton)findViewById(radioGroup.getCheckedRadioButtonId());
            String activity = radioButton.getText().toString();

            sqlite = mydb.getWritableDatabase();

            String sql = "INSERT INTO location(latitude, longitude, activity) VALUES('" +latitude+ "', '" +longitude+ "', '" +activity+"') " ;

            Log.d("myLocation", sql);

            sqlite.execSQL(sql);
            sqlite.close();
            Toast.makeText(getApplicationContext(), msg + " 데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private void startLocationService(){
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        GPSListener gpsListener = new GPSListener();

        long minTime = 5000;
        float minDistance = 0;

        try{
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime, minDistance, gpsListener);
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime, minDistance, gpsListener);

//            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if (lastLocation != null) {
//                Double latitude = lastLocation.getLatitude();
//                Double longitude = lastLocation.getLongitude();
//
//                Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_SHORT).show();
//            }
        }
        catch (SecurityException ex){
            ex.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "위치 정보 저장", Toast.LENGTH_SHORT).show();
    }
}
