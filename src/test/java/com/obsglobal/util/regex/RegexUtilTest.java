package com.obsglobal.util.regex;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import com.obsglobal.util.UtilTestHelper;
import org.junit.Test;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Unit test for {@link RegexUtil}.
 */
public class RegexUtilTest {
	public static final String INPUT = "abcABCfooabcDEFbarabcGHIfooabcZYXbar";

	@Test
	public void testSimpleFindAllMatches() throws Exception {
		List<MatchResult> matchResults = RegexUtil.findAllMatches(Pattern.compile("abc"), INPUT);
		assertEquals(4, matchResults.size());
		// first match
		MatchResult result = matchResults.get(0);
		assertEquals(0, result.start());
		assertEquals(3, result.end());
		// second match
		result = matchResults.get(1);
		assertEquals(9, result.start());
		assertEquals(12, result.end());
		// third match
		result = matchResults.get(2);
		assertEquals(18, result.start());
		assertEquals(21, result.end());
		// fourth match
		result = matchResults.get(3);
		assertEquals(27, result.start());
		assertEquals(30, result.end());
	}

	@Test
	public void testFindAllMatches() throws Exception {
		List<MatchResult> matchResults = RegexUtil.findAllMatches(Pattern.compile("foo|bar"), INPUT);
		assertEquals(4, matchResults.size());
		// first match
		MatchResult result = matchResults.get(0);
		assertEquals(6, result.start());
		assertEquals(9, result.end());
		// second match
		result = matchResults.get(1);
		assertEquals(15, result.start());
		assertEquals(18, result.end());
		// third match
		result = matchResults.get(2);
		assertEquals(24, result.start());
		assertEquals(27, result.end());
		// fourth match
		result = matchResults.get(3);
		assertEquals(33, result.start());
		assertEquals(36, result.end());
	}

	@Test
	public void testFindFirstMatch() throws Exception {
		MatchResult matchResult = RegexUtil.findFirstMatch(Pattern.compile("foo|bar"), INPUT);
		assertNotNull(matchResult);
		assertEquals(6, matchResult.start());
		assertEquals(9, matchResult.end());
	}

	@Test
	public void testFindNoFirstMatch() throws Exception {
		MatchResult matchResult = RegexUtil.findFirstMatch(Pattern.compile("jedi"), INPUT);
		assertNull(matchResult);
	}

	@Test
	public void testFindNoMatches() throws Exception {
		List<MatchResult> matchResults = RegexUtil.findAllMatches(Pattern.compile("jedi"), INPUT);
		assertNotNull(matchResults);
		assertEquals(0, matchResults.size());
	}

	@Test(expected = NullPointerException.class)
	public void testFirstNullPatternParameter() throws Exception {
		RegexUtil.findFirstMatch(null, INPUT);
	}

	@Test(expected = NullPointerException.class)
	public void testFirstNullInputParameter() throws Exception {
		RegexUtil.findFirstMatch(Pattern.compile("jedi"), null);
	}

	@Test(expected = NullPointerException.class)
	public void testAllNullPatternParameter() throws Exception {
		RegexUtil.findAllMatches(null, INPUT);
	}

	@Test(expected = NullPointerException.class)
	public void testAllNullInputParameter() throws Exception {
		RegexUtil.findAllMatches(Pattern.compile("jedi"), null);
	}

	@Test(expected = InstantiationException.class)
	public void testInstantiation() throws Exception {
		UtilTestHelper.testPrivateConstructor(RegexUtil.class);
	}
}
