package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.eternalfairy.timeaware.ui.screen.BOTTOM_BAR_COLOR
import com.eternalfairy.timeaware.ui.screen.BOTTOM_BAR_TEXT_COLOR

@Composable
fun TaskDropdownMenu(
    dropdownItems: List<DropDownItem>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box (
    ) {
        IconButton(
            onClick = { expanded = !expanded }
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More options",
                modifier = modifier,
                tint = Color(BOTTOM_BAR_TEXT_COLOR)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(BOTTOM_BAR_COLOR))
            ) {
                for (dropdownItem in dropdownItems) {
                    DropdownMenuItem(
                        text = { Text(dropdownItem.text) },
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = dropdownItem.iconId),
                                contentDescription = null,
                                tint = Color(BOTTOM_BAR_TEXT_COLOR)
                            )
                        },
                        onClick = dropdownItem.onClick
                    )
                }
            }
        }
    }
}

class DropDownItem (
    val text: String,
    val iconId: Int,
    val onClick: () -> Unit
)