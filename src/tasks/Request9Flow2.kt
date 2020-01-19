package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import contributors.logRepos
import contributors.logUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

suspend fun loadContributorsFlow2(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) = coroutineScope {
    flow {
        val users = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .bodyList()
            .map { repo ->
                async(Dispatchers.IO) {
                    service
                        .getRepoContributors(req.org, repo.name)
                        .also { logUsers(repo, it) }
                        .bodyList()
                }
            }

        var allUsers = listOf<User>()
        for ((index, user) in users.withIndex()) {
            val newUsers = user.await()
            allUsers = (allUsers + newUsers).aggregate()
            emit(allUsers to (index == users.lastIndex))
        }
    }
    .flowOn(Dispatchers.IO)
    .collect { (users, isCompleted) ->
        updateResults(users, isCompleted)
    }
}
