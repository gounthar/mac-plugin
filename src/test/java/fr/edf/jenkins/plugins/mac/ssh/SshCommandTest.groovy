package fr.edf.jenkins.plugins.mac.ssh

import org.apache.sshd.common.SshException
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import com.trilead.ssh2.Connection

import fr.edf.jenkins.plugins.mac.MacHost
import fr.edf.jenkins.plugins.mac.MacUser
import fr.edf.jenkins.plugins.mac.ssh.SshCommand
import fr.edf.jenkins.plugins.mac.ssh.SshCommandException
import fr.edf.jenkins.plugins.mac.ssh.SshCommandLauncher
import fr.edf.jenkins.plugins.mac.ssh.connection.SSHClientFactory
import fr.edf.jenkins.plugins.mac.util.Constants
import hudson.util.Secret
import spock.lang.Specification

class SshCommandTest extends Specification {

    @Rule
    JenkinsRule jenkins

    // TODO : Get NoClassDefFoundException
//    def "createUserOnMac should not throw exception"() {
//        setup:
//        String label = "label"
//        MacHost macHost = Mock(MacHost)
//        Connection conn = Mock(Connection)
//        GroovySpy(SSHClientFactory, global:true)
//        1 * SSHClientFactory.getSshClient(*_) >> conn
//        GroovySpy(SSHCommandLauncher, global:true)
//        GroovySpy(SSHCommand, global:true)
//        1 * SSHCommandLauncher.executeCommand(conn, false, _) >> "OK"
//        1 * SSHCommand.isUserExist(conn, _) >> true
//        when:
//        MacUser user = SSHCommand.createUserOnMac(label, macHost)
//
//        then:
//        notThrown Exception
//        user != null
//    }

    def "createUserOnMac should throw exception because user does not exist after creation"() {
        setup:
        String label = "label"
        MacHost macHost = Mock(MacHost)
        Connection conn = Mock(Connection)
        
        GroovySpy(SSHClientFactory, global:true)
        1 * SSHClientFactory.getSshClient(*_) >> conn
        GroovySpy(SshCommandLauncher, global:true)
        1 * SshCommandLauncher.executeCommand(conn, false, _) >> "OK"
        1 * SshCommandLauncher.executeCommand(conn, true, _) >> "OK"

        when:
        MacUser user = SshCommand.createUserOnMac(label, macHost)

        then:
        SshCommandException e = thrown()
        e.getMessage().contains("Cannot create MacUser on host")
        user == null
    }

    def "deleteUserOnMac should works"() {
        setup:
        String username = "mac_user_test"
        MacHost macHost = Mock(MacHost)
        Connection conn = Mock(Connection)
        GroovySpy(SSHClientFactory, global:true)
        1 * SSHClientFactory.getSshClient(*_) >> conn
        GroovySpy(SshCommandLauncher, global:true)
        1 * SshCommandLauncher.executeCommand(conn, false, _) >> "OK"
        1 * SshCommandLauncher.executeCommand(conn, true, _) >> ""

        when:
        SshCommand.deleteUserOnMac(username, macHost)

        then:
        notThrown Exception
    }

