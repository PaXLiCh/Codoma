package ru.kolotnev.codoma;

import android.view.KeyEvent;

/**
 * Interprets shortcut key combinations and contains utility methods
 * to map Android keycodes to Unicode equivalents.
 */
public final class KeysInterpreter {
	public final static char EOF = '\uFFFF';
	public final static char NULL_CHAR = '\u0000';
	public final static char NEWLINE = '\n';
	public final static char BACKSPACE = '\b';
	public final static char TAB = '\t';
	public final static String GLYPH_NEWLINE = "\u21b5";
	public final static String GLYPH_SPACE = "\u00b7";
	public final static String GLYPH_TAB = "\u00bb";

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\n' || c == '\t' ||
				c == '\r' || c == '\f' || c == EOF);
	}

	public boolean isSentenceTerminator(char c) {
		return (c == '.');
	}

	public boolean isEscapeChar(char c) {
		return (c == '\\');
	}

	private final static char[] BASIC_C_OPERATORS = {
			'(', ')', '{', '}', '.', ',', ';', '=', '+', '-',
			'/', '*', '&', '!', '|', ':', '[', ']', '<', '>',
			'?', '~', '%', '^'
	};

	public static boolean isSwitchPanel(KeyEvent event) {
		return (event.isShiftPressed() &&
				(event.getKeyCode() == KeyEvent.KEYCODE_ENTER));
	}

	/**
	 * Maps shortcut keys and Android keycodes to printable characters.
	 * Note that whitespace is considered printable.
	 *
	 * @param event
	 * 		The KeyEvent to interpret
	 *
	 * @return The printable character the event represents,
	 * or Language.NULL_CHAR if the event does not represent a printable char
	 */
	// TODO: fix this interpreter!
	public static char keyEventToPrintableChar(KeyEvent event) {
		char c = NULL_CHAR;

		// convert tab, backspace, newline and space keycodes to standard ASCII values
		if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			c = NEWLINE;
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
			c = BACKSPACE;
		}
		// This should be before the check for isSpace() because the
		// shortcut for TAB uses the SPACE key.
		else if ((event.isShiftPressed() &&
				(event.getKeyCode() == KeyEvent.KEYCODE_SPACE)) ||
				(event.getKeyCode() == KeyEvent.KEYCODE_TAB)) {
			c = TAB;
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_SPACE) {
			c = ' ';
		} else if (event.isPrintingKey()) {
			c = (char) event.getUnicodeChar(event.getMetaState());
		}

		return c;
	}

	public static boolean isNavigationKey(KeyEvent event) {
		int keyCode = event.getKeyCode();
		return keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
				keyCode == KeyEvent.KEYCODE_DPAD_UP ||
				keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
				keyCode == KeyEvent.KEYCODE_DPAD_LEFT;
	}
}
