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
		jdk 'openjdk-jdk21-latest'
	}
	stages {
		stage('Build') {
			steps {
					sh """#!/bin/bash -x
					
					# /opt/tools/java/openjdk/jdk-11/latest/bin/java -version
					java -version
					
					mkdir -p $WORKSPACE/tmp
					
					unset JAVA_TOOL_OPTIONS
					unset _JAVA_OPTIONS
					
					# The max heap should be specified for tycho explicitly
					# via configuration/argLine property in pom.xml
					# export MAVEN_OPTS="-Xmx2G"
					
					mvn clean install -f org.eclipse.jdt.core.compiler.batch -DlocalEcjVersion=99.99 -Dmaven.repo.local=$WORKSPACE/.m2/repository -DcompilerBaselineMode=disable -DcompilerBaselineReplace=none
					
					mvn -U clean verify --batch-mode --fail-at-end -Dmaven.repo.local=$WORKSPACE/.m2/repository \
					-Ptest-on-javase-21 -Pbree-libs -Papi-check \
					-Dcompare-version-with-baselines.skip=false \
					-Djava.io.tmpdir=$WORKSPACE/tmp -Dproject.build.sourceEncoding=UTF-8 \
					-Dtycho.surefire.argLine="--add-modules ALL-SYSTEM -Dcompliance=1.8,11,17,20,21 -Djdt.performance.asserts=disabled" \
					-DDetectVMInstallationsJob.disabled=true \
					-Dtycho.apitools.debug \
					-Dcbi-ecj-version=99.99
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
