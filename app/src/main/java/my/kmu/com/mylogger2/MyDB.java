package my.kmu.com.mylogger2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by eomhyeong-geun on 2016. 11. 15..
 */

public class MyDB extends SQLiteOpenHelper {

    public MyDB(Context context){
        super(context, "myLocation", null, 1);   // 1번 버전으로 만들겠다.
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 테이블 이름이 멤버, 학번 이름 주소를 선언해줌.
        String sql = "Create table location" +
                "( _id integer primary key autoincrement" + // 1씩 자동증가 시켜주는 _id를 따로 만들어줘야 나중에 커서가 위치를 찾을 기준이됨.
                ", latitude real" +
                ", longitude real" +
                ", activity char(10)" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 만약 member라는 테이블이 존재한다면 날려버려라.
        String sql = "DROP TABLE IF EXISTS location";
        db.execSQL(sql);
        onCreate(db);   // 다시 생성을 해줘야됨.
    }
}
