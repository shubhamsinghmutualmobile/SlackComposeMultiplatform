package dev.baseio.slackclone.uichat.chatthread.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.Keyboard
import dev.baseio.slackclone.commonui.reusable.MentionsTextField
import dev.baseio.slackclone.commonui.reusable.SpanInfos
import dev.baseio.slackclone.commonui.theme.SlackCloneColorProvider
import dev.baseio.slackclone.commonui.theme.SlackCloneTypography
import dev.baseio.slackclone.uichat.chatthread.BoxState
import dev.baseio.slackclone.uichat.chatthread.ChatScreenComponent
import dev.baseio.slackclone.uichat.chatthread.ChatViewModel
import dev.keyboardAsState
import mainDispatcher

@Composable
fun ChatMessageBox(
    screenComponent: ChatScreenComponent,
    viewModel: ChatViewModel = screenComponent.chatViewModel,
    modifier: Modifier
) {
    val keyboard by keyboardAsState()
    var focusState by remember { mutableStateOf<FocusState?>(null) }
    val focusRequester = FocusRequester()

    LaunchedEffect(true) {
        if (keyboard is Keyboard.HardwareKeyboard) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier.background(SlackCloneColorProvider.colors.uiBackground),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        MessageTFRow(
            viewModel,
            modifier = Modifier.padding(
                start = 4.dp
            ).onFocusChanged { newFocusState ->
                focusState = newFocusState
            }.focusRequester(focusRequester)
        )
        AnimatedVisibility(
            keyboard is Keyboard.Opened ||
                keyboard is Keyboard.HardwareKeyboard ||
                focusState?.hasFocus == true
        ) {
            ChatOptions(
                viewModel,
                Modifier
            )
        }
    }
}

@Composable
fun ChatOptions(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val search by viewModel.message.collectAsState(mainDispatcher)

    Row(
        modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Outlined.Add, contentDescription = null, chatOptionIconSize())
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = null, chatOptionIconSize())
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Outlined.Email, contentDescription = null, chatOptionIconSize())
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null, chatOptionIconSize())
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Outlined.Phone, contentDescription = null, chatOptionIconSize())
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Outlined.MailOutline, contentDescription = null, chatOptionIconSize())
            }
        }
        Box(Modifier.padding(end = 8.dp)) {
            SendMessageButton(viewModel = viewModel, search = search.text)
        }
    }
}

private fun chatOptionIconSize() = Modifier.size(20.dp)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MessageTFRow(
    viewModel: ChatViewModel,
    modifier: Modifier
) {

    val mentionText by viewModel.message.collectAsState(mainDispatcher)

    var currentlyEditing by remember {
        mutableStateOf<SpanInfos?>(null)
    }

    Column {
        Divider(color = SlackCloneColorProvider.colors.lineColor, thickness = 0.5.dp)
        Row(
            modifier
        ) {
            MentionsTextField(
                mentionText = mentionText,
                onSpanUpdate = { _, spans, range ->
                    viewModel.setSpanInfo(spans)
                    spans.firstOrNull { infos ->
                        range.intersects(infos.range) || range.end == infos.range.end
                    }?.let { infos ->
                        currentlyEditing = infos
                    } ?: kotlin.run {
                        currentlyEditing = null
                    }
                },
                onValueChange = {
                    viewModel.message.value = it
                },
                maxLines = 4,
                cursorBrush = SolidColor(SlackCloneColorProvider.colors.textPrimary),
                textStyle = SlackCloneTypography.subtitle1.copy(
                    color = SlackCloneColorProvider.colors.textPrimary
                ),
                decorationBox = { innerTextField ->
                    ChatTFPlusPlaceHolder(mentionText.text, Modifier, innerTextField, viewModel)
                },
                modifier = Modifier.weight(1f).onKeyEvent { event: KeyEvent ->
                    when {
                        eventIsEnter(event) -> {
                            viewModel.sendMessageNow(mentionText.text)
                            return@onKeyEvent true
                        }

                        event.isShiftPressed && event.key == Key.Enter -> {
                            // allow next line
                            return@onKeyEvent true
                        }

                        else -> false
                    }
                }
            )
            CollapseExpandButton(viewModel)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun eventIsEnter(event: KeyEvent) = !event.isShiftPressed && event.type == KeyEventType.KeyUp &&
    event.key == Key.Enter

@Composable
fun CollapseExpandButton(viewModel: ChatViewModel) {
    val isExpanded by viewModel.chatBoxState.collectAsState()
    IconButton(
        onClick = {
            viewModel.switchChatBoxState()
        }
    ) {
        Icon(
            Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            modifier = Modifier.graphicsLayer {
                rotationZ = if (isExpanded != BoxState.Collapsed) 180F else 0f
            }
        )
    }
}

@Composable
private fun SendMessageButton(
    viewModel: ChatViewModel,
    search: String,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            viewModel.sendMessageNow(search)
        },
        enabled = search.isNotEmpty(),
        modifier = modifier
    ) {
        Icon(
            Icons.Default.Send,
            contentDescription = null,
            tint = if (search.isEmpty()) SlackCloneColorProvider.colors.sendButtonDisabled else SlackCloneColorProvider.colors.sendButtonEnabled
        )
    }
}

@Composable
private fun ChatTFPlusPlaceHolder(
    search: String,
    modifier: Modifier = Modifier,
    innerTextField: @Composable () -> Unit,
    viewModel: ChatViewModel
) {
    val channel by viewModel.channelFlow.subscribeAsState()
    Row(
        modifier
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (search.isEmpty()) {
            Text(
                text = "Message ${channel.channelName}",
                style = SlackCloneTypography.subtitle1.copy(
                    color = SlackCloneColorProvider.colors.textSecondary
                ),
                modifier = Modifier.weight(1f)
            )
        } else {
            innerTextField()
        }
    }
}
