package dev.baseio.slackclone.uichannels.directmessages

import ViewModel

import dev.baseio.slackclone.chatcore.data.UiLayerChannels
import dev.baseio.slackdomain.mappers.UiModelMapper
import dev.baseio.slackdomain.model.channel.DomainLayerChannels
import dev.baseio.slackdomain.model.message.DomainLayerMessages
import dev.baseio.slackdomain.model.workspaces.DomainLayerWorkspaces
import dev.baseio.slackdomain.usecases.channels.UseCaseFetchChannelsWithLastMessage
import dev.baseio.slackdomain.usecases.workspaces.UseCaseGetSelectedWorkspace
import kotlinx.coroutines.flow.*

class MessageViewModel constructor(
  private val useCaseFetchChannels: UseCaseFetchChannelsWithLastMessage,
  private val channelPresentationMapper: UiModelMapper<DomainLayerChannels.SKChannel, UiLayerChannels.SKChannel>,
  private val useCaseGetSelectedWorkspace: UseCaseGetSelectedWorkspace
) : ViewModel() {


  val channels = MutableStateFlow(fetchFlow())

  fun refresh() {
    channels.value = useCaseGetSelectedWorkspace.performStreaming(Unit).flatMapConcat {
      useCaseFetchChannels.performStreaming(it!!.uuid)
    }
  }

  fun fetchFlow(): Flow<List<DomainLayerMessages.SKLastMessage>> {
    return  useCaseGetSelectedWorkspace.performStreaming(Unit).flatMapConcat {
      useCaseFetchChannels.performStreaming(it!!.uuid)
    }

  }

  fun mapToUI(channel: DomainLayerChannels.SKChannel): UiLayerChannels.SKChannel {
    return channelPresentationMapper.mapToPresentation(channel)
  }

}