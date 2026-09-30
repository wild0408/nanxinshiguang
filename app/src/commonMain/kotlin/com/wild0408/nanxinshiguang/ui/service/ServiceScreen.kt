package com.wild0408.nanxinshiguang.ui.service

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.Destination
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.chevron_right_24px
import nanxinshiguang.generated.resources.desc_grade_center
import nanxinshiguang.generated.resources.desc_electricity
import nanxinshiguang.generated.resources.item_grade_center
import nanxinshiguang.generated.resources.item_electricity
import nanxinshiguang.generated.resources.electricity_24px
import nanxinshiguang.generated.resources.list_alt_24px
import nanxinshiguang.generated.resources.nav_service

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.nav_service)) }
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Card(
                    onClick = { onNavigate(Destination.GradeCenter) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    RowContent(
                        icon = Res.drawable.list_alt_24px,
                        title = stringResource(Res.string.item_grade_center),
                        description = stringResource(Res.string.desc_grade_center)
                    )
                }
            }
            item {
                Card(
                    onClick = { onNavigate(Destination.ElectricityCenter) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    RowContent(
                        icon = Res.drawable.electricity_24px,
                        title = stringResource(Res.string.item_electricity),
                        description = stringResource(Res.string.desc_electricity)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowContent(
    icon: org.jetbrains.compose.resources.DrawableResource,
    title: String,
    description: String
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = vectorResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = vectorResource(Res.drawable.chevron_right_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
