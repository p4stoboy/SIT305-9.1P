package sit305.a71p

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@Database(entities = [LostAndFoundItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): LostAndFoundItemDao
}

@Volatile
private var INSTANCE: AppDatabase? = null

fun getDatabase(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(AppDatabase::class.java) {
        val instance = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "lost_and_found_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
    }
}

@Entity
data class LostAndFoundItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postType: PostType,
    val name: String,
    val phone: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
)

enum class PostType {
    LOST, FOUND
}

@Dao
interface LostAndFoundItemDao {
    @Query("SELECT * FROM LostAndFoundItem")
    fun getAllItems(): Flow<List<LostAndFoundItem>>

    @Insert
    suspend fun insertItem(item: LostAndFoundItem): Long

    @Delete
    suspend fun deleteItem(item: LostAndFoundItem): Int
}

class LostAndFoundItemRepo(private val itemDao: LostAndFoundItemDao, private val scope: CoroutineScope) {
    val allItems: StateFlow<List<LostAndFoundItem>> = itemDao.getAllItems()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    suspend fun insert(item: LostAndFoundItem) {
        itemDao.insertItem(item)
    }

    suspend fun delete(item: LostAndFoundItem) {
        itemDao.deleteItem(item)
    }
}
