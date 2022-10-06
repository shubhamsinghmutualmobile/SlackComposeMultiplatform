package dev.baseio.slackserver.services

import database.SkUser
import dev.baseio.slackdata.protos.*
import dev.baseio.slackserver.data.AuthDataSource
import dev.baseio.slackserver.data.UsersDataSource
import dev.baseio.slackserver.services.interceptors.AUTH_CONTEXT_KEY
import dev.baseio.slackserver.services.interceptors.USER_ID
import dev.baseio.slackserver.services.interceptors.WORKSPACE_ID
import io.grpc.Status
import io.grpc.StatusException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.Dispatchers
import java.security.Key
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


class AuthService(
  coroutineContext: CoroutineContext = Dispatchers.IO,
  private val authDataSource: AuthDataSource
) :
  AuthServiceGrpcKt.AuthServiceCoroutineImplBase(coroutineContext) {

  override suspend fun register(request: SKAuthUser): SKAuthResult {
    return try {
      val user = request.user.toDBUser()
      val generatedUser = authDataSource.register(
        request.email,
        request.password,
        user
      )
      skAuthResult(generatedUser)
    } catch (ex: Exception) {
      ex.printStackTrace()
      SKAuthResult.newBuilder().build()
    }
  }

  override suspend fun login(request: SKAuthUser): SKAuthResult {
    authDataSource.login(request.email, request.password, request.user.workspaceId)?.let {
      return skAuthResult(it)
    }
    return SKAuthResult
      .newBuilder()
      .setStatus(
        SKStatus.newBuilder()
          .setInformation("User not found for the workspace!")
          .build()
      )
      .build()
  }

  private fun skAuthResult(generatedUser: SkUser?): SKAuthResult {
    val keyBytes =
      Decoders.BASE64.decode("8Zz5tw0Ionm3XPZZfN0NOml3z9FMfmpgXwovR9fp6ryDIoGRM8EPHAB6iHsc0fb")// TODO move this to env variables
    val key: Key = Keys.hmacShaKeyFor(keyBytes)
    val jws = jwtTokenFiveDays(generatedUser, key)
    return SKAuthResult.newBuilder()
      .setToken(jws) //no refresh token for now
      .build()
  }

  private fun jwtTokenFiveDays(generatedUser: SkUser?, key: Key): String? = Jwts.builder()
    .setClaims(hashMapOf<String, String?>().apply {
      put(USER_ID, generatedUser?.uuid)
      put(WORKSPACE_ID, generatedUser?.workspaceId)
    })
    .setExpiration(Date.from(Instant.now().plusMillis(TimeUnit.DAYS.toMillis(5))))// valid for 5 days
    .signWith(key)
    .compact()

}