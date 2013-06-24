package com.obsglobal.util.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex utilities.
 */
public class RegexUtil {
	private RegexUtil() throws InstantiationException {
		throw new InstantiationException("RegexUtil");
	}

	public static List<MatchResult> findAllMatches(Pattern pattern, String input) {
		List<MatchResult> matchResults = new ArrayList<MatchResult>();
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			matchResults.add(matcher.toMatchResult());
		}

		return matchResults;
	}

	public static MatchResult findFirstMatch(Pattern pattern, String input) {
		Matcher matcher = pattern.matcher(input);
		return matcher.find() ? matcher.toMatchResult() : null;
	}
}
