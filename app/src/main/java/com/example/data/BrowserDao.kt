package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {

    // Quick Shortcuts
    @Query("SELECT * FROM quick_shortcuts ORDER BY sortOrder ASC, id ASC")
    fun getAllShortcuts(): Flow<List<QuickShortcutEntity>>

    @Query("SELECT COUNT(*) FROM quick_shortcuts")
    suspend fun getShortcutCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: QuickShortcutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcuts(shortcuts: List<QuickShortcutEntity>)

    @Query("DELETE FROM quick_shortcuts WHERE id = :id")
    suspend fun deleteShortcutById(id: Int)

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Int)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    // Downloads
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadItemEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Int)

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()

    // Extensions
    @Query("SELECT * FROM extensions ORDER BY sourceType ASC, name ASC")
    fun getAllExtensions(): Flow<List<BrowserExtensionEntity>>

    @Query("SELECT COUNT(*) FROM extensions")
    suspend fun getExtensionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtension(extension: BrowserExtensionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtensions(extensions: List<BrowserExtensionEntity>)

    @Update
    suspend fun updateExtension(extension: BrowserExtensionEntity)

    @Query("DELETE FROM extensions WHERE id = :id")
    suspend fun deleteExtensionById(id: String)
}
