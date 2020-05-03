package com.byted.camp.todolist.db;

import android.provider.BaseColumns;

import com.byted.camp.todolist.operation.db.FeedReaderContract;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public final class TodoContract {

    // TODO 定义表结构和 SQL 语句常量

    /**
     * created by lihan
     * 2020/4/28
     * 数据库中的列还未确定 ID CONTENT STATE DATE?
     */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TodoEntry.TABLE_NAME + " (" +
                    TodoEntry._ID + " INTEGER PRIMARY KEY," +
                    TodoEntry.COLUMN_CONTENT + " TEXT,"+
                    TodoEntry.COLUMN_STATE+" TEXT,"+
                    TodoEntry.COLUMN_DATE+" TEXT)";


    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TodoEntry.TABLE_NAME;
    private TodoContract() {
    }

    /* Inner class that defines the table contents */
    public static class TodoEntry implements BaseColumns {

        public static final String TABLE_NAME = "entry";

        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_STATE="state";
        public static final String COLUMN_DATE="date";
        public static final String COLUMN_ID="id";

    }



}
