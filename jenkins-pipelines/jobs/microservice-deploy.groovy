pipelineJob('MicroService-Deploy-Job') {
  description('Deploys the microservices application')
  parameters {
    booleanParam('DEPLOY_MULTIPLE_APPLICATIONS', false, 'Check if you want to deploy a release version to an envionment.')
    activeChoiceReactiveParam('APP_NAME') {
      description('Please select the application name. If DEPLOY_RELEASE is checked, All the applications part of the chosen release will be deployed.')
      choiceType('RADIO')
      groovyScript {
        script('''import groovy.json.JsonSlurper
          def scanCommand = "aws cli to list --output json --region eu-west-1"
          def output = scanCommand.execute().in.text
          def jsonOut = new JsonSlurper().parseText(output)
          def scanResults = []
          jsonOut.Items.each { entry ->
            def parsedEntry = [:]
            entry.each { field ->
              field.value.each{ key, value -> 
                parsedEntry.put(field.key, value)
              }
            }
          scanResults.add(parsedEntry)
          }
          sortedReducedScanResults = scanResults
                                      .sort{ it.app_name }
          ''')
        fallbackScript('return ["error"]')
      }
      referencedParameter('DEPLOY_MULTIPLE_APPLICATIONS')
    }
    activeChoiceReactiveReferenceParam('name') {
      description('Please enter the application version number.' )
      omitValueField()
      choiceType('FORMATTED_HTML')
      groovyScript {
          script('''if(!DEPLOY_MULTIPLE_APPLICATIONS) 
                {
                    if(!APP_NAME?.trim()) {
                      return "Enabled when an APP_NAME is selected"
                    } else {
                      return '<input name=\"value\" class=\"setting-input\" type=\"text\">'
                    } 
                } else {
                    return "Not Applicable"
                }''')
          fallbackScript('return ["error"]')
      }
      referencedParameter('DEPLOY_MULTIPLE_APPLICATIONS')
      referencedParameter('APP_NAME')
    }
    activeChoiceReactiveParam('ENV_NAME') {
      description('select your choice')
      choiceType('RADIO')
      groovyScript {
          script('''import groovy.json.JsonSlurper
              import jenkins.model.*
              nodes = Jenkins.instance.globalNodeProperties
              nodes.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)
              envVars = nodes[0].envVars
              def jenkins_env = envVars['JENKINS_ENVIRONMENT']
              def myList = []
              def output = ['bash', '-c', "aws cli"].execute().text
              def list = new JsonSlurper().parseText( output )
              list.each { myList.add(it) }
              return myList''')
          fallbackScript('return ["error"]')
      }
    }
  }
  definition {
    cpsScm {
      scm {
        def git_users = [
          'non_prod': 'service-acc-np',
          'prod': 'service-acc-p'
        ]
        git {
          branch('master')
          remote {
            credentials(git_users[JENKINS_ENVIRONMENT])
            url("https://${git_users[JENKINS_ENVIRONMENT]}@bitbucket.org/rbsmimoteam/reporitory.git")
          }
        }
      }
      scriptPath('pipelines/microServicesDeploy.pipeline')
    }
  }
}