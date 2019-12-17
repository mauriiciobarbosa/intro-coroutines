package tasks

import contributors.GitHubService
import contributors.Repo
import contributors.RequestData
import contributors.User
import contributors.logRepos
import contributors.logUsers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()

    val channel: Channel<List<User>> = Channel()

    getUsers(service, req, repos, channel)
    updateUsersList(channel, repos.size, updateResults)
}

private fun CoroutineScope.getUsers(
    service: GitHubService,
    req: RequestData,
    repos: List<Repo>,
    channel: SendChannel<List<User>>
) {
    for (repo in repos) {
        launch {
            val users = service
                .getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()

            channel.send(users)
        }
    }
}

private fun CoroutineScope.updateUsersList(
    channel: ReceiveChannel<List<User>>,
    numberOfItems: Int,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {

    launch {
        var allUsers = listOf<User>()
        repeat(numberOfItems) { currentItemNumber ->
            val newUsers = channel.receive()
            allUsers = (allUsers + newUsers).aggregate()
            updateResults(allUsers, currentItemNumber == numberOfItems - 1)
        }
    }
}
