// TEMPORARY DIAGNOSTIC ONLY. Restore f980c629ba7891343d60884a2ef3359ba8b93345 after this run.
// This pipeline deliberately fails and does not represent a JDT regression result.
pipeline {
	options {
		timeout(time: 6, unit: 'MINUTES')
		disableConcurrentBuilds(abortPrevious: true)
		timestamps()
	}
	agent { label 'ubuntu-latest' }
	tools {
		maven 'apache-maven-latest'
		jdk 'openjdk-jdk26-latest'
	}
	stages {
		stage('Infrastructure probe - not regression validation') {
			steps {
				sh '''#!/usr/bin/env bash
set -eu
exec > >(tee ci-startup-probe.log) 2>&1
unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS
work=$(mktemp -d "${WORKSPACE:-/tmp}/jdt-startup-probe.XXXXXX")
trap 'rm -rf -- "$work"' EXIT
printf 'JDT_INFRA_PROBE: no JDT regression tests are executed by this diagnostic run\n'
date -u; uname -a; uptime
for name in /proc/self/cgroup /proc/loadavg /sys/fs/cgroup/cpu.max /sys/fs/cgroup/cpu.stat /sys/fs/cgroup/cpu.pressure /sys/fs/cgroup/memory.max /sys/fs/cgroup/memory.current /sys/fs/cgroup/memory.events /sys/fs/cgroup/cpuset.cpus.effective /sys/fs/cgroup/cpu/cpu.cfs_quota_us /sys/fs/cgroup/cpu/cpu.cfs_period_us; do
  if [[ -r "$name" ]]; then printf '\n%s\n' "$name"; head -c 4096 "$name"; fi
done
timeout 30 java -XshowSettings:system -version 2>&1
cat > "$work/JdtStartupProbe.java" <<'JAVA'
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
public class JdtStartupProbe {
  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      System.out.println("child processors=" + Runtime.getRuntime().availableProcessors());
      return;
    }
    System.out.println("parent runtime=" + Runtime.version() + " vendor=" + System.getProperty("java.vendor")
        + " processors=" + Runtime.getRuntime().availableProcessors() + " maxHeap=" + Runtime.getRuntime().maxMemory());
    for (int i = 0; i < 3; i++) {
      long start = System.nanoTime();
      var addresses = java.net.InetAddress.getAllByName("localhost");
      System.out.println("localhost_ms=" + (System.nanoTime() - start) / 1_000_000.0 + " addresses=" + Arrays.toString(addresses));
    }
    String[][] options = { {}, {"-XX:ActiveProcessorCount=2"}, {"-XX:+UseSerialGC"}, {"-Xint"} };
    String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    for (String[] option : options) {
      for (int i = 0; i < 3; i++) {
        List<String> command = new ArrayList<>(); command.add(java); command.addAll(Arrays.asList(option));
        command.addAll(List.of("-cp", System.getProperty("java.class.path"), "JdtStartupProbe", "child"));
        long start = System.nanoTime();
        Process child = new ProcessBuilder(command).redirectErrorStream(true).start();
        try {
          if (!child.waitFor(15, TimeUnit.SECONDS)) throw new AssertionError("Probe child did not exit: " + Arrays.toString(option));
          System.out.println("startup options=" + Arrays.toString(option) + " run=" + i + " exit=" + child.exitValue()
              + " elapsed_ms=" + (System.nanoTime() - start) / 1_000_000.0 + " " + new String(child.getInputStream().readAllBytes()).strip());
          if (child.exitValue() != 0) throw new AssertionError("Probe child failed");
        } finally {
          if (child.isAlive()) { child.destroyForcibly(); child.waitFor(5, TimeUnit.SECONDS); }
        }
      }
    }
    Path directory = Path.of(System.getProperty("java.class.path"));
    long start = System.nanoTime();
    for (int i = 0; i < 500; i++) {
      Path file = directory.resolve("io-" + i);
      Files.writeString(file, "class X { public static void main(String[] args) {} }");
      Files.readAllBytes(file); Files.delete(file);
    }
    System.out.println("500_small_file_write_read_delete_ms=" + (System.nanoTime() - start) / 1_000_000.0);
  }
}
JAVA
timeout 30 javac -d "$work" "$work/JdtStartupProbe.java"
timeout 210 java -cp "$work" JdtStartupProbe
for name in /sys/fs/cgroup/cpu.stat /sys/fs/cgroup/cpu.pressure /proc/loadavg; do
  if [[ -r "$name" ]]; then printf '\nAFTER %s\n' "$name"; head -c 4096 "$name"; fi
done
printf '\nJDT_INFRA_PROBE_COMPLETE: intentional exit 86; diagnostics only, NOT a JDT build/test result.\n'
exit 86
'''
			}
		}
	}
	post {
		always {
			archiveArtifacts artifacts: 'ci-startup-probe.log', allowEmptyArchive: true
		}
	}
}
