package com.x8bit.bitwarden.ui.vault.feature.itemlisting

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.x8bit.bitwarden.R
import com.x8bit.bitwarden.ui.platform.components.BitwardenListHeaderTextWithSupportLabel
import com.x8bit.bitwarden.ui.platform.components.BitwardenListItem
import com.x8bit.bitwarden.ui.platform.components.SelectionItemData
import com.x8bit.bitwarden.ui.platform.components.model.toIconResources
import kotlinx.collections.immutable.toPersistentList

/**
 * Content view for the [VaultItemListingScreen].
 */
@Composable
fun VaultItemListingContent(
    state: VaultItemListingState.ViewState.Content,
    vaultItemClick: (id: String) -> Unit,
    onOverflowItemClick: (action: VaultItemListingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        item {
            BitwardenListHeaderTextWithSupportLabel(
                label = stringResource(id = R.string.items),
                supportingLabel = state.displayItemList.size.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }
        items(state.displayItemList) {
            BitwardenListItem(
                startIcon = it.iconData,
                label = it.title,
                supportingLabel = it.subtitle,
                onClick = { vaultItemClick(it.id) },
                trailingLabelIcons = it
                    .extraIconList
                    .toIconResources()
                    .toPersistentList(),
                selectionDataList = it
                    .overflowOptions
                    .map { option ->
                        SelectionItemData(
                            text = option.title(),
                            onClick = { onOverflowItemClick(option.action) },
                        )
                    }
                    .toPersistentList(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        // There is some built-in padding to the menu button that makes up
                        // the visual difference here.
                        end = 12.dp,
                    ),
            )
        }
    }
}
