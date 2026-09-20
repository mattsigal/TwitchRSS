package me.ash.reader.ui.page.home.reading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.infrastructure.preference.LocalReadingFonts
import me.ash.reader.infrastructure.preference.LocalReadingTitleAlign
import me.ash.reader.infrastructure.preference.LocalReadingTitleBold
import me.ash.reader.infrastructure.preference.LocalReadingTitleFontSize
import me.ash.reader.infrastructure.preference.LocalReadingTitleUpperCase
import me.ash.reader.infrastructure.preference.LocalHighlightSubreddits
import me.ash.reader.infrastructure.preference.LocalHighlightSubredditColor
import me.ash.reader.infrastructure.preference.LocalCustomHighlightRules
import me.ash.reader.infrastructure.preference.highlightTitle
import me.ash.reader.ui.ext.formatAsString
import me.ash.reader.ui.ext.requiresBidi
import me.ash.reader.ui.theme.applyTextDirection
import java.util.Date

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Metadata(
    feedName: String,
    title: String,
    publishedDate: Date,
    modifier: Modifier = Modifier,
    author: String? = null,
) {
    val context = LocalContext.current
    val titleBold = LocalReadingTitleBold.current
    val titleUpperCase = LocalReadingTitleUpperCase.current
    val titleAlign = LocalReadingTitleAlign.current.toTextAlign()
    val dateString =
        remember(publishedDate) { publishedDate.formatAsString(context, atHourMinute = true) }
    val fontFamily = LocalReadingFonts.current.asFontFamily(context)

    val titleUpperCaseString by remember { derivedStateOf { title.uppercase() } }

    val labelColor = MaterialTheme.colorScheme.outline.copy(alpha = .7f)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(12.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = dateString,
            color = labelColor,
            style = MaterialTheme.typography.labelMedium.merge(fontFamily = fontFamily),
            textAlign = titleAlign,
        )
        Spacer(modifier = Modifier.height(4.dp))
        val titleFontSize = LocalReadingTitleFontSize.current.value
        val highlightSubreddits = LocalHighlightSubreddits.current
        val highlightSubredditColor = LocalHighlightSubredditColor.current
        val customRules = LocalCustomHighlightRules.current
        val rawTitle = if (titleUpperCase.value) titleUpperCaseString else title
        val annotatedTitle = remember(rawTitle, highlightSubreddits, highlightSubredditColor, customRules) {
            rawTitle.highlightTitle(
                highlightSubreddits = highlightSubreddits,
                subredditColorHex = highlightSubredditColor,
                customRules = customRules,
            )
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = annotatedTitle,
            color = MaterialTheme.colorScheme.onSurface,
            style =
                MaterialTheme.typography.headlineLarge
                    .merge(
                        fontSize = titleFontSize.sp,
                        lineHeight = (titleFontSize * 1.3f).sp,
                        fontFamily = fontFamily,
                        fontWeight = if (titleBold.value) FontWeight.Bold else FontWeight.Medium,
                    )
                    .applyTextDirection(requiresBidi = title.requiresBidi()),
            textAlign = titleAlign,
        )
        Spacer(modifier = Modifier.height(4.dp))
        author?.let {
            if (it.isNotEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    color = labelColor,
                    style = MaterialTheme.typography.labelMedium.merge(fontFamily = fontFamily),
                    textAlign = titleAlign,
                )
            }
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = feedName,
            color = labelColor,
            style = MaterialTheme.typography.labelMedium.merge(fontFamily = fontFamily),
            textAlign = titleAlign,
        )
    }
}
