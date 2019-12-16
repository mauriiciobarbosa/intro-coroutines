package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    return service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()
        .map { repo ->
            GlobalScope.async {
                service
                    .getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            }
        }
        .awaitAll()
        .flatten()
        .aggregate()
}