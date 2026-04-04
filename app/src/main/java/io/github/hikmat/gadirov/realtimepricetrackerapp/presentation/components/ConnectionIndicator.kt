package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.hikmat.gadirov.realtimepricetrackerapp.R

@Composable
fun ConnectionIndicator(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (isConnected) Color.Green else Color.Red)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(
                id = if (isConnected) R.string.status_connected else R.string.status_disconnected
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionIndicatorConnectedPreview() {
    MaterialTheme {
        ConnectionIndicator(isConnected = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionIndicatorDisconnectedPreview() {
    MaterialTheme {
        ConnectionIndicator(isConnected = false)
    }
}
