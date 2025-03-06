package org.eclipse.jdt.core.tests.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemHandler;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class ProblemReporterTest extends TestCase {

	public ProblemReporterTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ProblemReporterTest.class.getPackageName());
		suite.addTest(new TestSuite(ProblemReporterTest.class));
		return suite;
	}

	private static int[] getAllProblemIds() {
		List<Integer> problemIds = new ArrayList<>();
		for (Field field : IProblem.class.getFields()) {
            try {
                if (field.getType() == int.class) {
                    problemIds.add(field.getInt(null));
                }
            } catch (IllegalAccessException e) {
                System.err.println("Could not access problem ID: " + field.getName());
            }
        }
		return problemIds.stream().mapToInt(Integer::intValue).toArray();
	}
	protected Hashtable<String, String> getDefaultJavaCoreOptions() {
		return JavaCore.getDefaultOptions();
	}

	public void testMessageTemplates() {
		Hashtable<String, String> defaultOptions = getDefaultJavaCoreOptions();
		CompilerOptions compilerOptions = new CompilerOptions(defaultOptions);
		ProblemReporter reporter = new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				compilerOptions,
				new DefaultProblemFactory(Locale.getDefault())) {
//					@Override
//				    public void handle(int problemId,
//				    		String[] problemArguments,
//				    		String[] messageArguments,
//				    		int problemStartPosition,
//				    		int problemEndPosition,
//				    		ReferenceContext referenceContext1,
//				    		CompilationResult unitResult) {
//						System.out.println("sasi");
//
//					}
				};
		int[] problemIDs = getAllProblemIds();
		int pCount = 0;
		CompilationUnitDeclaration dummyContext = new CompilationUnitDeclaration(null, null, 0);  // Create a mock context
		CompilationResult unitResult = new CompilationResult("Test.java".toCharArray(), 0, 0, 10);
		for (int problemId : problemIDs) {
			try {
				int messageKey = ProblemMessageUtil.getMessageKey(problemId);
				if(messageKey !=0) {
					String messageTemplate = ProblemMessageUtil.getMessageTemplate(messageKey);
					if (messageTemplate != "Message not found" ) {
						int expectedArgs = ProblemMessageUtil.getPlaceholderCount(messageTemplate);

						String[] validArgs = new String[expectedArgs];
		                for (int i = 0; i < expectedArgs; i++) {
		                    validArgs[i] = "validArgs" + i;
		                }
		                System.out.println("sam="+problemId);
		                reporter.handle(problemId, validArgs, validArgs, 0, 1, dummyContext, unitResult);
		                pCount++;
		                if (expectedArgs == 1) {
		                	try {
		                		reporter.handle(problemId, ProblemHandler.NoArgument, ProblemHandler.NoArgument, 0, 1, dummyContext, unitResult);
		                		System.out.println("Warning: AIOOBE did not occur for problemId: " + problemId);
//		                        fail("AIOOBE expected but did not occur for problemId: " + problemId);
		                	} catch (ArrayIndexOutOfBoundsException e) {
		                		System.out.println("Expected AIOOBE detected for problemId: " + problemId);
		                	}
		                }
					}
				}
			} catch (Exception e) {
				System.err.println("Unexpected error for problemId: " + problemId + " - " + e.getMessage());
			}
		}
		assertEquals("Problem IDs count does not match with message templates", pCount, 958);

	}

}

class ProblemMessageUtil {
    private static final String MESSAGES_FILE = "messages.properties";

    private static final int TYPE_RELATED = 0x01000000;
    private static final int FIELD_RELATED = 0x02000000;
    private static final int METHOD_RELATED = 0x04000000;
    private static final int CONSTRUCTOR_RELATED = 0x08000000;
    private static final int IMPORT_RELATED = 0x10000000;
    private static final int INTERNAL = 0x20000000;
    private static final int SYNTAX = 0x40000000;
    private static final int JAVADOC = 0x80000000;
    private static final int MODULE_RELATED = 0x00800000;
    private static final int COMPLIANCE = 0x00400000;
    private static final int PREVIEW_RELATED = 0x00200000;
    // Load messages.properties file
    public static Properties loadMessages() {
        Properties properties = new Properties();
        //try (InputStream input = ProblemReporter.class.getClassLoader().getResourceAsStream(MESSAGES_FILE)) {
        try (InputStream input = ProblemReporter.class.getResourceAsStream(MESSAGES_FILE)) {
            if (input == null) {
                System.err.println("Could not find " + MESSAGES_FILE);
                return properties;
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    // Count {X} placeholders in messages
    public static int getPlaceholderCount(String message) {
    	if (message == null) return 0;

        Matcher matcher = Pattern.compile("\\{\\d+\\}").matcher(message);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    public static int getMessageKey(int problemId) {
        int offset = extractOffset(problemId);

        if (offset <= 0) {
            //System.out.println("Invalid offset for problemId: " + problemId);
            return 0;
        }
        return Integer.parseInt(String.valueOf(offset)); // The message key
    }

    public static int extractOffset(int problemId) {
        int[] categories = {
        		TYPE_RELATED, FIELD_RELATED, METHOD_RELATED, CONSTRUCTOR_RELATED,
        		IMPORT_RELATED, INTERNAL, SYNTAX, JAVADOC, MODULE_RELATED, COMPLIANCE, PREVIEW_RELATED
        };

        int offset = problemId;

        // Remove all known categories
        for (int category : categories) {
            if ((problemId & category) == category) {
                offset -= category;
            }
        }

        return offset; // The remaining value is the message key
    }
    public static String getMessageTemplate(int problemId) {
        int messageKey = extractOffset(problemId);

        if (messageKey <= 0) {
            System.out.println("Invalid message key for problemId: " + problemId);
            return "Unknown Problem ID";
        }

        // Load messages from properties file
        Properties messages = ProblemMessageUtil.loadMessages();
        String messageTemplate = messages.getProperty(String.valueOf(messageKey));

        if (messageTemplate == null) {
            //System.out.println("No message found for key: " + messageKey);
            return "Message not found";
        }

        return messageTemplate;
    }

}
