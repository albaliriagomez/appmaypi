package com.torrezpillcokevin.nuna.dbSqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.torrezpillcokevin.nuna.models.Contact

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_CONTACTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_LINE TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    // Método para insertar contacto
    fun addContact(contact: Contact): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, contact.name)
            put(COLUMN_PHONE, contact.phone)
            put(COLUMN_LINE, contact.line)
        }
        val result = db.insert(TABLE_CONTACTS, null, values)
        db.close()

        //Log.d("DB_INSERT", "Insertado: $contact (resultado: $result)")
        return result
    }

    // Método para obtener todos los contactos
    fun getAllContacts(): List<Contact> {
        val contactList = mutableListOf<Contact>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_CONTACTS", null)

        if (cursor.moveToFirst()) {
            do {
                val contact = Contact(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    line = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LINE))
                )
                contactList.add(contact)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return contactList
    }
    // Método para actualizar un contacto
    fun updateContact(contact: Contact): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, contact.name)
            put(COLUMN_PHONE, contact.phone)
            put(COLUMN_LINE, contact.line)
        }
        val result = db.update(TABLE_CONTACTS, values, "$COLUMN_ID = ?", arrayOf(contact.id.toString()))
        db.close()
        return result
    }

    // Método para eliminar un contacto por ID
    fun deleteContact(id: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_CONTACTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_CONTACTS = "contacts"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_LINE = "line"
    }
}
