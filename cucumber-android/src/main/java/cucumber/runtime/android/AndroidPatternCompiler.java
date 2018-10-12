package cucumber.runtime.android;

import io.cucumber.cucumberexpressions.PatternCompiler;

import java.util.regex.Pattern;

public class AndroidPatternCompiler implements PatternCompiler {

	@Override
	public Pattern compile(String regexp, int flags) {
		return Pattern.compile(regexp,flags& ~Pattern.UNICODE_CHARACTER_CLASS);
	}
}
