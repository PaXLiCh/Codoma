package ru.kolotnev.codoma;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.Queue;

public final class LineReader {
	private final Readable readable;
	private final Reader reader;
	private final char[] buf = new char[0x1000]; // 4K
	private final CharBuffer cbuf = CharBuffer.wrap(buf);
	private final Queue<String> lines = new LinkedList<>();
	private String eol = "";
	private boolean isConsistent = true;
	private String eolLast = "";
	private final LineBuffer lineBuf = new LineBuffer() {
		@Override
		protected void handleLine(String line, String end) {
			lines.add(line);
			eolLast = end;
			if (isConsistent) {
				if (eol.isEmpty()) {
					eol = end;
				} else {
					if (!eol.equals(end)) {
						isConsistent = false;
						eol = "BAD";
					}
				}
			}
		}
	};
	/**
	 * Creates a new instance that will read lines from the given
	 * {@code Readable} object.
	 */
	LineReader(Readable readable) {
		//this.readable = checkNotNull(readable);
		if (readable == null) {
			throw new NullPointerException();
		}
		this.readable = readable;
		this.reader = (readable instanceof Reader) ? (Reader) readable : null;
	}

	/**
	 * Reads a line of text. A line is considered to be terminated by any
	 * one of a line feed ({@code '\n'}), a carriage return
	 * ({@code '\r'}), or a carriage return followed immediately by a linefeed
	 * ({@code "\r\n"}).
	 *
	 * @return a {@code String} containing the contents of the line, not
	 * including any line-termination characters, or {@code null} if the
	 * end of the stream has been reached.
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public String readLine() throws IOException {
		while (lines.peek() == null) {
			cbuf.clear();
			// The default implementation of Reader#read(CharBuffer) allocates a
			// temporary char[], so we call Reader#read(char[], int, int) instead.
			int read = (reader != null)
					? reader.read(buf, 0, buf.length)
					: readable.read(cbuf);
			if (read == -1) {
				lineBuf.finish();
				break;
			}
			lineBuf.add(buf, 0, read);
		}
		return lines.poll();
	}

	public LineEnding getLineEndings() {
		return LineEnding.getByEnding(eol);
	}

	public String getLastEol() {
		return eolLast;
	}

	public enum LineEnding {
		LF(0, "\n"),
		CRLF(1, "\r\n"),
		CR(2, "\r"),
		INCONSISTENT(3, "BAD");

		private String lineEnd;
		private int index;

		LineEnding(final int index, final String lineTerminator) {
			this.index = index;
			lineEnd = lineTerminator;
		}

		public static LineEnding getByEnding(String ending) {
			switch (ending) {
				case "\r":
					return CR;
				case "\n":
					return LF;
				case "\r\n":
					return CRLF;
				default:
					return INCONSISTENT;
			}
		}

		public String getLineEnd() { return lineEnd; }

		public int getIndex() { return index; }
	}
}
