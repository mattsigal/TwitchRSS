package me.ash.reader.ui.page.settings.color.highlights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.*
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.checkColorHex
import me.ash.reader.ui.theme.palette.onLight

private val PRESET_COLORS = listOf(
    "#FF4500" to "Reddit Orange",
    "#FF5722" to "Deep Orange",
    "#FF9800" to "Amber",
    "#E53935" to "Crimson Red",
    "#4CAF50" to "Green",
    "#009688" to "Teal",
    "#1E88E5" to "Blue",
    "#8E24AA" to "Purple",
    "#E91E63" to "Pink",
)

@Composable
fun TitleHighlightsPage(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val highlightSubreddits = LocalHighlightSubreddits.current
    val highlightSubredditColor = LocalHighlightSubredditColor.current
    val customRules = LocalCustomHighlightRules.current

    var subredditColorDialogVisible by remember { mutableStateOf(false) }
    var customSubredditHexDialogVisible by remember { mutableStateOf(false) }
    var customSubredditColorInput by remember { mutableStateOf(highlightSubredditColor) }
    var addRuleDialogVisible by remember { mutableStateOf(false) }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack,
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    DisplayText(
                        text = stringResource(R.string.title_highlights),
                        desc = stringResource(R.string.title_highlights_desc),
                    )
                }

                // Live Preview
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.preview),
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            val sample1 = "/r/playstation/ There are 8 Horcruxs of Sony out there"
                            val sample2 = "[Skip] [SPC] get 50% off your next order of $30+"
                            val sample3 = "/r/JellyfinCommunity/ Fishbowl has a new update (v1.4.5) Jellyfin 12.1"

                            Text(
                                text = sample1.highlightTitle(
                                    highlightSubreddits = highlightSubreddits,
                                    subredditColorHex = highlightSubredditColor,
                                    customRules = customRules,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = sample2.highlightTitle(
                                    highlightSubreddits = highlightSubreddits,
                                    subredditColorHex = highlightSubredditColor,
                                    customRules = customRules,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = sample3.highlightTitle(
                                    highlightSubreddits = highlightSubreddits,
                                    subredditColorHex = highlightSubredditColor,
                                    customRules = customRules,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Subreddit Section
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.highlight_subreddits),
                    )
                    SettingItem(
                        title = stringResource(R.string.highlight_subreddits),
                        desc = stringResource(R.string.highlight_subreddits_desc),
                        separatedActions = true,
                        onClick = {
                            HighlightSubredditsPreference(!highlightSubreddits).put(context, scope)
                        },
                    ) {
                        RYSwitch(activated = highlightSubreddits) {
                            HighlightSubredditsPreference(!highlightSubreddits).put(context, scope)
                        }
                    }

                    if (highlightSubreddits) {
                        SettingItem(
                            title = stringResource(R.string.subreddit_color),
                            desc = highlightSubredditColor,
                            onClick = { subredditColorDialogVisible = true },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(highlightSubredditColor))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Custom Rules Section
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.custom_rules),
                    )
                }

                if (customRules.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_rules_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                    }
                } else {
                    items(customRules) { rule ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(rule.colorHex))
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = rule.pattern,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = parseHexColor(rule.colorHex),
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val updated = customRules.filter { it != rule }
                                        CustomHighlightRulesPreference(updated).put(context, scope)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.delete_rule),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { addRuleDialogVisible = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.add_rule))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        },
    )

    // Subreddit Color Preset Dialog
    if (subredditColorDialogVisible) {
        RadioDialog(
            visible = subredditColorDialogVisible,
            title = stringResource(R.string.subreddit_color),
            options = PRESET_COLORS.map { (hex, name) ->
                RadioDialogOption(
                    text = "$name ($hex)",
                    selected = highlightSubredditColor.equals(hex, ignoreCase = true),
                ) {
                    HighlightSubredditColorPreference(hex).put(context, scope)
                    subredditColorDialogVisible = false
                }
            } + listOf(
                RadioDialogOption(
                    text = stringResource(R.string.custom_hex),
                    selected = PRESET_COLORS.none { it.first.equals(highlightSubredditColor, ignoreCase = true) },
                ) {
                    customSubredditColorInput = highlightSubredditColor
                    subredditColorDialogVisible = false
                    customSubredditHexDialogVisible = true
                }
            ),
        ) {
            subredditColorDialogVisible = false
        }
    }

    // Subreddit Custom Hex Dialog
    if (customSubredditHexDialogVisible) {
        TextFieldDialog(
            visible = customSubredditHexDialogVisible,
            title = stringResource(R.string.custom_hex),
            value = customSubredditColorInput,
            placeholder = "#FF4500",
            onValueChange = {
                customSubredditColorInput = it
            },
            onDismissRequest = {
                customSubredditHexDialogVisible = false
            },
            onConfirm = { hex ->
                hex.checkColorHex()?.let {
                    HighlightSubredditColorPreference(it).put(context, scope)
                    customSubredditHexDialogVisible = false
                }
            }
        )
    }

    // Add Custom Rule Dialog
    if (addRuleDialogVisible) {
        AddRuleDialog(
            onDismiss = { addRuleDialogVisible = false },
            onAddRule = { pattern, colorHex ->
                val newRule = HighlightRule(pattern = pattern, colorHex = colorHex)
                val updated = customRules.filter { it.pattern != pattern } + newRule
                CustomHighlightRulesPreference(updated).put(context, scope)
                addRuleDialogVisible = false
            },
        )
    }
}

@Composable
private fun AddRuleDialog(
    onDismiss: () -> Unit,
    onAddRule: (pattern: String, colorHex: String) -> Unit,
) {
    var patternText by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf(PRESET_COLORS.first().first) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.add_rule)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = patternText,
                    onValueChange = { patternText = it },
                    label = { Text(text = stringResource(R.string.keyword_or_pattern)) },
                    placeholder = { Text(text = stringResource(R.string.pattern_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.choose_color),
                    style = MaterialTheme.typography.titleSmall,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PRESET_COLORS.take(5).forEach { (hex, _) ->
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape,
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PRESET_COLORS.drop(5).forEach { (hex, _) ->
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape,
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (patternText.isNotBlank()) {
                        onAddRule(patternText.trim(), selectedColorHex)
                    }
                },
                enabled = patternText.isNotBlank(),
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
