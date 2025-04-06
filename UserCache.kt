package cache

import model.Repository
import model.User

data class CachedUser(
    val user: User,
    val repositories: List<Repository>
)

object UserCache {
    private val cache = mutableMapOf<String, CachedUser>()

    fun getUser(username: String): CachedUser? = cache[username]

    fun addUser(username: String, user: CachedUser) {
        cache[username] = user
    }

    fun getAllUsers(): Map<String, CachedUser> = cache
}
