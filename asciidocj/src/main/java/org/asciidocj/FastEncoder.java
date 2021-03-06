/*
 * Copyright (c) 2013 Ricardo Arguello
 *
 * Based on pegdown (c) 2010-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidocj;

import java.util.Random;

import org.parboiled.common.StringUtils;

/**
 * Static class holding simple HTML encoding logic.
 */
public class FastEncoder {

	private static Random random = new Random(0x2626);

	public static String encode(String string) {
		if (StringUtils.isNotEmpty(string)) {
			for (int i = 0; i < string.length(); i++) {
				if (encode(string.charAt(i)) != null) {
					// we have at least one character that needs encoding, so do
					// it one by one
					StringBuilder sb = new StringBuilder();
					for (i = 0; i < string.length(); i++) {
						char c = string.charAt(i);
						String encoded = encode(c);
						if (encoded != null)
							sb.append(encoded);
						else
							sb.append(c);
					}
					return sb.toString();
				}
			}
			return string;
		} else
			return "";
	}

	public static void encode(String string, StringBuilder sb) {
		if (StringUtils.isNotEmpty(string)) {
			for (int i = 0; i < string.length(); i++) {
				if (encode(string.charAt(i)) != null) {
					// we have at least one character that needs encoding, so do
					// it one by one
					for (i = 0; i < string.length(); i++) {
						char c = string.charAt(i);
						String encoded = encode(c);
						if (encoded != null)
							sb.append(encoded);
						else
							sb.append(c);
					}
					return;
				}
			}
			sb.append(string);
		}
	}

	public static String encode(char c) {
		switch (c) {
		case '&':
			return "&amp;";
		case '<':
			return "&lt;";
		case '>':
			return "&gt;";
		case '"':
			return "&quot;";
		case '\'':
			return "&#39;";
		}
		return null;
	}

	public static String obfuscate(String email) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < email.length(); i++) {
			char c = email.charAt(i);
			switch (random.nextInt(5)) {
			case 0:
			case 1:
				sb.append("&#").append((int) c).append(';');
				break;
			case 2:
			case 3:
				sb.append("&#x").append(Integer.toHexString(c)).append(';');
				break;
			case 4:
				String encoded = encode(c);
				if (encoded != null)
					sb.append(encoded);
				else
					sb.append(c);
			}
		}
		return sb.toString();
	}
}