package com.albrimon.app.ui.screens.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.albrimon.app.navigation.Routes
import com.albrimon.app.ui.components.AlbrimonScaffold
import com.albrimon.app.ui.components.ClientCard
import com.albrimon.app.ui.components.FilterPills
import com.albrimon.app.ui.components.SearchField
import com.albrimon.app.viewmodel.client.ClientFilter
import com.albrimon.app.viewmodel.client.ClientListViewModel

@Composable
fun ClientListScreen(
    navController: NavController,
    viewModel: ClientListViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AlbrimonScaffold(navController = navController) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Clientes",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge
                )
                // Nuevo cliente (formulario pendiente para la siguiente iteración)
                IconButton(onClick = { navController.navigate(Routes.soon("Nuevo cliente")) }) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Nuevo cliente",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            SearchField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Buscar cliente..."
            )
            Spacer(modifier = Modifier.height(12.dp))

            FilterPills(
                options = ClientFilter.entries.map { it.label },
                selectedIndex = state.filter.ordinal,
                onSelect = { viewModel.onFilterChange(ClientFilter.entries[it]) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.clients.isEmpty()) {
                Text(
                    text = "No se encontraron clientes",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.clients, key = { it.id }) { client ->
                        ClientCard(
                            client = client,
                            onClick = { navController.navigate(Routes.clientDetail(client.id)) }
                        )
                    }
                }
            }
        }
    }
}
