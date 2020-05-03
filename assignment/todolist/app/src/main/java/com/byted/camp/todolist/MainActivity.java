package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.operation.db.FeedReaderContract;
import com.byted.camp.todolist.operation.db.FeedReaderDbHelper;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.byted.camp.todolist.db.TodoContract.TodoEntry;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;
    private static final String TAG = "TodoDB";

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper dbHelper;

//    public MainActivity(TodoDbHelper dbHelper) {
//        this.dbHelper = dbHelper;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //////////
        dbHelper = new TodoDbHelper(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) throws ParseException {

                MainActivity.this.deleteNote(note);
                notesAdapter.refresh(loadNotesFromDatabase());
            }

            @Override
            public void updateNote(Note note) throws ParseException {
                MainActivity.this.updateNode(note);
                notesAdapter.refresh(loadNotesFromDatabase());

            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());

        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans

        /**
         * created by lihan
         * 2020/4/28
         * 添加到数据库中的元素？content state date
         * date格式？
         */
        List<Note> Notes=new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //时间格式，参考NoteViewHolder中的格式
        SimpleDateFormat sdf =new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss" );
        //时间降序，最新添加的note在最上面
        String sortOrder = TodoEntry.COLUMN_DATE + " DESC";
        //游标
        Cursor cursor = db.query(
                TodoEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder            // The sort order
        );

        while (cursor.moveToNext()) {

            String id = cursor.getString(cursor.getColumnIndex(TodoEntry._ID));
            long id_note =Long.valueOf(id);
            Note note=new Note(id_note);
            //CONTENT
            note.setContent(cursor.getString(cursor.getColumnIndex(TodoEntry.COLUMN_CONTENT)));
            //DATE
            try{
                note.setDate(sdf.parse(cursor.getString(cursor.getColumnIndex(TodoEntry.COLUMN_DATE))));
            }catch (ParseException e){
                e.printStackTrace();
            }

            //STATE
            String state = cursor.getString(cursor.getColumnIndex(TodoEntry.COLUMN_STATE));
            if (state.equals("DONE"))
                note.setState(State.DONE);
            else if (state.equals("TODO"))
                note.setState(State.TODO);

            Notes.add(note);
            Log.i(TAG, "perform query data, result:" + id);

        }
        cursor.close();

        //返回notes
        return Notes;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据

        /**
         * created by lihan
         * 2020/4/28
         */

        SQLiteDatabase db = dbHelper.getWritableDatabase();

//        //根据此note中的content。
//        String selection = TodoEntry.COLUMN_CONTENT + " LIKE ?";
//        String[] selectionArgs = {note.getContent()};

        //不能根据content和state！！！因为有可能出现两项或多项相同的情况。
        //根据date。唯一
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        String selection = TodoEntry.COLUMN_DATE + " LIKE ?";
        String[] selectionArgs = {sdf.format(note.getDate())};


        int deletedRows = db.delete(TodoEntry.TABLE_NAME, selection, selectionArgs);
        Log.i(TAG, "perform delete data, result:" + deletedRows);
    }

    private void updateNode(Note note) {
        // 更新数据

        /**
         * created by lihan
         * 2020/4/30
         */
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        //只更新state的值就行，其他不变。
        values.put(TodoEntry.COLUMN_STATE,"DONE");

        //不能用state和content！！！
//        String selection = TodoEntry.COLUMN_CONTENT + " LIKE ?";
//        String[] selectionArgs = {note.getContent()};

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        String selection = TodoEntry.COLUMN_DATE + " LIKE ?";
        String[] selectionArgs = {sdf.format(note.getDate())};

        int count = db.update(
                TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

    }

}
