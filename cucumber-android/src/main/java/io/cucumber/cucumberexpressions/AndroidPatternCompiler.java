package io.cucumber.cucumberexpressions;

import android.os.Build;

import java.util.regex.Pattern;

public class AndroidPatternCompiler implements PatternCompiler {

	private static final int UNICODE_CHARACTER_CLASS = 0x100;

	@Override
	public Pattern compile(String regexp, int flags) {
		return Pattern.compile(regexp, flags & ~(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Pattern.UNICODE_CHARACTER_CLASS : UNICODE_CHARACTER_CLASS));
	}
}