    def "deleteUserOnMac should return exception because user still exist after command"() {
        setup:
        String username = "mac_user_test"
        MacHost macHost = Mock(MacHost)
        Connection conn = Mock(Connection)
        GroovySpy(SSHClientFactory, global:true)
        1 * SSHClientFactory.getSshClient(*_) >> conn
        GroovySpy(SshCommandLauncher, global:true)
        1 * SshCommandLauncher.executeCommand(conn, false, _) >> "OK"
//        1 * SSHCommandLauncher.executeCommand(conn, true, String.format("sudo dseditgroup -o checkmember -m %s %s", username, username)) >> "no mac_user_test is NOT a member of mac_user_test"
        1 * SshCommandLauncher.executeCommand(conn, true, String.format("dscl . list /Users | grep -v ^_ | grep %s", username)) >> username

        when:
        SshCommand.deleteUserOnMac(username, macHost)

        then:
        SshCommandException e = thrown()
        e.getMessage().contains("An error occured while deleting user " + username)
        e.getCause().getMessage().contains("The user " + username + " still exist after verification")
    }
    
//    def "deleteUserOnMac should not return exception if user still exist in group after command"() {
//        setup:
//        String username = "mac_user_test"
//        MacHost macHost = Mock(MacHost)
//        Connection conn = Mock(Connection)
//        GroovySpy(SSHClientFactory, global:true)
//        1 * SSHClientFactory.getSshClient(*_) >> conn
//        GroovySpy(SSHCommandLauncher, global:true)
//        3 * SSHCommandLauncher.executeCommand(conn, false, _) >> "OK"
//        1 * SSHCommandLauncher.executeCommand(conn, true, String.format("sudo dseditgroup -o checkmember -m %s %s", username, username)) >> "yes mac_user_test is a member of mac_user_test"
//        1 * SSHCommandLauncher.executeCommand(conn, true, String.format("dscl . list /Users | grep -v ^_ | grep %s", username)) >> ""
//        1 * SSHCommandLauncher.executeCommand(conn, true, String.format("sudo dseditgroup -o read %s", username)) >> ""
//
//        when:
//        SSHCommand.deleteUserOnMac(username, macHost)
//
//        then:
//        notThrown Exception
//    }
//    
//    def "deleteUserOnMac should not return exception if group still exist after command"() {
//        setup:
//        String username = "mac_user_test"
//        MacHost macHost = Mock(MacHost)
//        Connection conn = Mock(Connection)
//        GroovySpy(SSHClientFactory, global:true)
//        1 * SSHClientFactory.getSshClient(*_) >> conn
//        GroovySpy(SSHCommandLauncher, global:true)
//        3 * SSHCommandLauncher.executeCommand(conn, false, _) >> "OK"
//        1 * SSHCommandLauncher.executeCommand(conn, true, String.format("sudo dseditgroup -o checkmember -m %s %s", username, username)) >> "yes mac_user_test is a member of mac_user_test"
//        1 * SSHCommandLauncher.executeCommand(conn, true, String.format("dscl . list /Users | grep -v ^_ | grep %s", username)) >> ""
//        1 * SSHCommandLauncher.executeCommand(conn, true, String.format("sudo dseditgroup -o read %s", username)) >> username
//
//        when:
//        SSHCommand.deleteUserOnMac(username, macHost)
//
//        then:
//        SSHCommandException e = thrown()
//        e.getMessage().contains("An error occured while deleting user " + username)
//        e.getCause().getMessage().contains("The group " + username + " still exist after verification")
//    }

    def "jnlpConnect should works"() {
        setup:
        MacUser user = Mock(MacUser)
        MacHost macHost = Mock(MacHost)
        String slaveSecret = "secret"
        Connection conn = Mock(Connection)
        GroovySpy(SSHClientFactory, global:true)
        1 * SSHClientFactory.getUserClient(*_) >> conn
        GroovySpy(SshCommandLauncher, global:true)
        2 * SshCommandLauncher.executeCommand(conn, false, _) >> "OK"

        when:
        SshCommand.jnlpConnect(macHost, user, null, slaveSecret)

        then:
        notThrown Exception
    }

    def "jnlpConnect should throw exception"() {
        setup:
        MacUser user = new MacUser("test", Secret.fromString("password"), "workdir")
        MacHost macHost = new MacHost("host", "credentialsId", 0, 1, 5, 5, 5, false, 5)
        String slaveSecret = "secret"
        Connection conn = Mock(Connection)
        GroovySpy(SSHClientFactory, global:true)
        1 * SSHClientFactory.getUserClient(*_) >> conn

        when:
        SshCommand.jnlpConnect(macHost, user, null, slaveSecret)

        then:
        SshCommandException e = thrown()
        e.getMessage().contains("Cannot connect Mac " + macHost.host + " with user " + user.username + " to jenkins with JNLP")
    }

    def "listLabelUsers should works without exception"() {
        setup:
        String label = "label"
        MacHost macHost = new MacHost("host", "credentialsId", 0, 1, 5, 5, 5, false, 5)
        Connection conn = Mock(Connection)
        GroovySpy(SSHClientFactory, global:true)
        1 * SSHClientFactory.getSshClient(*_) >> conn
        GroovySpy(SshCommandLauncher, global:true)
        1 * SshCommandLauncher.executeCommand(conn, true, String.format(Constants.LIST_USERS, label+"_jenkins_")) >> ""

        when:
        SshCommand.listLabelUsers(macHost, label)

        then:
        notThrown Exception
    }
}