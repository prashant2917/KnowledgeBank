package com.pocket.knowledge.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pocket.knowledge.models.News
import java.util.*

class DbHandler(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        val createContactsTable = ("CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NID + " INTEGER,"
                + KEY_NEWS_TITLE + " TEXT,"
                + KEY_CATEGORY_NAME + " TEXT,"
                + KEY_NEWS_DATE + " TEXT,"
                + KEY_NEWS_IMAGE + " TEXT,"
                + KEY_NEWS_DESCRIPTION + " TEXT,"
                + KEY_CONTENT_TYPE + " TEXT,"
                + KEY_VIDEO_URL + " TEXT,"
                + KEY_VIDEO_ID + " TEXT,"
                + KEY_COMMENTS_COUNT + " INTEGER"
                + ")")
        db.execSQL(createContactsTable)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")

        // Create tables again
        onCreate(db)
    }

    //Adding Record in Database
    fun addToFavorite(pj: News) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NID, pj.nid)
        values.put(KEY_NEWS_TITLE, pj.news_title)
        values.put(KEY_CATEGORY_NAME, pj.category_name)
        values.put(KEY_NEWS_DATE, pj.news_date)
        values.put(KEY_NEWS_IMAGE, pj.news_image)
        values.put(KEY_NEWS_DESCRIPTION, pj.news_description)
        values.put(KEY_CONTENT_TYPE, pj.content_type)
        values.put(KEY_VIDEO_URL, pj.video_url)
        values.put(KEY_VIDEO_ID, pj.video_id)
        values.put(KEY_COMMENTS_COUNT, pj.comments_count)

        // Inserting Row
        db.insert(TABLE_NAME, null, values)
        db.close() // Closing database connection
    }// Adding contact to list

    // return contact list
// Select All Query

    // looping through all rows and adding to list

    // Getting All Data
    val allData: List<News>
        get() {
            val dataList: MutableList<News> = ArrayList()
            // Select All Query
            val selectQuery = "SELECT  * FROM $TABLE_NAME ORDER BY id DESC"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    val contact = News()
                    contact.id = cursor.getString(0).toInt()
                    contact.nid = cursor.getString(1).toInt().toLong()
                    contact.news_title = cursor.getString(2)
                    contact.category_name = cursor.getString(3)
                    contact.news_date = cursor.getString(4)
                    contact.news_image = cursor.getString(5)
                    contact.news_description = cursor.getString(6)
                    contact.content_type = cursor.getString(7)
                    contact.video_url = cursor.getString(8)
                    contact.video_id = cursor.getString(9)
                    contact.comments_count = cursor.getString(10).toInt().toLong()

                    // Adding contact to list
                    dataList.add(contact)
                } while (cursor.moveToNext())
            }

            // return contact list
            return dataList
        }

    //getting single row
    fun getFavRow(id: Long): List<News> {
        val dataList: MutableList<News> = ArrayList()
        // Select All Query
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE nid=$id"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                val contact = News()
                contact.id = cursor.getString(0).toInt()
                contact.nid = cursor.getString(1).toInt().toLong()
                contact.news_title = cursor.getString(2)
                contact.category_name = cursor.getString(3)
                contact.news_date = cursor.getString(4)
                contact.news_image = cursor.getString(5)
                contact.news_description = cursor.getString(6)
                contact.content_type = cursor.getString(7)
                contact.video_url = cursor.getString(8)
                contact.video_id = cursor.getString(9)
                contact.comments_count = cursor.getString(10).toInt().toLong()

                // Adding contact to list
                dataList.add(contact)
            } while (cursor.moveToNext())
        }

        // return contact list
        return dataList
    }

    //for remove favorite
    fun removeFav(contact: News) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$KEY_NID = ?", arrayOf(contact.nid.toString()))
        db.close()
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "db_android_news_app"
        private const val TABLE_NAME = "tbl_favorite"
        private const val KEY_ID = "id"
        private const val KEY_NID = "nid"
        private const val KEY_NEWS_TITLE = "news_title"
        private const val KEY_CATEGORY_NAME = "category_name"
        private const val KEY_NEWS_DATE = "news_date"
        private const val KEY_NEWS_IMAGE = "news_image"
        private const val KEY_NEWS_DESCRIPTION = "news_description"
        private const val KEY_CONTENT_TYPE = "content_type"
        private const val KEY_VIDEO_URL = "video_url"
        private const val KEY_VIDEO_ID = "video_id"
        private const val KEY_COMMENTS_COUNT = "comments_count"
    }
}