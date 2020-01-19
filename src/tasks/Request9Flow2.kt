package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import contributors.logRepos
import contributors.logUsers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

fun loadContributorsFlow2(
    service: GitHubService,
    req: RequestData
): Flow<Pair<List<User>, Boolean>> = flow {
    coroutineScope {
        var allUsers = listOf<User>()
        var lastIndex: Int

        service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .bodyList()
            .also { lastIndex = it.lastIndex }
            .map { repo ->
                async {
                    service
                        .getRepoContributors(req.org, repo.name)
                        .also { logUsers(repo, it) }
                        .bodyList()
                }
            }.forEachIndexed { index, deferredUser ->
                val newUsers = deferredUser.await()
                allUsers = (allUsers + newUsers).aggregate()
                emit(allUsers to (index == lastIndex))
            }
    }
}.flowOn(Dispatchers.IO)
