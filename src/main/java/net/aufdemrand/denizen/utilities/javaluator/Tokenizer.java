package net.aufdemrand.denizen.utilities.javaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A String tokenizer that accepts delimiters that are greater than one character.
 * @author Jean-Marc Astesana
 * @see <a href="../../../license.html">License information</a>
 */
public class Tokenizer {
	private Pattern pattern;
	private String tokenDelimiters;
	
	/** Constructor.
	 * @param delimiters the delimiters
	 */
	Tokenizer(List<String> delimiters) {
		if (onlyOneChar(delimiters)) {
			StringBuilder builder = new StringBuilder();
			for (String delimiter : delimiters) {
				builder.append(delimiter);
			}
			tokenDelimiters = builder.toString();
		} else {
			this.pattern = delimitersToRegexp(delimiters);
		}
	}
	
	/** Tests whether a String list contains only 1 character length elements.
	 * @param delimiters The list to test
	 * @return true if it contains only one char length elements (or no elements) 
	 */
	private boolean onlyOneChar(List<String> delimiters) {
		for (String delimiter : delimiters) {
			if (delimiter.length()!=1) return false;
		}
		return true;
	}

	private static Pattern delimitersToRegexp(List<String> delimiters) {
		// First, create a regular expression that match the union of the delimiters
		// Be aware that, in case of delimiters containing others (example && and &),
		// the longer may be before the shorter (&& should be before &) or the regexpr
		// parser will recognize && as two &.
		Collections.sort(delimiters, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return -o1.compareTo(o2);
			}
		});
		// Build a string that will contain the regular expression
		StringBuilder result = new StringBuilder();
		result.append('(');
		for (String delim : delimiters) { // For each delimiter
			if (result.length()!=1) result.append('|'); // Add it to the union
			for (int i=0;i<delim.length();i++) {
				// Add an escape character if the character is a regexp reserved char
				result.append('\\');
				result.append(delim.charAt(i));
			}
		}
		result.append(')');
		return Pattern.compile(result.toString());
	}

	/** Converts a string into tokens.
	 * @param string The string to be split into tokens
	 * @return The tokens
	 */
	Iterator<String> tokenize(String string) {
		if (pattern!=null) {
			List<String> res = new ArrayList<String>();
			Matcher m = pattern.matcher(string);
			int pos = 0;
			while (m.find()) { // While there's a delimiter in the string
				if (pos != m.start()) {
					// If there's something between the current and the previous delimiter
					// Add to the tokens list
					res.add(string.substring(pos, m.start()));
				}
				res.add(m.group()); // add the delimiter
				pos = m.end(); // Remember end of delimiter
			}
			if (pos != string.length()) {
				// If it remains some characters in the string after last delimiter
				res.add(string.substring(pos));
			}
			// Return the result
			return res.iterator();
		} else {
			final StringTokenizer tokens = new StringTokenizer(string, tokenDelimiters, true);
			return new Iterator<String>() {
				public boolean hasNext() {
					return tokens.hasMoreTokens();
				}
				public String next() {
					return tokens.nextToken();
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
}
