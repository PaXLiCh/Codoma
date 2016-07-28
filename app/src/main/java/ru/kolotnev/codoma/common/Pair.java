package ru.kolotnev.codoma.common;

public final class Pair {
	private int _first;
	private int _second;

	public Pair(int x, int y) {
		_first = x;
		_second = y;
	}

	public final int getFirst() { return _first; }

	public final void setFirst(int value) { _first = value; }

	public final int getSecond() { return _second; }

	public final void setSecond(int value) { _second = value; }
}
