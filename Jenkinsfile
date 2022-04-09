pipeline {
	options {
		timeout(time: 60, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'15'))
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
					sh """#!/bin/bash -x
					
					/opt/tools/java/openjdk/jdk-11/latest/bin/java -version
					java -version
					
					df -k
					mkdir -p $WORKSPACE/tmp
					
					unset JAVA_TOOL_OPTIONS
					unset _JAVA_OPTIONS
					MAVEN_OPTS="-Xmx2G"
					mvn -U clean verify --batch-mode --fail-at-end -Dmaven.repo.local=$WORKSPACE/.m2/repository \
					-Pbuild-individual-bundles -Ptest-on-javase-17 -Pbree-libs -Papi-check -Dtycho.version=2.7.0 \
					-Djava.io.tmpdir=$WORKSPACE/tmp -Dcompare-version-with-baselines.skip=false -Dproject.build.sourceEncoding=UTF-8 \
					-Dtycho.surefire.argLine="--add-modules ALL-SYSTEM -Dcompliance=1.8,11,17 -Djdt.performance.asserts=disabled" 
					"""
				}
			}
			post {
				always {
					sh"""df -k
					"""
					archiveArtifacts artifacts: '*.log,*/target/work/data/.metadata/*.log,*/tests/target/work/data/.metadata/*.log,apiAnalyzer-workspace/.metadata/*.log', allowEmptyArchive: true
					recordIssues aggregatingResults: true, enabledForFailure: true, qualityGates: [[threshold: 1, type: 'DELTA', unstable: false]], tools: [acuCobol()]
					publishIssues issues:[scanForIssues(tool: java()), scanForIssues(tool: mavenConsole())]
					junit '**/target/surefire-reports/*.xml'
				}
			}
		}
	}
}
