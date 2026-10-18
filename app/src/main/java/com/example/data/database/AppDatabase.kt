package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        UdhaarTransactionEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        StockMovementEntity::class,
        ExpenseEntity::class,
        DailyHisaabEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun udhaarDao(): UdhaarDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun dailyHisaabDao(): DailyHisaabDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_kirana_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDatabase(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Double check if products exist, otherwise seed
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.productDao().getProductCount() == 0) {
                            populateInitialDatabase(database)
                        }
                    }
                }
            }

            private suspend fun populateInitialDatabase(database: AppDatabase) {
                database.productDao().insertAll(SampleData.initialProducts)
                database.customerDao().insertAll(SampleData.initialCustomers)
                database.supplierDao().insertAll(SampleData.initialSuppliers)
            }
        }
    }
}
