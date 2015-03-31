package org.scoverage.coveralls

import java.io.File

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryBuilder
import org.scoverage.coveralls.GitClient.GitRevision
import sbt.Logger

object GitClient {
  case class GitRevision(id: String,
                         authorName: String,
                         authorEmail: String,
                         committerName: String,
                         committerEmail: String,
                         shortMessage: String)
}

class GitClient(cwd: String)(implicit log: Logger) {

  import scala.collection.JavaConversions._

  val repository = new RepositoryBuilder().setGitDir(new File(cwd + "/.git")).findGitDir(new File(cwd)).build()
  val storedConfig = repository.getConfig
  log.info("Repository = " + repository.getDirectory)


  def remotes: Seq[String] = {
    storedConfig.getSubsections("remote").toList
  }

  def remoteUrl(remoteName: String): String = {
    storedConfig.getString("remote", remoteName, "url")
  }

  def currentBranch: String =
    sys.env.get("CI_BRANCH").getOrElse(repository.getBranch())

  def lastCommit(): GitRevision = {
    val git = new Git(repository)
    val headRev = git.log().setMaxCount(1).call().head
    val id = headRev.getId
    val author = headRev.getAuthorIdent
    val committer = headRev.getCommitterIdent
    GitRevision(id.name,
      author.getName,
      author.getEmailAddress,
      committer.getName,
      committer.getEmailAddress,
      headRev.getShortMessage)
  }
}
