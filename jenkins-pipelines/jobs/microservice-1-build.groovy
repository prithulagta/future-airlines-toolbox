multibranchPipelineJob('Microservice-1-ci-pipeline') {
    branchSources {
        branchSource {
            source {
                bitbucket {
                    credentialsId("credential-created-in-jenkins")
                    repoOwner("FutureAitlines")
                    repository("microservice-1")
                    traits {
                        headWildcardFilter {
                            includes("master release/* feature/* bugfix/*")
                            excludes("")
                        }
                    }
                    strategy {
                        defaultBranchPropertyStrategy {
                            props {
                            // keep only the last 10 builds
                            buildRetentionBranchProperty {
                                buildDiscarder {
                                logRotator {
                                    daysToKeepStr("-1")
                                    numToKeepStr("10")
                                }
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
    }
    // discover Branches (workaround due to JENKINS-46202)
    configure {
        def cron_string = JENKINS_QUIET_TEST != 'true' ? '* * * * *' : '0 0 5 31 2' // To stop duplicate jobs the cron is set to never run on test Jenkins
        def traits = it / sources / data / 'jenkins.branch.BranchSource' / source / traits
        traits << 'com.cloudbees.jenkins.plugins.bitbucket.BranchDiscoveryTrait' {
            strategyId(3) // detect all branches
        }
        
    }
    // check every minute for scm changes as well as new / deleted branches
    triggers {
      periodic(10)
    }
    factory {
        workflowBranchProjectFactory {
            scriptPath('Jenkinsfile')
        }
    }
    orphanedItemStrategy {
        discardOldItems {}
    }
}