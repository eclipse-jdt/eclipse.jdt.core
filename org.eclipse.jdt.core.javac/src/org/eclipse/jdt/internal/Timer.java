/*******************************************************************************
 * Copyright (c) 2025, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;

public class Timer {
	Map<String, Duration> durations = new HashMap<>();
	Deque<Map.Entry<String, Instant>> started = new LinkedList<>();
	public void reportDuration(String id, Runnable r) {
		Instant before = Instant.now();
		r.run();
		Duration d = Duration.between(before, Instant.now());
		System.err.println(
			id + " current:" + d.toMillis() + " total:" + durations.compute(id, (_, prev) -> prev == null ? d : prev.plus(d)).toMillis());
	}
	public <T> T reportDuration(String id, Supplier<T> r) {
		Instant before = Instant.now();
		T res = r.get();
		Duration d = Duration.between(before, Instant.now());
		System.err.println(
				id + " current:" + d.toMillis() + " total:" + durations.compute(id, (_, prev) -> prev == null ? d : prev.plus(d)).toMillis());
		return res;
	}
	public void start(String id) {
		started.push(new SimpleEntry<>(id, Instant.now()));
	}
	public void stopCurrent() {
		if (!started.isEmpty()) {
			Entry<String, Instant> entry = started.pop();
			String current = entry.getKey();
			Duration d = Duration.between(entry.getValue(), Instant.now());
			System.err.println(
					current + " current:" + d.toMillis() + " total:" + durations.compute(current, (_, prev) -> prev == null ? d : prev.plus(d)).toMillis());
		}
	}
	public void stopLast(String id) {
		Entry<String, Instant> e = this.started.reversed().stream().filter(entry -> Objects.equals(id, entry.getKey())).findFirst().orElse(null);
		if (e != null) {
			started.remove(e);
			String current = e.getKey();
			Duration d = Duration.between(e.getValue(), Instant.now());
			System.err.println(
					current + " current:" + d.toMillis() + " total:" + durations.compute(current, (_, prev) -> prev == null ? d : prev.plus(d)).toMillis());

		}
	}
}