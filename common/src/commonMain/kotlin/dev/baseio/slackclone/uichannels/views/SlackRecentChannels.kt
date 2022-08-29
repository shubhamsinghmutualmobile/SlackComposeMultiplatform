package dev.baseio.slackclone.uichannels.views

import dev.baseio.slackclone.uichannels.SlackChannelVM
import dev.baseio.slackclone.chatcore.data.ExpandCollapseModel
import androidx.compose.runtime.*
import dev.baseio.slackclone.chatcore.data.UiLayerChannels
import org.koin.java.KoinJavaComponent

@Composable
fun SlackRecentChannels(
  onItemClick: (UiLayerChannels.SlackChannel) -> Unit = {},
  onClickAdd: () -> Unit
) {
  val channelVM: SlackChannelVM by KoinJavaComponent.inject(SlackChannelVM::class.java)

  val recent = "Recent"
  val channelsFlow = channelVM.channels.collectAsState()
  val channels by channelsFlow.value.collectAsState(emptyList())

  LaunchedEffect(key1 = Unit) {
    channelVM.allChannels()
  }

  var expandCollapseModel by remember {
    mutableStateOf(
      ExpandCollapseModel(
      1, recent,
      needsPlusButton = false,
      isOpen = true
    )
    )
  }
  SKExpandCollapseColumn(expandCollapseModel, onItemClick, {
    expandCollapseModel = expandCollapseModel.copy(isOpen = it)
  }, channels, onClickAdd)
}