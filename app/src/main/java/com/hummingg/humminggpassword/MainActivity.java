package com.hummingg.humminggpassword;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {
    private static int MIN_PASSWORD_LENGTH = 12;

    private DatabaseManager dbManager;

    private ActivityResultLauncher<Intent> createFileLauncher, openFileLauncher;
    private Uri[] directoryUriReturn = new Uri[1]; // 使用数组来存储 Uri
    private String content; // 要加密的内容
    private String password; // 加密口令
    private SecretKey key; // 加密密钥
    private String cryptFileName = "请勿删除_加密_个人信息_重要_备份.txt"; // 加密解密文件名


    private EditText txtCryptPwd;
    private EditText txtAppName;
    private EditText txtUserName;
    private EditText txtPassword;
    private TextView txtResult;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtCryptPwd = findViewById(R.id.txtCryptPwd);
        txtAppName = findViewById(R.id.txtAppName);
        txtUserName = findViewById(R.id.txtUserName);
        txtPassword = findViewById(R.id.txtPassword);
        txtResult = findViewById(R.id.txtResult);
        txtResult.setMovementMethod(new ScrollingMovementMethod());

        // 注册 ActivityResultLauncher
        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        directoryUriReturn[0] = result.getData().getData();
                        // 在这里使用之前生成的 encryptedData
                        String encryptedData = null;
                        try {
                            encryptedData = AESUtils.encrypt(content, key);
                        } catch (Exception e) {
                            txtResult.setText(e.getMessage());
                            return;
                        }
                        saveFileToDirectory(directoryUriReturn[0], cryptFileName, encryptedData);
                    }
                });

        // 注册 ActivityResultLauncher
        openFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (uri != null) {
                                String fileContent = readFileContent(uri);
                                import2Database(fileContent);
                            }
                        }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void import2Database(String fileContent) {
        Log.i("import data", fileContent);
        // 解析格式 1+3+1=5个字段，按\n分割
        String[] colValues = fileContent.split("\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // batch insert
        for(int i=0; i<colValues.length; i+=5){
            dbManager = new DatabaseManager(this);
            String appName = colValues[i+1];
            String userName = colValues[i+2];
            String pass = colValues[i+3];
            String timeStr = colValues[i+4];
            LocalDateTime time = null;
            if(!timeStr.isEmpty() && !timeStr.equals("null")) {
                time = LocalDateTime.parse(timeStr, formatter);
            }
            dbManager.insertData(appName, userName, pass, time);
        }
        Toast.makeText(this, "导入成功", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onBtnUpdateClick(View view) {
        String appName = txtAppName.getText().toString().trim();
        String userName = txtUserName.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        if(appName.isEmpty() || userName.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        dbManager = new DatabaseManager(this);
        // 插入数据到数据库
        dbManager.insertData(appName, userName, password, null);
        Toast.makeText(this, "添加成功", Toast.LENGTH_LONG).show();
    }

    public void onBtnSearchClick(View view) {
        String appName = txtAppName.getText().toString();
        String userName = txtUserName.getText().toString();

        // 查询数据
        dbManager = new DatabaseManager(this);
        Cursor cursor = dbManager.searchData(appName, userName);
        StringBuilder results = new StringBuilder();
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String app = cursor.getString(cursor.getColumnIndex("app_name"));
                @SuppressLint("Range") String user = cursor.getString(cursor.getColumnIndex("user_name"));
                @SuppressLint("Range") String pass = cursor.getString(cursor.getColumnIndex("password"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("create_time"));

                results.append(id).append("\n")
                        .append(app).append("\n")
                        .append(user).append("\n")
                        .append(pass).append("\n")
                        .append(time).append("\n***************\n");
            } while (cursor.moveToNext());
        } else {
            results.append("没有匹配的密码记录。");
        }
        cursor.close();

        txtResult.setText(results.toString());

    }

    public void onBtnGenerateClick(View view) {
        String password = PasswordUtils.generatePassword(MIN_PASSWORD_LENGTH);
        txtPassword.setText(password);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onBtnExportClick(View view){
        password = txtCryptPwd.getText().toString().trim();
        if(password.isEmpty()){
            Toast.makeText(this, "请先输入口令再导入、导出", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            key = AESUtils.getKeyFromPassword(password);
        } catch (Exception e) {
            txtResult.setText(e.getMessage());
            return;
        }

        dbManager = new DatabaseManager(this);
        StringBuilder results = new StringBuilder();
        try(Cursor cursor = dbManager.searchData("", "")) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String app = cursor.getString(cursor.getColumnIndex("app_name"));
                    @SuppressLint("Range") String user = cursor.getString(cursor.getColumnIndex("user_name"));
                    @SuppressLint("Range") String pass = cursor.getString(cursor.getColumnIndex("password"));
                    @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("create_time"));

                    results.append(id).append("\n")
                            .append(app).append("\n")
                            .append(user).append("\n")
                            .append(pass).append("\n")
                            .append(time).append("\n");
                } while (cursor.moveToNext());
            } else {
                results.append("没有匹配的密码记录。");
                Toast.makeText(this, "没有可导出的密码", Toast.LENGTH_LONG).show();
                return;
            }
        }
        content = results.toString();
        Log.i("export data", content);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        createFileLauncher.launch(intent);
    }

    public void onBtnImportClick(View view){
        password = txtCryptPwd.getText().toString().trim();
        if(password.isEmpty()){
            Toast.makeText(this, "请先输入口令再导入、导出", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            key = AESUtils.getKeyFromPassword(password);
        } catch (Exception e) {
            txtResult.setText(e.getMessage());
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // 选择所有类型的文件
        openFileLauncher.launch(intent);
    }

    private void saveFileToDirectory(Uri directoryUri, String fileName, String content) {
        try {
            // 使用 DocumentsContract 创建文件
            Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(directoryUri,
                    DocumentsContract.getTreeDocumentId(directoryUri));

            // 创建文件
            Uri newFileUri = DocumentsContract.createDocument(getContentResolver(), fileUri, "text/plain", fileName);
            if(newFileUri == null){
                Toast.makeText(this, "已取消导入", Toast.LENGTH_LONG).show();
                return;
            }
            OutputStream outputStream = getContentResolver().openOutputStream(newFileUri);
            if (outputStream != null) {
                outputStream.write(content.getBytes());
                outputStream.close();
            }
        } catch (Exception e) {
            txtResult.setText(e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String readFileContent(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        try (InputStream inputStream = contentResolver.openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String fileContent = stringBuilder.toString();
            fileContent = AESUtils.decrypt(fileContent, key);
            return fileContent;
        } catch (Exception e) {
            txtResult.setText(e.getMessage());
            return "";
        }
    }
}