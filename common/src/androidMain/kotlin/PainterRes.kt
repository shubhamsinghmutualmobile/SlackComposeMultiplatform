import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import org.example.common.R

actual object PainterRes {
    @Composable
    actual fun gettingStarted() = painterResource(R.drawable.gettingstarted)

    @Composable
    actual fun slackLogo() = painterResource(R.drawable.ic_launcher_foreground)
}
