package com.knownassurajit.dvide_finance.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.knownassurajit.dvide_finance.app.BuildConfig
import com.knownassurajit.dvide_finance.app.data.local.CycleDao
import com.knownassurajit.dvide_finance.app.data.local.DvideDatabase
import com.knownassurajit.dvide_finance.app.data.local.TransactionDao
import com.knownassurajit.dvide_finance.app.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class BackupCounts(val cycles: Int, val transactions: Int)

/**
 * Local (file based) backup and restore. Nothing here touches the network: the user chooses the destination
 * or source through the system file picker (SAF), so no storage permission is needed.
 *
 * Restore policy is **replace**: all current cycles, entries and the backed-up preferences are swapped for the
 * file's content inside one Room transaction, so a failure leaves the existing data untouched. Merging and
 * duplicate resolution are intentionally not offered; the UI asks the user to confirm the replacement.
 */
@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: DvideDatabase,
    private val cycleDao: CycleDao,
    private val transactionDao: TransactionDao,
    private val settingsRepo: SettingsRepository,
) {
    suspend fun exportTo(uri: Uri): Result<BackupCounts> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = BackupSnapshot(
                cycles = cycleDao.getAll(),
                transactions = transactionDao.getAll(),
                settings = settingsRepo.exportForBackup(),
            )
            val json = BackupCodec.encode(snapshot, Instant.now(), BuildConfig.VERSION_NAME)
            val out = context.contentResolver.openOutputStream(uri, "wt")
                ?: throw IOException("Couldn't open the destination file.")
            out.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            BackupCounts(snapshot.cycles.size, snapshot.transactions.size)
        }
    }

    /** Reads and validates [uri] without changing any data. */
    suspend fun read(uri: Uri): BackupParse = withContext(Dispatchers.IO) {
        try {
            val stream = context.contentResolver.openInputStream(uri)
                ?: return@withContext BackupParse.Failure("Couldn't open the selected file.")
            val bytes = stream.use { it.readBoundedBytes() }
                ?: return@withContext BackupParse.Failure("That file is too large to be a DVIDE backup.")
            BackupCodec.decode(String(bytes, Charsets.UTF_8))
        } catch (e: IOException) {
            BackupParse.Failure("Couldn't read the selected file.")
        } catch (e: SecurityException) {
            BackupParse.Failure("DVIDE isn't allowed to read that file.")
        }
    }

    /** Replaces the current data with [backup]. All-or-nothing for cycles and entries. */
    suspend fun restore(backup: BackupParse.Success): Result<BackupCounts> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = backup.snapshot
            db.withTransaction {
                transactionDao.deleteAll()
                cycleDao.deleteAll()
                cycleDao.insertAll(snapshot.cycles)
                transactionDao.insertAll(snapshot.transactions)
                keepCycleIdsUnique(snapshot)
            }
            // Preferences are a separate store; they are applied only after the data committed.
            settingsRepo.applyBackup(snapshot.settings)
            BackupCounts(snapshot.cycles.size, snapshot.transactions.size)
        }
    }

    /**
     * Entries may reference cycles that were deleted (the app keeps entries when a cycle is removed). SQLite's
     * AUTOINCREMENT counter restarts at the highest *existing* id after a restore, so a new cycle could otherwise
     * reuse an id that old entries still point at and silently adopt them. Raise the counter past every id in use.
     */
    private fun keepCycleIdsUnique(snapshot: BackupSnapshot) {
        val highest = maxOf(
            snapshot.cycles.maxOfOrNull { it.id } ?: 0L,
            snapshot.transactions.maxOfOrNull { it.cycleId ?: 0L } ?: 0L,
        )
        if (highest > 0) {
            db.openHelper.writableDatabase.execSQL(
                "UPDATE sqlite_sequence SET seq = MAX(seq, $highest) WHERE name = 'cycles'"
            )
        }
    }

    private companion object {
        const val MAX_BYTES = 64 * 1024 * 1024

        /** Reads the whole stream, or returns null when it exceeds [MAX_BYTES] (guards against huge/wrong files). */
        fun java.io.InputStream.readBoundedBytes(): ByteArray? {
            val out = ByteArrayOutputStream()
            val buf = ByteArray(64 * 1024)
            var total = 0
            while (true) {
                val n = read(buf)
                if (n < 0) break
                total += n
                if (total > MAX_BYTES) return null
                out.write(buf, 0, n)
            }
            return out.toByteArray()
        }
    }
}
