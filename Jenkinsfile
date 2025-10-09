pipeline {
	options {
		timeout(time: 90, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr: (env.BRANCH_NAME == 'master' || env.BRANCH_NAME ==~ 'BETA.*') ? '100':'5', artifactNumToKeepStr: (env.BRANCH_NAME == 'master' || env.BRANCH_NAME ==~ 'BETA.*') ? '15':'2'))
		disableConcurrentBuilds(abortPrevious: true)
		timestamps()
	}
	agent {
		label "ubuntu-latest"
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'openjdk-jdk26-latest'
	}
	stages {
		stage('Build') {
			steps {
					sh """#!/bin/bash -x
					
					java -version
					
					mkdir -p $WORKSPACE/tmp
					
					unset JAVA_TOOL_OPTIONS
					unset _JAVA_OPTIONS
					
					# The max heap should be specified for tycho explicitly
					# via configuration/argLine property in pom.xml
					# export MAVEN_OPTS="-Xmx2G"
					
					mvn clean install -f org.eclipse.jdt.core.compiler.batch -DlocalEcjVersion=99.99 -Dmaven.repo.local=$WORKSPACE/.m2/repository -DcompilerBaselineMode=disable -DcompilerBaselineReplace=none
					
					mvn -U clean verify --batch-mode --fail-at-end -Dmaven.repo.local=$WORKSPACE/.m2/repository \
						-Ptest-on-javase-26 -Pbree-libs -Papi-check -Pjavadoc -Pp2-repo \
						-Dmaven.test.failure.ignore=true \
						-Dcompare-version-with-baselines.skip=false \
						-Djava.io.tmpdir=$WORKSPACE/tmp -Dproject.build.sourceEncoding=UTF-8 \
						-Dtycho.surefire.argLine="--add-modules ALL-SYSTEM -Dcompliance=1.8,11,17,21,25,26 -Djdt.performance.asserts=disabled" \
						-DDetectVMInstallationsJob.disabled=true \
						-Dtycho.apitools.debug \
						-Dtycho.debug.artifactcomparator \
						-e \
						-Dcbi-ecj-version=99.99
					"""
			}
			post {
				always {
					archiveArtifacts artifacts: '*.log,*/target/work/data/.metadata/*.log,*/tests/target/work/data/.metadata/*.log,apiAnalyzer-workspace/.metadata/*.log,repository/target/repository/**,**/target/artifactcomparison/**', allowEmptyArchive: true
					// The following lines use the newest build on master that did not fail a reference
					// To not fail master build on failed test maven needs to be started with "-Dmaven.test.failure.ignore=true" it will then only marked unstable.
					// To not fail the build also "unstable: true" is used to only mark the build unstable instead of failing when qualityGates are missed
					// To accept unstable builds (test errors or new warnings introduced by third party changes) as reference using "ignoreQualityGate:true"
					// To only show warnings related to the PR on a PR using "publishAllIssues:false"
					discoverGitReferenceBuild referenceJob: 'eclipse.jdt.core-github/master'
					junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
					recordIssues publishAllIssues: false, ignoreQualityGate: true, enabledForFailure: true, tools: [
							eclipse(name: 'Compiler', pattern: '**/target/compilelogs/*.xml'),
							issues(name: 'API Tools', id: 'apitools', pattern: '**/target/apianalysis/*.xml'),
						], qualityGates: [[threshold: 1, type: 'DELTA', unstable: true]]
					recordIssues tools: [javaDoc(), mavenConsole()]
				}
			}
		}
	}
}
