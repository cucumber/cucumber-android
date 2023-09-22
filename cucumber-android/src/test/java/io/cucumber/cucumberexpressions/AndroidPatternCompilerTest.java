package io.cucumber.cucumberexpressions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ServiceLoader;
import java.util.regex.Pattern;

public class AndroidPatternCompilerTest {

	@Test
	public void compiles_pattern_only_with_supported_flag() {

		AndroidPatternCompiler compiler = (AndroidPatternCompiler) ServiceLoader.load(PatternCompiler.class).iterator().next();

		Pattern pattern = compiler.compile("HELLO", Pattern.UNICODE_CHARACTER_CLASS);

		assertFalse(pattern.matcher("hello").find());


		pattern = compiler.compile("HELLO", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);

		assertTrue(pattern.matcher("hello").find());
	}
}