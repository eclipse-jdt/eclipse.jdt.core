package test1;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;

public class Test {
    static Set<@NonNull String> test1(Set<String> args) {
        @NonNull Set<@NonNull String> mapped = args.stream().collect(Collectors.toSet());
        return mapped;
    }
    public static void main(String[] args) {
		Set<String> set = Collections.singleton(null);
		for (@NonNull String s : test1(set))
			System.out.println(s.toUpperCase());
	}
}
