package test;

import java.lang.annotation.Native;

public record TestRecord(
		@Deprecated
		@Native
		@SuppressWarnings(value = {
				"" })
		String name,
		@SuppressWarnings(value = { "" })
		String age) {
}