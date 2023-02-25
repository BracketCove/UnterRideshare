package com.bracketcove.android.services

import android.util.Log
import com.bracketcove.android.ServiceResult

import com.bracketcove.android.domain.UnterUser
import com.bracketcove.android.keys.*
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class StreamUserService(
    private val client: ChatClient
) : UserService {

    /**
     * Due to permission issues with roles, all users must be elevated to admin to be able to
     * add themselves to channels they
     */
    private fun updateRole(userId: String) {
        client.partialUpdateUser(
            id = userId,
            set = mutableMapOf(
                KEY_ROLE to "admin"
            )
        )
    }
    override suspend fun getUserById(userId: String): ServiceResult<UnterUser?> =
        withContext(Dispatchers.IO) {
            val currentUser = client.getCurrentUser()
            if (currentUser != null && currentUser.id == userId) {
                val extraData = currentUser.extraData
                val type: String? = extraData[KEY_TYPE] as String?
                val status: String? = extraData[KEY_STATUS] as String?

                if (currentUser.role == "user") updateRole(userId)

                ServiceResult.Value(
                    UnterUser(
                        userId = userId,
                        username = currentUser.name,
                        avatarPhotoUrl = currentUser.image,
                        createdAt = currentUser.createdAt.toString(),
                        updatedAt = currentUser.updatedAt.toString(),
                        status = status ?: "",
                        type = type ?: ""
                    )
                )
            } else if (currentUser != null && currentUser.id != userId){
                val streamUser = User(
                    id = userId
                )

                val devToken = client.devToken(userId)
                val getUserResult = client.switchUser(streamUser, devToken).await()

                if (getUserResult.isSuccess) {
                    val user = getUserResult.data().user
                    val extraData = user.extraData
                    val type: String? = extraData[KEY_TYPE] as String?
                    val status: String? = extraData[KEY_STATUS] as String?

                    if (currentUser.role == "user") updateRole(userId)

                    ServiceResult.Value(
                        UnterUser(
                            userId = userId,
                            username = user.name,
                            avatarPhotoUrl = user.image,
                            createdAt = user.createdAt.toString(),
                            updatedAt = user.updatedAt.toString(),
                            status = status ?: "",
                            type = type ?: ""
                        )
                    )
                } else {
                    Log.d(
                        "GET_USER_BY_ID",
                        getUserResult.error().message ?: "Stream error occurred for update user"
                    )
                    ServiceResult.Failure(Exception(getUserResult.error().message))
                }
            } else {
                val streamUser = User(
                    id = userId
                )

                val devToken = client.devToken(userId)
                val getUserResult = client.connectUser(streamUser, devToken).await()

                if (getUserResult.isSuccess) {
                    val user = getUserResult.data().user
                    val extraData = user.extraData
                    val type: String? = extraData[KEY_TYPE] as String?
                    val status: String? = extraData[KEY_STATUS] as String?

                    ServiceResult.Value(
                        UnterUser(
                            userId = userId,
                            username = user.name,
                            avatarPhotoUrl = user.image,
                            createdAt = user.createdAt.toString(),
                            updatedAt = user.updatedAt.toString(),
                            status = status ?: "",
                            type = type ?: ""
                        )
                    )
                } else {
                    Log.d(
                        "GET_USER_BY_ID",
                        getUserResult.error().message ?: "Stream error occurred for update user"
                    )
                    ServiceResult.Failure(Exception(getUserResult.error().message))
                }
            }
        }


    override suspend fun updateUser(user: UnterUser): ServiceResult<UnterUser?> =
        withContext(Dispatchers.IO) {
            val result = client.partialUpdateUser(
                id = user.userId,
                set = mutableMapOf(
                    KEY_STATUS to user.status,
                    KEY_TYPE to user.type,
                    KEY_ROLE to "admin",
                    KEY_IMAGE to user.avatarPhotoUrl
                )
            ).await()

            if (result.isSuccess) {
                ServiceResult.Value(user)
            } else {
                Log.d(
                    "UPDATE_USER",
                    result.error().message ?: "Stream error occurred for update user"
                )
                ServiceResult.Failure(Exception(result.error().cause))
            }
        }

    override suspend fun initializeNewUser(user: UnterUser): ServiceResult<UnterUser?> =
        withContext(Dispatchers.IO) {
            disconnectUser(user.userId)

            delay(4000L)
            val streamUser = user.let {
                User(
                    id = user.userId,
                    name = user.username,
                    extraData = mutableMapOf(
                        KEY_STATUS to user.status,
                        KEY_TYPE to user.type
                    )
                )
            }

            val devToken = client.devToken(user.userId)
            val result = client.connectUser(streamUser, devToken).await()

            if (result.isSuccess) {
                ServiceResult.Value(
                    user
                )
            } else {
                ServiceResult.Failure(Exception(result.error().cause))
            }
        }

    private suspend fun disconnectUser(userId: String) {
        val currentUser = client.getCurrentUser()
        if (currentUser != null && userId == currentUser.id) {
            client.disconnect(false).await()
        }
    }

    override suspend fun logOutUser(user: UnterUser) =
        withContext(Dispatchers.IO) {
            val result = client.disconnect(flushPersistence = true).await()
            if (result.isError) Log.d(
                "LOG_USER_OUT",
                result.error().message ?: "Error logging out"
            )
            Unit
        }
}