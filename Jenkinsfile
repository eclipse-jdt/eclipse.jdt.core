pipeline {
	options {
		timeout(time: 90, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'15'))
		disableConcurrentBuilds(abortPrevious: true)
		timestamps()
	}
	agent {
		label "centos-latest"
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'openjdk-jdk17-latest'
	}
	stages {
		stage('Build') {
			steps {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					sh """#!/bin/bash -x
					
					/opt/tools/java/openjdk/jdk-11/latest/bin/java -version
					java -version
					
					mkdir -p $WORKSPACE/tmp
					
					unset JAVA_TOOL_OPTIONS
					unset _JAVA_OPTIONS
					
					# The max heap should be specified for tycho explicitly
					# via configuration/argLine property in pom.xml
					# export MAVEN_OPTS="-Xmx2G"
					
					mvn -U clean verify --batch-mode --fail-at-end -Dmaven.repo.local=$WORKSPACE/.m2/repository \
					-Pbuild-individual-bundles -Ptest-on-javase-18 -Pbree-libs -Papi-check \
					-Djava.io.tmpdir=$WORKSPACE/tmp -Dproject.build.sourceEncoding=UTF-8 \
					-Dtycho.surefire.argLine="--add-modules ALL-SYSTEM -Dcompliance=1.8,11,17,18 -Djdt.performance.asserts=disabled"
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
	}
}
