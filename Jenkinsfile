pipeline {
	options {
		timeout(time: 140, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'5'))
		timestamps()
	}
	agent {
		label "centos-7"
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'openjdk-jdk11-latest'
	}
	stages {
		stage('Build') {
			steps {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					sh """
					mvn -f pom.xml -U clean verify --batch-mode -Pbuild-individual-bundles -Pbree-libs -Ptest-on-javase-17 -Papi-check \
          -Dmaven.repo.local=$WORKSPACE/.m2/repository \
          -Dtycho.surefire.argLine="--add-modules ALL-SYSTEM -Dcompliance=1.8,11,17 -Djdt.performance.asserts=disabled" 
					"""
				}
			}
			post {
				always {
					archiveArtifacts artifacts: '*.log,*/target/work/data/.metadata/*.log,*/tests/target/work/data/.metadata/*.log,apiAnalyzer-workspace/.metadata/*.log', allowEmptyArchive: true
					recordIssues aggregatingResults: true, enabledForFailure: true, qualityGates: [[threshold: 1, type: 'DELTA', unstable: false]], tools: [acuCobol()]
					publishIssues issues:[scanForIssues(tool: java()), scanForIssues(tool: mavenConsole())]
					junit '**/target/surefire-reports/*.xml'
				}
			}
		}
		stage('Check freeze period') {
			when {
				not {
					branch 'master'
				}
			}
			steps {
				sh "wget https://download.eclipse.org/eclipse/relengScripts/scripts/verifyFreezePeriod.sh"
				sh "chmod +x verifyFreezePeriod.sh"
				withCredentials([string(credentialsId: 'google-api-key', variable: 'GOOGLE_API_KEY')]) {
					sh './verifyFreezePeriod.sh'
				}
			}
		}
	}
}
