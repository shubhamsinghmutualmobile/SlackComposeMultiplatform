package dev.baseio.slackclone.uionboarding.vmtest

import app.cash.turbine.test
import dev.baseio.slackclone.uichat.newchat.NavigateChatThreadVM
import dev.baseio.slackdomain.usecases.channels.UseCaseCreateChannel
import dev.baseio.slackdomain.usecases.users.UseCaseFetchChannelsWithSearch
import kotlinx.coroutines.test.runTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.asserter
import kotlin.time.Duration.Companion.seconds

class NavigateChatThreadVMTest : SlackKoinUnitTest() {
    private val useCaseCreateChannel: UseCaseCreateChannel by inject()
    private val useCaseFetchChannelsWithSearch: UseCaseFetchChannelsWithSearch by inject()

    private val navigateChatThreadVM: NavigateChatThreadVM by lazy {
        NavigateChatThreadVM(
            useCaseGetSelectedWorkspace,
            getUsers,
            coroutineDispatcherProvider,
            useCaseCreateChannel,
            useCaseFetchChannelsWithSearch
        ) {

        }
    }

    @Test
    fun `when user searches a channel he gets the channel list with that criteria`() {
        runTest {
            authorizeUserFirst()
            navigateChatThreadVM.search("new_channel")
            navigateChatThreadVM.channelsStream.test(timeout = 5.seconds) {
                awaitItem()
                awaitItem().apply {
                    asserter.assertTrue({ "was expecting items!" }, this.isNotEmpty())
                    asserter.assertTrue(
                        { "was expecting new_channel!" },
                        this.filter { it.channelName == "new_channel" }.size == 1
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}