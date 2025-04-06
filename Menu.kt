package ui

import cache.UserCache
import model.Repository
import model.User
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Scanner

object Menu {
    private val scanner = Scanner(System.`in`)
    private val api = RetrofitClient.instance

    fun show() {
        while (true) {
            println(
                """
                ==============================
                1- Fetch GitHub user info
                2- List cached users
                3- Search user in cache
                4- Search repository in cache
                5- Exit
                ==============================
                Choose an option:
                """.trimIndent()
            )

            when (scanner.nextLine()) {
                "1" -> fetchUser()
                "2" -> listCachedUsers()
                "3" -> searchUserInCache()
                "4" -> searchRepoInCache()
                "5" -> {
                    println("Exiting program...")
                    return
                }
                else -> println("Invalid option!")
            }
        }
    }

    private fun fetchUser() {
        print("Enter GitHub username: ")
        val username = scanner.nextLine()

        val cached = UserCache.getUser(username)
        if (cached != null) {
            println("User found in cache:")
            printUserInfo(cached.user, cached.repositories)
            return
        }

        println("Please Wait...")

        api.getUser(username).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body()
                if (user != null) {
                    api.getRepositories(username).enqueue(object : Callback<List<Repository>> {
                        override fun onResponse(call: Call<List<Repository>>, response: Response<List<Repository>>) {
                            val repos = response.body() ?: emptyList()
                            val cachedUser = cache.CachedUser(user, repos)
                            UserCache.addUser(username, cachedUser)
                            println("Data successfully fetched:")
                            printUserInfo(user, repos)
                        }

                        override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                            println("Failed to fetch repositories: ${t.message}")
                        }
                    })
                } else {
                    println("User not found.")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                println("API request failed: ${t.message}")
            }
        })
    }

    private fun listCachedUsers() {
        val users = UserCache.getAllUsers()
        if (users.isEmpty()) {
            println("Cache is empty.")
            return
        }

        println("Cached users:")
        users.forEach { (username, data) ->
            println("- $username | ${data.user.followers} followers | ${data.repositories.size} repositories")
        }
    }

    private fun searchUserInCache() {
        print("Enter username to search in cache: ")
        val username = scanner.nextLine()
        val cached = UserCache.getUser(username)
        if (cached != null) {
            println("User found:")
            printUserInfo(cached.user, cached.repositories)
        } else {
            println("User not found in cache.")
        }
    }

    private fun searchRepoInCache() {
        print("Enter repository name to search: ")
        val query = scanner.nextLine()

        val results = UserCache.getAllUsers()
            .flatMap { it.value.repositories }
            .filter { it.name.contains(query, ignoreCase = true) }

        if (results.isEmpty()) {
            println("No repositories found with that name.")
        } else {
            println("Repositories found:")
            results.forEach { println("- ${it.name}: ${it.html_url}") }
        }
    }

    private fun printUserInfo(user: User, repos: List<Repository>) {
        println("Username: ${user.login}")
        println("Created at: ${user.created_at}")
        println("Followers: ${user.followers}")
        println("Following: ${user.following}")
        println("Public Repositories:")
        if (repos.isEmpty()) println("— No repositories found.")
        repos.forEach { println("- ${it.name}: ${it.html_url}") }
    }
}
