package com.knownassurajit.dvide_finance.app.domain.model

enum class ExportFormat(
    val id: String,
    val label: String,
    val extension: String,
    val mimeType: String,
) {
    CSV("csv", "CSV Spreadsheet (.csv)", "csv", "text/csv"),
    SPREADSHEET("tsv", "Tab-Separated TSV (.tsv)", "tsv", "text/tab-separated-values"),
    MARKDOWN("md", "Markdown Table (.md)", "md", "text/markdown"),
    TEXT("txt", "Plain Text (.txt)", "txt", "text/plain");

    companion object {
        fun fromId(id: String): ExportFormat =
            entries.firstOrNull { it.id == id } ?: CSV
    }
}
