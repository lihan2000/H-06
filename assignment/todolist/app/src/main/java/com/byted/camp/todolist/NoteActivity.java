package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.db.FeedReaderContract;
import com.byted.camp.todolist.operation.db.FeedReaderDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.byted.camp.todolist.db.TodoContract.TodoEntry;

public class NoteActivity extends AppCompatActivity {

    private static final String TAG = "database";
    private EditText editText;
    private Button addBtn;
    private TodoDbHelper dbHelper;
    private String contenttext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                contenttext=editText.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功

        /**
         * created by lihan
         * 2020/4/30
         */
        TodoDbHelper dbHelper = new TodoDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Date date=new Date();
        SimpleDateFormat ft = new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss");

        ContentValues values = new ContentValues();

        values.put(TodoEntry.COLUMN_CONTENT,content);
        values.put(TodoEntry.COLUMN_STATE, "TODO");
        values.put(TodoEntry.COLUMN_DATE,ft.format(date));

        long newRowId = db.insert(TodoEntry.TABLE_NAME, null, values);
        Log.i(TAG, "perform add data, result:" + newRowId);
        //return true;

        //返回是否插入成功
        if(newRowId > 0)
            return true;
        else
            return false;

    }
}
