package com.jnetai.churnpredictor.data

import androidx.room.*
import com.jnetai.churnpredictor.model.Customer
import com.jnetai.churnpredictor.model.Interaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    suspend fun getAllCustomersSync(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): Customer?

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface InteractionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(interaction: Interaction): Long

    @Update
    suspend fun update(interaction: Interaction)

    @Delete
    suspend fun delete(interaction: Interaction)

    @Query("SELECT * FROM interactions WHERE customerId = :customerId ORDER BY date DESC")
    fun getForCustomer(customerId: Long): Flow<List<Interaction>>

    @Query("SELECT * FROM interactions WHERE customerId = :customerId ORDER BY date DESC")
    suspend fun getForCustomerSync(customerId: Long): List<Interaction>

    @Query("SELECT * FROM interactions WHERE customerId = :customerId AND date >= :sinceDate ORDER BY date DESC")
    suspend fun getRecentForCustomer(customerId: Long, sinceDate: String): List<Interaction>

    @Query("SELECT * FROM interactions ORDER BY date DESC")
    fun getAllInteractions(): Flow<List<Interaction>>

    @Query("DELETE FROM interactions WHERE customerId = :customerId")
    suspend fun deleteForCustomer(customerId: Long)
}