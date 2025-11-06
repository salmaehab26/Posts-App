import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.example.postsapp.ui.theme.Pink40

@Composable
fun ModernLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Pink40,
    dotCount: Int = 3
) {
    val transition = rememberInfiniteTransition(label = "")
    val animations = List(dotCount) { index ->
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    1f at (index * 150)
                    0.3f at (index * 150 + 500)
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "dot-$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        animations.forEach { scaleAnim ->
            Canvas(modifier = Modifier.size(14.dp)) {
                scale(scaleAnim.value) {
                    drawCircle(color = color)
                }
            }
        }
    }
}
