package dev.baseio.slackclone.uichat.newchat

import ViewModel
import dev.baseio.slackclone.navigation.ComposeNavigator
import dev.baseio.slackclone.navigation.NavigationKey
import dev.baseio.slackclone.navigation.SlackScreens
import dev.baseio.slackdomain.CoroutineDispatcherProvider
import dev.baseio.slackdomain.model.channel.DomainLayerChannels
import dev.baseio.slackdomain.usecases.channels.UseCaseCreateChannel
import dev.baseio.slackdomain.usecases.channels.UseCaseWorkspaceChannelRequest
import dev.baseio.slackdomain.usecases.channels.UseCaseSearchChannel
import dev.baseio.slackdomain.usecases.users.UseCaseFetchAndSaveUsers
import dev.baseio.slackdomain.usecases.users.UseCaseFetchChannelsWithSearch
import dev.baseio.slackdomain.usecases.users.UseCaseFetchLocalUsers
import dev.baseio.slackdomain.usecases.workspaces.UseCaseGetSelectedWorkspace
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class NewChatThreadVM(
  private val useCaseGetSelectedWorkspace: UseCaseGetSelectedWorkspace,
  private val useCaseFetchAndSaveUsers: UseCaseFetchAndSaveUsers,
  private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
  private val useCaseCreateChannel: UseCaseCreateChannel,
  private val useCaseFetchChannelsWithSearch: UseCaseFetchChannelsWithSearch
) :
  ViewModel() {

  val search = MutableStateFlow("")
  var channelsStream = MutableStateFlow<List<DomainLayerChannels.SKChannel>>(emptyList())
    private set

  var errorStream = MutableStateFlow<Throwable?>(null)
    private set

  init {
    viewModelScope.launch {
      useCaseGetSelectedWorkspace.invokeFlow().onEach { workspace ->
        workspace?.uuid?.let { useCaseFetchAndSaveUsers(it) }
      }.launchIn(this)

      search.collectLatest { search ->
        useCaseGetSelectedWorkspace.invokeFlow()
          .mapNotNull { it }
          .flatMapConcat { workspace ->
            useCaseFetchChannelsWithSearch(workspace.uuid, search)
          }.flowOn(coroutineDispatcherProvider.io)
          .onEach {
            channelsStream.value = it
          }.flowOn(coroutineDispatcherProvider.main)
          .launchIn(viewModelScope)
      }
    }
  }

  fun search(newValue: String) {
    search.value = newValue
  }

  private fun navigate(channel: DomainLayerChannels.SKChannel, composeNavigator: ComposeNavigator) {
    composeNavigator.deliverResult(
      NavigationKey.NavigateChannel,
      channel,
      SlackScreens.Dashboard
    )
  }

  fun createChannel(channel: DomainLayerChannels.SKChannel, composeNavigator: ComposeNavigator) {
    viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
      errorStream.value = throwable
    }) {
      channel.channelId.takeIf { it.isNotEmpty() }?.let {
        navigate(channel, composeNavigator)
      } ?: run {
        val result = useCaseCreateChannel.invoke(channel)
        val channelNew = result.getOrThrow()
        navigate(channelNew, composeNavigator)
      }
    }

  }
}