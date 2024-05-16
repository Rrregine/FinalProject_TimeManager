package com.wang.finalproject_calendar

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TITLE VARCHAR," +
                "$COLUMN_DETAILS VARCHAR," +
                "$COLUMN_DATE VARCHAR," +
                "$COLUMN_TIME VARCHAR," +
                "$COLUMN_COLOR VARCHAR)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addPerson(title: String, details: String, date: String, time: String, color: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_TITLE, title)
        contentValues.put(COLUMN_DETAILS, details)
        contentValues.put(COLUMN_DATE, date)
        contentValues.put(COLUMN_TIME, time)
        contentValues.put(COLUMN_COLOR, color)

        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L
    }

    fun updateEvent(id: Int, title: String, details: String, date: String, time: String, color: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_TITLE, title)
        contentValues.put(COLUMN_DETAILS, details)
        contentValues.put(COLUMN_DATE, date)
        contentValues.put(COLUMN_TIME, time)
        contentValues.put(COLUMN_COLOR, color)
        val selection = "$COLUMN_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        val updatedRows = db.update(TABLE_NAME, contentValues, selection, selectionArgs)
        return updatedRows > 0
    }

    companion object {
        const val DATABASE_NAME = "TaskDatabase"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DETAILS = "details"
        const val COLUMN_DATE = "date"
        const val COLUMN_TIME = "time"
        const val COLUMN_COLOR = "color"
    }
}