package com.example.data

import android.content.Context
import android.database.Cursor
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Centralized SQLite Database Controller Module.
 * Provides granular low-level and high-level control over local SQLite database persistence,
 * supporting offline-first features, raw diagnostic queries, vacuum commands, backups, and restores.
 */
class SQLiteDatabaseController(private val context: Context, private val repository: POSRepository) {

    private val dbFile: File = context.getDatabasePath("zam_zam_pos_db")

    /**
     * Retrieves the physical size of the SQLite database in bytes.
     */
    fun getDatabaseSize(): Long {
        return if (dbFile.exists()) dbFile.length() else 0L
    }

    /**
     * Executes vacuum to optimize and defragment space in the local SQLite database.
     */
    suspend fun runVacuum(): Boolean = withContext(Dispatchers.IO) {
        try {
            val rawDb = repository.db.openHelper.writableDatabase
            rawDb.execSQL("VACUUM")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Retrieves a list of tables and their record counts to assist with diagnostic verification.
     */
    suspend fun getTableStats(): List<TableStat> = withContext(Dispatchers.IO) {
        val stats = mutableListOf<TableStat>()
        val tables = listOf("users", "products", "customers", "suppliers", "sales", "sale_items", "purchases", "purchase_items", "expenses", "audit_logs")
        
        try {
            val rawDb = repository.db.openHelper.writableDatabase
            for (tableName in tables) {
                val cursor = rawDb.query(SimpleSQLiteQuery("SELECT COUNT(*) FROM $tableName"))
                cursor.use { c ->
                    if (c.moveToFirst()) {
                        stats.add(TableStat(tableName, c.getInt(0)))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stats
    }

    /**
     * Backs up the SQLite database to a custom backup file.
     */
    suspend fun backupDatabase(): File? = withContext(Dispatchers.IO) {
        try {
            // Close the database to run checkpoint and save all wal logs
            repository.db.close()
            
            val backupFile = File(context.filesDir, "zam_zam_pos_db_backup.sqlite")
            if (dbFile.exists()) {
                FileInputStream(dbFile).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }
                backupFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Restores the SQLite database from the local backup file.
     */
    suspend fun restoreDatabase(): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(context.filesDir, "zam_zam_pos_db_backup.sqlite")
            if (!backupFile.exists()) return@withContext false

            repository.db.close()

            if (dbFile.exists()) {
                dbFile.delete()
            }
            
            // Delete accompanying shm and wal journals to prevent transaction conflicts
            val shmFile = File(dbFile.path + "-shm")
            val walFile = File(dbFile.path + "-wal")
            if (shmFile.exists()) shmFile.delete()
            if (walFile.exists()) walFile.delete()

            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Completely resets the SQLite database schema and re-seeds it with default entities.
     */
    suspend fun resetAndReinitialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            repository.db.clearAllTables()
            repository.populateSampleDataIfEmpty()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Executes arbitrary raw SQL query and returns a list of row maps for deep system inspection.
     */
    suspend fun executeRawQuery(sql: String, bindArgs: Array<Any>? = null): List<Map<String, Any?>> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<Map<String, Any?>>()
        try {
            val rawDb = repository.db.openHelper.writableDatabase
            val query = if (bindArgs != null) {
                SimpleSQLiteQuery(sql, bindArgs)
            } else {
                SimpleSQLiteQuery(sql)
            }
            val cursor = rawDb.query(query)
            cursor.use { c ->
                val columnNames = c.columnNames
                while (c.moveToNext()) {
                    val row = mutableMapOf<String, Any?>()
                    for (i in 0 until c.columnCount) {
                        val columnName = columnNames[i]
                        val value = when (c.getType(i)) {
                            Cursor.FIELD_TYPE_NULL -> null
                            Cursor.FIELD_TYPE_INTEGER -> c.getLong(i)
                            Cursor.FIELD_TYPE_FLOAT -> c.getDouble(i)
                            Cursor.FIELD_TYPE_STRING -> c.getString(i)
                            Cursor.FIELD_TYPE_BLOB -> c.getBlob(i)
                            else -> c.getString(i)
                        }
                        row[columnName] = value
                    }
                    resultList.add(row)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            resultList.add(mapOf("ERROR" to (e.localizedMessage ?: "Unknown query execution error")))
        }
        resultList
    }
}

data class TableStat(val tableName: String, val recordCount: Int)
