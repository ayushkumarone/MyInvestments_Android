package com.lalorosas.myinvestments

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.NumberFormat

class Investment {
    var id: Int = 0
    var investmentName: String? = null
    var investmentAmount: Float = 0f

    constructor(id: Int, investmentName: String, investmentAmount: Float) {
        this.id = id
        this.investmentAmount = investmentAmount
        this.investmentName = investmentName
    }

    constructor(investmentName: String, investmentAmount: Float) {
        this.investmentAmount = investmentAmount
        this.investmentName = investmentName
    }

    override fun toString(): String {
        val amountString = NumberFormat.getCurrencyInstance().format(investmentAmount)
        return "$investmentName ($amountString)"
    }
}

class InvestmentDBOpenHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val table = ("CREATE TABLE " +
                TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME + " TEXT," +
                COLUMN_AMOUNT + " REAL" + ")")
        db.execSQL(table)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insert(name: Investment) {
        val values = ContentValues()
        values.put(COLUMN_NAME, name.investmentName)
        values.put(COLUMN_AMOUNT, name.investmentAmount)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun read(): ArrayList<Investment>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        val investments = ArrayList<Investment>()

        if (cursor.moveToFirst()) {
            do {
                val nameColumnIndex = cursor.getColumnIndex(COLUMN_NAME)
                val amountColumnIndex = cursor.getColumnIndex(COLUMN_AMOUNT)

                if (nameColumnIndex >= 0 && amountColumnIndex >= 0) {
                    val name = cursor.getString(nameColumnIndex)
                    val amount = cursor.getFloat(amountColumnIndex)
                    val investment = Investment(name, amount)
                    investments.add(investment)
                } else {
                    if (nameColumnIndex < 0) {
                        Log.e("InvestmentDBOpenHelper", "Column $COLUMN_NAME not found in cursor")
                    }
                    if (amountColumnIndex < 0) {
                        Log.e("InvestmentDBOpenHelper", "Column $COLUMN_AMOUNT not found in cursor")
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        return investments
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "investments.db"
        const val TABLE_NAME = "investment"
        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "investmentname"
        const val COLUMN_AMOUNT = "investmentamount"
    }
}
