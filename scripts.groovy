import jenkins.model.*
import hudson.FilePath
backupPath = "/datas/shoeshop/backups/"
def node = Jenkins.getInstance().getNode(server)
def remoteDir = new FilePath(node.getChannel(), "${backupPath}")

def files = remoteDir.list()
def nameFile = files.collect {it.name}

if (action == "rollback") {
  return nameFile
}
