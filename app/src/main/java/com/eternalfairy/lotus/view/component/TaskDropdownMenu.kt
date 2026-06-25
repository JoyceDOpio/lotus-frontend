package com.eternalfairy.lotus.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eternalfairy.lotus.R
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.HEADER_TEXT_COLOR

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
                imageVector = ImageVector.vectorResource(id = R.drawable.more_vert_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                contentDescription = "More options",
                modifier = modifier,
                tint = HEADER_TEXT_COLOR
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(COMPONENT_BACKGROUND_COLOR)
            ) {
                for (dropdownItem in dropdownItems) {
                    DropdownMenuItem(
                        text = { Text(dropdownItem.text) },
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = dropdownItem.iconId),
                                contentDescription = null,
                                tint = HEADER_TEXT_COLOR,
                                modifier = Modifier.size(dropdownItem.size)
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
    val onClick: () -> Unit,
    val size: Dp = 25.dp
)