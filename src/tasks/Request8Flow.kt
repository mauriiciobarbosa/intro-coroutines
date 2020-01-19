package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import contributors.logRepos
import contributors.logUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

suspend fun loadContributorsFlow(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) = coroutineScope {
    var allUsers = listOf<User>()
    var index = 0
    var numberOfRepos: Int

    service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()
        .also { numberOfRepos = it.size }
        .map { repo ->
            async {
                service
                    .getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            }
        }
        .asFlow()
        .map {
            val newUsers = it.await()
            allUsers = (allUsers + newUsers).aggregate()
            allUsers
        }
        .flowOn(Dispatchers.IO)
        .collect { users ->
            updateResults(users, ++index == numberOfRepos)
        }
}
