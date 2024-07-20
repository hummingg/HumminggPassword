package com.hummingg.humminggpassword;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;


public class DatabaseManager {

    private DatabaseHelper dbHelper;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void insertData(String appName, String userName, String password, @Nullable LocalDateTime time) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("app_name", appName);
        values.put("user_name", userName);
        values.put("password", password);

        // Convert LocalDateTime to String
        if (time != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            values.put("create_time", time.format(formatter));
        }

        db.insert("user_info", null, values);
        db.close();
    }

    // 按 appName 和 userName 进行模糊查询
    public Cursor searchData(String appName, String userName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 使用 LIKE 进行模糊查询
        String selection = " 1=1 ";
        List<String> args = new ArrayList<String>();
        if(!appName.isEmpty() && !appName.isBlank()){
            selection += " AND app_name LIKE ? ";
            args.add("%" + appName + "%");
        }
        if(!userName.isEmpty() && !userName.isBlank()){
            selection += " AND user_name LIKE ? ";
            args.add("%" + userName + "%");
        }
        String[] selectionArgs = args.toArray(new String[0]);

        // 查询整个表
        return db.query(
                "user_info",    // 表名
                null,           // 返回所有列
                selection,      // WHERE 子句
                selectionArgs,  // WHERE 子句中的参数
                null,           // GROUP BY
                null,           // HAVING
                "create_time desc"            // ORDER BY
        );
    }
}
