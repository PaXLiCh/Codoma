package ru.kolotnev.codoma;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.kolotnev.codoma.SyntaxColor.SolarizedDarkSyntaxColor;
import ru.kolotnev.codoma.SyntaxColor.SyntaxColor;
import ru.kolotnev.codoma.TextSyntax.TextSyntax;

/**
 * Syntax colored EditText widget.
 */
public class ColoredEditText extends EditText {
	//region VARIABLES
	private static final String TAG = "ColoredEditText";
	private static final int
			ID_SELECT_ALL = android.R.id.selectAll,
			ID_CUT = android.R.id.cut,
			ID_COPY = android.R.id.copy,
			ID_PASTE = android.R.id.paste,
			SYNTAX_DELAY_MILLIS_SHORT = 250,
			SYNTAX_DELAY_MILLIS_LONG = 1500,
			CHARS_TO_COLOR = 2500,
			ID_UNDO = R.id.undo,
			ID_REDO = R.id.redo;
	private final Handler updateHandler = new Handler();
	private final TextPaint gutterPaintText = new TextPaint();
	private final TextPaint gutterPaintSelectedText = new TextPaint();
	private final Paint gutterPaintBack = new Paint();
	private final Paint gutterPaintSelectedBack = new Paint();
	private ColorUpdaterAsyncTask taskToUpdate = null;
	private final Runnable colorUpdater = new Runnable() {
		@Override
		public void run() {
			replaceTextKeepCursor(null);
		}
	};
	private boolean enabledChangeListener;
	private int selectionStart = -1;
	private int selectionEnd = -1;
	private boolean isNeedToUpdate = true;
	private GoodScrollView verticalScroll;
	private int deviceHeight;
	private int lineCount;
	private int startingLine;
	private LineUtils lineUtils;
	private KeyListener keyListener;
	private boolean[] isGoodLineArray;
	private int[] realLines;
	private TextView gutterView;
	private SyntaxColor.Style gutterStyleSelected;
	// TODO: цветовая схема (надо заменить её на загрузку цветовой темы)
	private SyntaxColor colorScheme = new SolarizedDarkSyntaxColor();
	private int firstVisibleIndex;
	private int lastVisibleIndex;
	private TextFile textFile;
	private boolean isHighlightEnabled;
	private final GoodScrollView.ScrollInterface scrollListener = new GoodScrollView.ScrollInterface() {
		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			updateTextSyntax(SYNTAX_DELAY_MILLIS_SHORT);
		}
	};
	private final TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			updateTextSyntax(SYNTAX_DELAY_MILLIS_LONG);
			// TODO: send correct invalidate
			((Activity) getContext()).invalidateOptionsMenu();
		}
	};
	private boolean isNeedLineNumbers;
	//endregion

	//region CONSTRUCTOR
	public ColoredEditText(final Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.e("Codoma", "CONSTRUCTOR");
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Log.e("Codoma", "ATTACHED TO WINDOW ");
	}

	private void setScrollListener(@Nullable final GoodScrollView.ScrollInterface listener) {
		if (verticalScroll == null) {
			Log.e(TAG, "vertical scroll == null");
		} else {
			verticalScroll.setScrollInterface(listener);
		}
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		super.onSelectionChanged(selStart, selEnd);
		//Log.e(TAG, "Selection changed " + selStart + ", " + selEnd);
		selectionStart = selStart;
		selectionEnd = selEnd;
		isNeedToUpdate = true;
	}

	public void setupEditor() {
		verticalScroll = (GoodScrollView) getRootView().findViewById(R.id.scroll_vertical);

		/*verticalScroll.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (ColoredEditText.this.hasFocus()) {
					ColoredEditText.this.clearFocus();
				}
				return false;
			}
		});*/

		setTextColor(colorScheme.getStyle(SyntaxColor.Scope.FOREGROUND).foreground);
		setBackgroundColor(colorScheme.getStyle(SyntaxColor.Scope.FOREGROUND).background);

		//setLayerType(View.LAYER_TYPE_NONE, null);

		final Context context = getContext();
		deviceHeight = getResources().getDisplayMetrics().heightPixels;

		isNeedLineNumbers = PreferenceHelper.getLineNumbers(context);
		isHighlightEnabled = PreferenceHelper.getSyntaxHighlight(context);

		gutterView = (TextView) getRootView().findViewById(R.id.edit_text_line_numbers);
		gutterView.setVisibility(isNeedLineNumbers ? View.VISIBLE : View.GONE);
		if (isNeedLineNumbers) {
			gutterView.setTextColor(colorScheme.getStyle(SyntaxColor.Scope.GUTTER).foreground);
			gutterView.setBackgroundColor(colorScheme.getStyle(SyntaxColor.Scope.GUTTER).background);

			gutterPaintText.setAntiAlias(true);
			gutterPaintText.setDither(false);
			gutterPaintText.setTextAlign(Paint.Align.RIGHT);
			gutterPaintText.setColor(colorScheme.getStyle(SyntaxColor.Scope.GUTTER).foreground);
			gutterPaintBack.setColor(colorScheme.getStyle(SyntaxColor.Scope.GUTTER).background);

			gutterPaintSelectedText.setAntiAlias(true);
			gutterPaintSelectedText.setDither(false);
			gutterPaintSelectedText.setTextAlign(Paint.Align.RIGHT);
			gutterPaintSelectedText.setColor(colorScheme.getStyle(SyntaxColor.Scope.GUTTER_SELECTED).foreground);
			gutterPaintSelectedBack.setColor(colorScheme.getStyle(SyntaxColor.Scope.GUTTER_SELECTED).background);

			gutterStyleSelected = colorScheme.getStyle(SyntaxColor.Scope.GUTTER_SELECTED);
		}

		lineUtils = new LineUtils();

		if (PreferenceHelper.getReadOnly(context)) {
			setReadOnly(true);
		} else {
			setReadOnly(false);
			if (PreferenceHelper.getSuggestionActive(context)) {
				setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_MULTI_LINE
						| InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
			} else {
				setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_MULTI_LINE
						| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
						| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
						| InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
			}
		}

		Typeface typeface = PreferenceHelper.getFont(context);
		if (typeface != null) {
			setTypeface(typeface);
			gutterView.setTypeface(typeface);
		}
		setTextSize(PreferenceHelper.getFontSize(context));
		gutterView.setTextSize(PreferenceHelper.getFontSize(context));

		setFocusable(true);
		setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!PreferenceHelper.getReadOnly(context)) {
					verticalScroll.tempDisableListener(1000);
					((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
							.showSoftInput(ColoredEditText.this, InputMethodManager.SHOW_IMPLICIT);
				}

			}
		});
		setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && !PreferenceHelper.getReadOnly(context)) {
					verticalScroll.tempDisableListener(1000);
					((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
							.showSoftInput(ColoredEditText.this, InputMethodManager.SHOW_IMPLICIT);
				}
			}
		});

		resetVariables();

		ViewTreeObserver vto = getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (!isNeedToUpdate) return;
				Layout l = getLayout();
				if (l != null) {
					updateLines(l);
					isNeedToUpdate = false;
				}
			}
		});
	}

	/**
	 * Attach ColoredEditText to text file.
	 *
	 * @param textFile
	 * 		ColoredEditText for editing file.
	 */
	public void setTextFile(@NonNull TextFile textFile) {
		this.textFile = textFile;
		replaceTextKeepCursor(textFile.getCurrentPageText());
	}

	public void smoothScrollTo(final int x, final int y) {
		verticalScroll.postDelayed(new Runnable() {
			@Override
			public void run() {
				verticalScroll.smoothScrollTo(x, y);
			}
		}, 200);
	}

	/**
	 * Set current line and place cursor at start of line.
	 *
	 * @param line
	 * 		New current line.
	 */
	public void setCurrentLine(int line) {
		line = line - textFile.getStartingLine();
		int fakeLine = lineUtils.fakeLineFromRealLine(line);
		Layout layout = getLayout();
		if (layout != null) {
			int offset = layout.getOffsetForHorizontal(line, 0);
			Log.e(TAG, "offset " + offset);
			setSelection(offset);
			requestFocus();
		}
		final int y = LineUtils.getYAtLine(verticalScroll, getLineCount(), fakeLine);
		smoothScrollTo(0, y);
	}

	public void setReadOnly(boolean value) {
		if (value) {
			keyListener = getKeyListener();
			setKeyListener(null);
		} else {
			if (keyListener != null)
				setKeyListener(keyListener);
		}
	}

	//region OVERRIDES
	@Override
	public void setTextSize(float size) {
		super.setTextSize(size);
		final float scale = getContext().getResources().getDisplayMetrics().density;
		gutterPaintText.setTextSize((int) (size * scale * 0.65f));
		gutterPaintSelectedText.setTextSize((int) (size * scale * 0.65f));
	}

	public int getFirstVisibleOffset() {
		return getLayout().getLineStart(LineUtils.getFirstVisibleLine(verticalScroll, getHeight(), getLineCount()));
	}

	public int getLastVisibleOffset() {
		return getLayout().getLineEnd(LineUtils.getLastVisibleLine(verticalScroll, getHeight(), getLineCount(), deviceHeight) - 1);
	}


	//endregion

	//region Other

	public void updateLines(Layout layout) {
		if (0 == getLineCount() || lineCount != getLineCount() || startingLine != textFile.getStartingLine()) {
			startingLine = textFile.getStartingLine();
			lineCount = getLineCount();
			if (lineCount == 0)
				lineCount = 1;

			//Log.e(TAG, "update lines must set new array");
			lineUtils.updateHasNewLineArray(
					textFile.getStartingLine(),
					lineCount,
					layout,
					getText().toString()
			);

			isGoodLineArray = lineUtils.getGoodLines();
			realLines = lineUtils.getRealLines();
		}

		if (!isNeedLineNumbers)
			return;

		if (gutterStyleSelected == null) { return; }

		boolean wrapContent = PreferenceHelper.getWrapContent(getContext());

		SpannableStringBuilder text = new SpannableStringBuilder();

		int[] selection = LineUtils.getCurrentCursorLines(layout, selectionStart, selectionEnd);

		int start = 0;
		for (int i = 0; i < lineCount; ++i) {
			// if last line we count it anyway
			int realLine = realLines[i];

			String lineNum;
			if (!wrapContent || isGoodLineArray[i]) {
				lineNum = "" + String.valueOf(realLine) + " \n";
			} else {
				lineNum = "\n";
			}
			text.append(lineNum);
			int end = start + lineNum.length();
			if (realLine >= selection[0] && realLine <= selection[1]) {
				text.setSpan(new ForegroundColorSpan(gutterStyleSelected.foreground), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.setSpan(new LineSpan(gutterStyleSelected.background), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			start = end;
		}

		gutterView.setText(text);
	}

	@Override
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
		// TODO: move to activity or fragment
		if (event.isCtrlPressed()) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_A:
					return onTextContextMenuItem(ID_SELECT_ALL);
				case KeyEvent.KEYCODE_X:
					return onTextContextMenuItem(ID_CUT);
				case KeyEvent.KEYCODE_C:
					return onTextContextMenuItem(ID_COPY);
				case KeyEvent.KEYCODE_V:
					return onTextContextMenuItem(ID_PASTE);
				case KeyEvent.KEYCODE_Z:
					/*if (getCanUndo()) {
						return onTextContextMenuItem(ID_UNDO);
					}*/
				case KeyEvent.KEYCODE_Y:
					/*if (getCanRedo()) {
						return onTextContextMenuItem(ID_REDO);
					}*/
				case KeyEvent.KEYCODE_S:
					// TODO: сохранение файла по горячке
					//((MainActivity)getContext()).saveTheFile(false);
					return true;
				default:
					return super.onKeyDown(keyCode, event);
			}
		} else {
			switch (keyCode) {
				case KeyEvent.KEYCODE_TAB:
					String textToInsert = "  ";
					int start, end;
					start = Math.max(getSelectionStart(), 0);
					end = Math.max(getSelectionEnd(), 0);
					getText().replace(Math.min(start, end), Math.max(start, end),
							textToInsert, 0, textToInsert.length());
					return true;
				default:
					return super.onKeyDown(keyCode, event);
			}
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
		if (event.isCtrlPressed()) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_A:
				case KeyEvent.KEYCODE_X:
				case KeyEvent.KEYCODE_C:
				case KeyEvent.KEYCODE_V:
				case KeyEvent.KEYCODE_Z:
				case KeyEvent.KEYCODE_Y:
				case KeyEvent.KEYCODE_S:
					return true;
				default:
					return false;
			}
		} else {
			switch (keyCode) {
				case KeyEvent.KEYCODE_TAB:
					return true;
				default:
					return false;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onTextContextMenuItem(final int id) {
		// TODO: move to activity or fragment
		if (id == ID_UNDO) {
			//undo();
			return true;
		} else if (id == ID_REDO) {
			//redo();
			return true;
		} else {
			return super.onTextContextMenuItem(id);
		}
	}

	//endregion

	public void resetVariables() {
		/*firstVisibleIndex = 0;
		firstColoredIndex = 0;*/
		lineCount = 0;
		startingLine = 0;
	}

	public LineUtils getLineUtils() {
		return lineUtils;
	}

	public void colorize(@NonNull TextSyntax textSyntax,
			@NonNull Editable e,
			@NonNull CharSequence textToHighlight,
			int start) {

		setSpan(textSyntax.getKeywords(), textToHighlight, start, e, SyntaxColor.Scope.KEYWORD);

		setSpan(textSyntax.getBuiltIns(), textToHighlight, start, e, SyntaxColor.Scope.BUILTIN);

		setSpan(textSyntax.getVariables(), textToHighlight, start, e, SyntaxColor.Scope.VARIABLE);

		setSpan(textSyntax.getComments(), textToHighlight, start, e, SyntaxColor.Scope.COMMENT);

		setSpan(textSyntax.getSymbols(), textToHighlight, start, e, SyntaxColor.Scope.PUNCTUATION);

		setSpan(textSyntax.getNumbers(), textToHighlight, start, e, SyntaxColor.Scope.NUMBER);

		setSpan(TextSyntax.LINK, textToHighlight, start, e, SyntaxColor.Scope.LINK);
	}

	private void setSpan(
			Pattern p,
			CharSequence textToHighlight,
			int start,
			Editable e,
			SyntaxColor.Scope scope) {
		if (p == null) return;
		SyntaxColor.Style style = colorScheme.getStyle(scope);
		for (Matcher m = p.matcher(textToHighlight); m.find(); ) {
			int from = start + m.start();
			int to = start + m.end();
			if (style.foreground != null) {
				e.setSpan(
						new ForegroundColorSpan(style.foreground),
						from,
						to,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (style.background != null) {
				e.setSpan(
						new BackgroundColorSpan(style.background),
						from,
						to,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

			if (style.typeface != Typeface.NORMAL) {
				e.setSpan(
						new StyleSpan(style.typeface),
						from,
						to,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

			if (style.isUnderline) {
				e.setSpan(
						new UnderlineSpan(),
						from,
						to,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	public void enableTextChangedListener() {
		if (enabledChangeListener) {return;}
		enabledChangeListener = true;
		addTextChangedListener(textWatcher);
		addTextChangedListener(textFile);
		setScrollListener(scrollListener);
	}

	public void disableTextChangedListener() {
		enabledChangeListener = false;
		removeTextChangedListener(textWatcher);
		removeTextChangedListener(textFile);
		setScrollListener(null);
	}

	/**
	 * Update syntax highlight.
	 *
	 * @param delay
	 * 		Delay before syntax update.
	 */
	public void updateTextSyntax(long delay) {
		if (!isHighlightEnabled
				// TODO: разобраться, можно ли выкинуть блокировку подсветки при выделенном тексте
				|| hasSelection()
				|| updateHandler == null
				|| colorUpdater == null)
			return;

		updateHandler.removeCallbacks(colorUpdater);
		updateHandler.postDelayed(colorUpdater, delay);
	}

	/**
	 * Set new text and try highlight syntax and save selection.
	 *
	 * @param textToUpdate
	 * 		New text.
	 */
	public void replaceTextKeepCursor(String textToUpdate) {
		if (taskToUpdate != null)
			taskToUpdate.cancel(true);

		Log.d(TAG, "updated highlight for file\n" + Log.getStackTraceString(new Exception()));

		taskToUpdate = new ColorUpdaterAsyncTask();
		taskToUpdate.execute(textToUpdate);
	}

	private static class LineSpan implements LineBackgroundSpan {
		private final int color;

		public LineSpan(int color) {
			this.color = color;
		}

		@Override
		public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline,
				int bottom, CharSequence text, int start, int end, int lnum) {
			final int paintColor = p.getColor();
			p.setColor(color);
			c.drawRect(new Rect(left, top, right, bottom), p);
			p.setColor(paintColor);
		}
	}

	private class ColorUpdaterAsyncTask extends AsyncTask<String, Void, Void> {
		private String textToUpdate = null;
		private Editable editable = null;

		private int cursorPos;
		private int cursorPosEnd;

		private Editable highlight(Editable editable, boolean newText) {
			editable.clearSpans();

			if (editable.length() == 0) {
				return editable;
			}

			int editorHeight = getHeight();

			if (!newText && editorHeight > 0) {
				firstVisibleIndex = getFirstVisibleOffset();
				lastVisibleIndex = getLastVisibleOffset();
			} else {
				firstVisibleIndex = 0;
				lastVisibleIndex = CHARS_TO_COLOR;
			}

			int firstColoredIndex = firstVisibleIndex - (CHARS_TO_COLOR / 5);

			// normalize
			if (firstColoredIndex < 0)
				firstColoredIndex = 0;
			if (lastVisibleIndex > editable.length())
				lastVisibleIndex = editable.length();
			if (firstColoredIndex > lastVisibleIndex)
				firstColoredIndex = lastVisibleIndex;

			CharSequence textToHighlight = editable.subSequence(firstColoredIndex, lastVisibleIndex);

			colorize(textFile.textSyntax, editable, textToHighlight, firstColoredIndex);

			return editable;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			cursorPos = getSelectionStart();
			cursorPosEnd = getSelectionEnd();

			disableTextChangedListener();

			editable = getEditableText();
		}

		@Override
		protected Void doInBackground(String... params) {
			if (params.length > 0)
				textToUpdate = params[0];

			Editable editable2 = textToUpdate == null ?
					new SpannableStringBuilder(editable.toString()) :
					new SpannableStringBuilder(textToUpdate);
			if (isHighlightEnabled) {
				editable = highlight(editable2, textToUpdate != null);
			} else {
				editable = editable2;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			setText(editable, TextView.BufferType.SPANNABLE);

			int newCursorPos;

			// Detect if cursor or whole selection visible
			boolean isSelectionStartVisible = cursorPos >= firstVisibleIndex && cursorPos <= lastVisibleIndex;
			boolean isSelectionEndVisible = cursorPosEnd >= firstVisibleIndex && cursorPosEnd <= lastVisibleIndex;

			// TODO: подумать над курсором и выделением, сделать так, чтобы при установке выделения не сбрасывался скролл
			if (isSelectionStartVisible) {
				// if the cursor is on screen
				// we don't change its position
				newCursorPos = cursorPos;
			} else {
				// else we set it to the first visible pos
				newCursorPos = cursorPosEnd;//firstVisibleIndex;
			}

			if (newCursorPos > -1 && newCursorPos <= length()) {
				if (cursorPosEnd != cursorPos && isSelectionStartVisible)
					setSelection(cursorPos, cursorPosEnd);
				else
					setSelection(newCursorPos);
			}/**/

			if (!isSelectionStartVisible && !isSelectionEndVisible) {
				// Prevent scrolling to selection
				if (hasFocus()) {
					clearFocus();
				}
			}

			enableTextChangedListener();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			enableTextChangedListener();
		}

		@Override
		protected void onCancelled(Void aVoid) {
			super.onCancelled(aVoid);

			enableTextChangedListener();
		}
	}
}
