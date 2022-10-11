package dev.baseio.slackclone.uionboarding.vm

import ViewModel
import dev.baseio.slackclone.navigation.ComposeNavigator
import dev.baseio.slackclone.uionboarding.compose.navigateDashboard
import dev.baseio.slackdomain.usecases.workspaces.UseCaseCreateWorkspace
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WorkspaceCreateVM(private val useCaseCreateWorkspace: UseCaseCreateWorkspace) : ViewModel() {
  val email = MutableStateFlow("")
  val password = MutableStateFlow("")
  val domain = MutableStateFlow("")
  val error = MutableStateFlow<Throwable?>(null)
  val loading = MutableStateFlow(false)
  fun createWorkspace(composeNavigator: ComposeNavigator) {
    viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
      throwable.printStackTrace()
      error.value = throwable
      loading.value = false
    }) {
      error.value = null
      loading.value = true
      useCaseCreateWorkspace(email.value, password.value, domain.value)
      loading.value = false
      navigateDashboard(composeNavigator)
    }
  }
}