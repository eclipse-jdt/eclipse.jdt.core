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
		jdk 'temurin-jdk17-latest'
	}
	stages {
		stage('Build') {
			steps {
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
					-Pbuild-individual-bundles -Ptest-on-javase-20 -Pbree-libs -Papi-check \
					-Djava.io.tmpdir=$WORKSPACE/tmp -Dproject.build.sourceEncoding=UTF-8 \
					-Dtycho.surefire.argLine="--add-modules ALL-SYSTEM -Dcompliance=1.8,11,17,20 -Djdt.performance.asserts=disabled"
					"""
			}
			post {
				always {
					archiveArtifacts artifacts: '*.log,*/target/work/data/.metadata/*.log,*/tests/target/work/data/.metadata/*.log,apiAnalyzer-workspace/.metadata/*.log', allowEmptyArchive: true
					junit '**/target/surefire-reports/*.xml'
					discoverGitReferenceBuild referenceJob: 'eclipse.jdt.core-github/master'
					recordIssues publishAllIssues: true, tools: [java(), mavenConsole(), javaDoc()]
				}
			}
		}
	}
}
