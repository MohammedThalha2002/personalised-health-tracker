package com.example.personalisedtracker.feature.reports.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.personalisedtracker.core.common.DispatcherProvider
import com.example.personalisedtracker.feature.reports.domain.MarkdownReportGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

/**
 * Writes a [MarkdownReportGenerator] output to the app cache and returns a
 * shareable `content://` URI. The Settings screen invokes the system
 * share-sheet on that URI.
 */
@Singleton
class MarkdownReportWriter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val generator: MarkdownReportGenerator,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun write(days: Long): Uri = withContext(dispatchers.io) {
        val text = generator.generate(days = days)
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val file = File(context.cacheDir, "tracker-report-${days}d-$stamp.md")
        file.writeText(text)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}

