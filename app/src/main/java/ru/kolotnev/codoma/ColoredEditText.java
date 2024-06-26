package ru.kolotnev.codoma;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.kolotnev.codoma.SyntaxColor.Base16TomorrowDaySyntaxColor;
import ru.kolotnev.codoma.SyntaxColor.Base16TomorrowNightSyntaxColor;
import ru.kolotnev.codoma.SyntaxColor.SyntaxColor;
import ru.kolotnev.codoma.TextSyntax.CSSTextSyntax;
import ru.kolotnev.codoma.TextSyntax.CppTextSyntax;
import ru.kolotnev.codoma.TextSyntax.CsharpTextSyntax;
import ru.kolotnev.codoma.TextSyntax.GLSLTextSyntax;
import ru.kolotnev.codoma.TextSyntax.HTMLTextSyntax;
import ru.kolotnev.codoma.TextSyntax.JavaTextSyntax;
import ru.kolotnev.codoma.TextSyntax.LUATextSyntax;
import ru.kolotnev.codoma.TextSyntax.ObjectiveCTextSyntax;
import ru.kolotnev.codoma.TextSyntax.PHPTextSyntax;
import ru.kolotnev.codoma.TextSyntax.PlainTextSyntax;
import ru.kolotnev.codoma.TextSyntax.PythonTextSyntax;
import ru.kolotnev.codoma.TextSyntax.SQLTextSyntax;
import ru.kolotnev.codoma.TextSyntax.TextSyntax;

/**
 * Syntax colored EditText widget.
 */
public class ColoredEditText extends androidx.appcompat.widget.AppCompatEditText {
	//region VARIABLES
	private static final String TAG = "ColoredEditText";
	private static final int
			ID_SELECT_ALL = android.R.id.selectAll,
			ID_CUT = android.R.id.cut,
			ID_COPY = android.R.id.copy,
			ID_PASTE = android.R.id.paste,
			SYNTAX_DELAY_MILLIS_SHORT = 250,
			CHARS_TO_COLOR = 2500,
			ID_UNDO = R.id.action_undo,
			ID_REDO = R.id.action_redo;
	private int updateDelay = 1000;
	private final Handler updateHandler = new Handler();
	private ColorUpdaterAsyncTask taskToUpdate = null;
	private final Runnable colorUpdater = () -> replaceTextKeepCursor(null);
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
	// TODO: цветовая схема (надо заменить её на загрузку цветовой темы)
	private SyntaxColor colorScheme;
	private SyntaxColor.Style styleWhitespaces;
	private TextFile textFile;
	private boolean isHighlightEnabled;
	private boolean isTabAsSpaces = false;
	private String tabulation;
	private int tabWidth = 0;
	private int tabWidthInPixels = 0;
	private int spaceWidthInPixels = 0;
	private boolean isWhitespaces = false;
	@Nullable
	public SearchResult searchResult;
	private boolean isEditedNotBySearch = true;

	@Nullable
	public TextSyntax textSyntax;

	private final GoodScrollView.ScrollInterface scrollListener = (l, t, oldl, oldt) -> {
        // TODO: разобраться, можно ли выкинуть блокировку подсветки при выделенном тексте
        if (hasSelection())
            return;

        cancelUpdate();
        updateHandler.postDelayed(colorUpdater, SYNTAX_DELAY_MILLIS_SHORT);
    };
	private final TextWatcher textWatcher = new TextWatcher() {
		private int start = 0;
		private int count = 0;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			/* do nothing */
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			this.start = start;
			this.count = count;
		}

		@Override
		public void afterTextChanged(Editable s) {
			cancelUpdate();
			if (isEditedNotBySearch) {
				convertTabs(s, start, count);
				searchResult = null;
			}

			updateHandler.postDelayed(colorUpdater, updateDelay);

			Activity activity = getActivity();
			// TODO: send correct invalidate
			if (activity != null) {
				activity.invalidateOptionsMenu();
			}
		}
	};

	@Nullable
	private Activity getActivity() {
		Context context = getContext();
		while (context instanceof ContextWrapper) {
			if (context instanceof Activity) {
				return (Activity)context;
			}
			context = ((ContextWrapper)context).getBaseContext();
		}
		return null;
	}

	private boolean isNeedLineNumbers;
	//endregion

	//region CONSTRUCTOR
	public ColoredEditText(final Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.e(TAG, "CONSTRUCTOR");
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Log.e(TAG, "ATTACHED TO WINDOW ");
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
		verticalScroll = getRootView().findViewById(R.id.scroll_vertical);

		/*verticalScroll.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (ColoredEditText.this.hasFocus()) {
					ColoredEditText.this.clearFocus();
				}
				return false;
			}
		});*/

		//setLayerType(View.LAYER_TYPE_NONE, null);

		final Context context = getContext();

		colorScheme = PreferenceHelper.isDarkTheme(context)
				? new Base16TomorrowNightSyntaxColor()
				: new Base16TomorrowDaySyntaxColor();

		setTextColor(colorScheme.getTextColor());
		setBackgroundColor(colorScheme.getBackgroundColor());
		setHighlightColor(colorScheme.getSelectionColor());

		updateDelay = PreferenceHelper.getUpdateDelay(context);

		deviceHeight = getResources().getDisplayMetrics().heightPixels;

		isNeedLineNumbers = PreferenceHelper.getLineNumbers(context);
		isHighlightEnabled = PreferenceHelper.getSyntaxHighlight(context);

		gutterView = getRootView().findViewById(R.id.edit_text_line_numbers);
		gutterView.setVisibility(isNeedLineNumbers ? View.VISIBLE : View.GONE);
		if (isNeedLineNumbers) {
			gutterView.setTextColor(colorScheme.getGutterTextColor());
			gutterView.setBackgroundColor(colorScheme.getGutterColor());
		}
		isWhitespaces = PreferenceHelper.getWhitespaces(context);
		styleWhitespaces = colorScheme.getWhitespaceStyle();

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

		tabWidth = PreferenceHelper.getTabWidth(context);
		tabWidthInPixels = Math.round(getPaint().measureText("m") * tabWidth);
		spaceWidthInPixels = Math.round(getPaint().measureText(" "));
		isTabAsSpaces = PreferenceHelper.getTabToSpaces(context);
		if (isTabAsSpaces) {
			StringBuilder str = new StringBuilder();
			for (int i = 0; i < tabWidth; ++i) {
				str.append(' ');
			}
			tabulation = str.toString();
		} else {
			tabulation = "\t";
		}

		setFocusable(true);
		setOnClickListener(v -> {
            if (!PreferenceHelper.getReadOnly(context)) {
                verticalScroll.tempDisableListener(1000);
                InputMethodManager iim = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (iim != null) {
                    iim.showSoftInput(ColoredEditText.this, InputMethodManager.SHOW_IMPLICIT);
                }
            }

        });
		setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !PreferenceHelper.getReadOnly(context)) {
                verticalScroll.tempDisableListener(1000);
                InputMethodManager iim = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (iim != null) {
                    iim.showSoftInput(ColoredEditText.this, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

		final boolean autoIndent = PreferenceHelper.getAutoIndent(context);
		setFilters(new InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    if (enabledChangeListener &&
                            end - start == 1 &&
                            start < source.length() &&
                            dstart < dest.length()) {
                        char c = source.charAt(start);

                        if (c == '\n' && autoIndent)
                            return autoIndent(
                                    source,
                                    dest,
                                    dstart,
                                    dend);
                    }

                    return source;
                }});

		resetVariables();

		ViewTreeObserver vto = getViewTreeObserver();
		vto.addOnGlobalLayoutListener(() -> {
            if (!isNeedToUpdate) return;
            Layout l = getLayout();
            if (l != null) {
                updateLines(l);
                isNeedToUpdate = false;
            }
        });
	}

	private CharSequence autoIndent(
			CharSequence source,
			Spanned dest,
			int dstart,
			int dend) {
		String indent = "";
		int istart = dstart - 1;
		int iend;

		// find start of this line
		boolean dataBefore = false;
		int pt = 0;

		for (; istart > -1; --istart) {
			char c = dest.charAt(istart);

			if (c == '\n')
				break;

			if (c != ' ' && c != '\t') {
				if (!dataBefore) {
					// indent always after those characters
					if (c == '{'
							|| c == '+'
							|| c == '-'
							|| c == '*'
							|| c == '/'
							|| c == '%'
							|| c == '^'
							|| c == '=')
						--pt;

					dataBefore = true;
				}

				// parenthesis counter
				if (c == '(')
					--pt;
				else if (c == ')')
					++pt;
			}
		}

		// copy indent of this line into the next
		if (istart > -1) {
			char charAtCursor = dest.charAt(dstart);

			for (iend = ++istart; iend < dend; ++iend) {
				char c = dest.charAt(iend);

				// auto expand comments
				if (charAtCursor != '\n'
						&& c == '/'
						&& iend + 1 < dend
						&& dest.charAt(iend) == c) {
					iend += 2;
					break;
				}

				if (c != ' ' && c != '\t')
					break;
			}

			indent += dest.subSequence(istart, iend);
		}

		// add new indent
		if (pt < 0)
			indent += tabulation;

		// append white space of previous line and new indent
		return source + indent;
	}

	public void detectSyntax() {
		if (!isHighlightEnabled) {
			textSyntax = new PlainTextSyntax();
			return;
		}
		String fileExtension
				= textFile.greatUri == null
				? ""
				: textFile.greatUri.getFileExtension().toLowerCase();

		if (fileExtension.contains("htm") || fileExtension.contains("xml")) {
			textSyntax = new HTMLTextSyntax();
		} else if (fileExtension.equals("css")) {
			textSyntax = new CSSTextSyntax();
		} else if (fileExtension.equals("lua")) {
			textSyntax = new LUATextSyntax();
		} else if (fileExtension.equals("py")) {
			textSyntax = new PythonTextSyntax();
		} else if (fileExtension.equals("php")) {
			textSyntax = new PHPTextSyntax();
		} else if (fileExtension.equals("sql")) {
			textSyntax = new SQLTextSyntax();
		} else if (fileExtension.equals("cpp")
				|| fileExtension.equals("c")
				|| fileExtension.equals("hpp")
				|| fileExtension.equals("h")) {
			textSyntax = new CppTextSyntax();
		} else if (fileExtension.equals("m")) {
			textSyntax = new ObjectiveCTextSyntax();
		} else if (fileExtension.equals("java")) {
			textSyntax = new JavaTextSyntax();
		} else if (fileExtension.equals("cs")) {
			textSyntax = new CsharpTextSyntax();
		} else if (fileExtension.equals("glsl")) {
			textSyntax = new GLSLTextSyntax();
		} else if (fileExtension.equals("prop") || fileExtension.contains("conf") ||
				(Arrays.asList(MimeTypes.MIME_MARKDOWN).contains(fileExtension))) {
			textSyntax = new PlainTextSyntax();
		} else {
			textSyntax = new PlainTextSyntax();
		}
	}

	/**
	 * Attach ColoredEditText to text file.
	 *
	 * @param textFile
	 * 		ColoredEditText for editing file.
	 */
	public void setTextFile(@NonNull TextFile textFile) {
		this.textFile = textFile;
		cancelUpdate();
		detectSyntax();
		replaceTextKeepCursor(textFile.getCurrentPageText());
	}

	public void smoothScrollTo(final int x, final int y) {
		verticalScroll.postDelayed(() -> verticalScroll.smoothScrollTo(x, y), 200);
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
		int lines = getLineCount();
		if (lines == 0
				|| lineCount != lines
				|| startingLine != textFile.getStartingLine()) {
			startingLine = textFile.getStartingLine();
			lineCount = lines;
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

		boolean wrapContent = PreferenceHelper.getWrapContent(getContext());

		SpannableStringBuilder text = new SpannableStringBuilder();

		int[] selection = LineUtils.getCurrentCursorLines(layout, selectionStart, selectionEnd);

		int start = 0;
		for (int i = 0; i < lineCount; ++i) {
			// if last line we count it anyway
			int realLine = realLines[i];

			String lineNum;
			if (!wrapContent || isGoodLineArray[i]) {
				lineNum = realLine + " \n";
			} else {
				lineNum = "\n";
			}
			text.append(lineNum);
			int end = start + lineNum.length();
			if (realLine >= selection[0] && realLine <= selection[1]) {
				text.setSpan(new ForegroundColorSpan(colorScheme.getGutterTextColorSelected()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.setSpan(new LineSpan(colorScheme.getGutterColorSelected()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            if (keyCode == KeyEvent.KEYCODE_TAB) {
                String textToInsert = "  ";
                int start, end;
                start = Math.max(getSelectionStart(), 0);
                end = Math.max(getSelectionEnd(), 0);
                getText().replace(Math.min(start, end), Math.max(start, end),
                        textToInsert, 0, textToInsert.length());
                return true;
            }
            return super.onKeyDown(keyCode, event);
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
            return keyCode == KeyEvent.KEYCODE_TAB;
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
		}
		if (id == ID_REDO) {
			//redo();
			return true;
		}
		return super.onTextContextMenuItem(id);
	}

	//endregion

	public void resetVariables() {
		/*firstVisibleIndex = 0;
		firstColoredIndex = 0;*/
		lineCount = 0;
		startingLine = 0;
	}

	public void doReplace(@NonNull String replacementText) {
		if (searchResult == null)
			return;
		SearchResult.SearchItem item = searchResult.getCurrentItem();
		if (item == null) return;
		searchResult.replace(replacementText);
		isEditedNotBySearch = false;
		getEditableText().replace(item.start, item.end, replacementText);
		isEditedNotBySearch = true;
		SearchResult.SearchItem itemNext = searchResult.getCurrentItem();
		if (itemNext != null) {
			setSelection(itemNext.start, itemNext.end);
		} else {
			setSelection(item.start, item.start + replacementText.length());
		}
		requestFocus();
		updateHandler.postDelayed(colorUpdater, SYNTAX_DELAY_MILLIS_SHORT);
		if (searchResult.getAmount() == 0)
			searchResult = null;
	}

	public void doReplaceAll(@NonNull String replacementText) {
		if (searchResult == null) return;
		SearchResult.SearchItem item;
		isEditedNotBySearch = false;
		while ((item = searchResult.getCurrentItem()) != null) {
			searchResult.replace(replacementText);
			getEditableText().replace(item.start, item.end, replacementText);
		}
		isEditedNotBySearch = true;
		updateHandler.postDelayed(colorUpdater, SYNTAX_DELAY_MILLIS_SHORT);
		searchResult = null;
	}

	public LineUtils getLineUtils() {
		return lineUtils;
	}

	private void colorize(@NonNull Editable e, @NonNull CharSequence textToHighlight, int start) {
		if (textSyntax == null) return;

		for (Map.Entry<String, Pattern> pair : textSyntax.getPatterns()) {
			setSpan(pair.getValue(), textToHighlight, start, e, pair.getKey());
		}
		if (searchResult != null) {
			for (SearchResult.SearchItem item : searchResult.getItems()) {
				e.setSpan(new SearchResultSpan(colorScheme.getTextColor(), colorScheme.getSearchResultColor()), item.start, item.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	private void setSpan(
			@NonNull Pattern p,
			@NonNull CharSequence textToHighlight,
			int start,
			@NonNull Editable e,
			@NonNull String patternName) {
		SyntaxColor.Style style = colorScheme.getStyle(patternName);
		for (Matcher m = p.matcher(textToHighlight); m.find(); ) {
			int from = start + m.start();
			int to = start + m.end();
			if (style.color != null) {
				e.setSpan(new ForegroundColorSpan(style.color), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (style.fontStyle != Typeface.NORMAL) {
				e.setSpan(new StyleSpan(style.fontStyle), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (style.isUnderline) {
				e.setSpan(new UnderlineSpan(), from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

	private void cancelUpdate() {
		updateHandler.removeCallbacks(colorUpdater);
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

		taskToUpdate = new ColorUpdaterAsyncTask(textToUpdate);
		taskToUpdate.execute();
	}

	private void convertTabs(@NonNull Editable e, int start, int count) {
		if (tabWidthInPixels < 2 && !isWhitespaces)
			return;

		String s = e.toString();

		for (int stop = start + count;
			 (start = s.indexOf("\t", start)) > -1 && start < stop;
			 ++start)
			e.setSpan(
					new TabWidthSpan(),
					start,
					start + 1,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	private void convertWhitespaces(@NonNull Editable e, int start, int count) {
		String s = e.toString();
		int stop = start + count;
		for (; (start = s.indexOf(" ", start)) > -1 && start < stop; ++start) {
			e.setSpan(
					new SpaceSpan(),
					start,
					start + 1,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}


	private static class LineSpan implements LineBackgroundSpan {
		private final int color;

		LineSpan(int color) {
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

	private class TabWidthSpan extends ReplacementSpan {
		@Override
		public int getSize(
				@NonNull Paint paint,
				CharSequence text,
				int start,
				int end,
				Paint.FontMetricsInt fm) {
			return tabWidthInPixels;
		}

		@Override
		public void draw(
				@NonNull Canvas canvas,
				CharSequence text,
				int start,
				int end,
				float x,
				int top,
				int y,
				int bottom,
				@NonNull Paint paint) {
			paint.setColor(styleWhitespaces.color);
			if (isWhitespaces)
				canvas.drawText("»", x, y, paint);
		}
	}

	private class NewLineSpan extends ReplacementSpan {
		@Override
		public int getSize(
				@NonNull Paint paint,
				CharSequence text,
				int start,
				int end,
				Paint.FontMetricsInt fm) {
			return 0;
		}

		@Override
		public void draw(
				@NonNull Canvas canvas,
				CharSequence text,
				int start,
				int end,
				float x,
				int top,
				int y,
				int bottom,
				@NonNull Paint paint) {
			paint.setColor(styleWhitespaces.color);
			canvas.drawText("¬", x, y, paint);
		}
	}

	private class SpaceSpan extends ReplacementSpan {
		@Override
		public int getSize(
				@NonNull Paint paint,
				CharSequence text,
				int start,
				int end,
				Paint.FontMetricsInt fm) {
			return spaceWidthInPixels;
		}

		@Override
		public void draw(
				@NonNull Canvas canvas,
				CharSequence text,
				int start,
				int end,
				float x,
				int top,
				int y,
				int bottom,
				@NonNull Paint paint) {
			paint.setColor(styleWhitespaces.color);
			canvas.drawText("·", x, y, paint);
		}
	}

	private class SearchResultSpan extends ReplacementSpan {
		private int CORNER_RADIUS = 8;
		private int backgroundColor = 0;
		private int textColor = 0;

		SearchResultSpan(int textColor, int backgroundColor) {
			super();
			this.backgroundColor = backgroundColor;
			this.textColor = textColor;
		}

		@Override
		public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
			RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);
			paint.setColor(backgroundColor);
			Paint.Style oldStyle = paint.getStyle();
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(2.0f);
			canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
			paint.setStyle(oldStyle);
			paint.setColor(textColor);
			canvas.drawText(text, start, end, x, y, paint);
		}

		@Override
		public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
			return Math.round(paint.measureText(text, start, end));
		}

		private float measureText(Paint paint, CharSequence text, int start, int end) {
			return paint.measureText(text, start, end);
		}
	}

	private class ColorUpdaterAsyncTask extends AsyncTask<String, Void, Void> {
		private String textToUpdate = null;
		private Editable editable = null;

		private int firstVisibleIndex;
		private int lastVisibleIndex;
		private int firstColoredIndex;

		ColorUpdaterAsyncTask(@Nullable String s) {
			super();
			textToUpdate = s;
		}

		private Editable highlight(@NonNull Editable editable) {
			if (editable.length() == 0) {
				return editable;
			}

			CharSequence textToHighlight = editable.subSequence(firstColoredIndex, lastVisibleIndex);

			colorize(editable, textToHighlight, firstColoredIndex);

			return editable;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			disableTextChangedListener();

			editable = getText();
		}

		private void doMagic(Editable editable2) {
			int editorHeight = getHeight();

			if (textToUpdate == null && editorHeight > 0) {
				firstVisibleIndex = getFirstVisibleOffset();
				lastVisibleIndex = getLastVisibleOffset();
			} else {
				firstVisibleIndex = 0;
				lastVisibleIndex = CHARS_TO_COLOR;
			}

			firstColoredIndex = firstVisibleIndex - (CHARS_TO_COLOR / 5);

			// normalize
			if (firstColoredIndex < 0)
				firstColoredIndex = 0;
			if (lastVisibleIndex > editable2.length())
				lastVisibleIndex = editable2.length();
			if (firstColoredIndex > lastVisibleIndex)
				firstColoredIndex = lastVisibleIndex;

			if (isHighlightEnabled) {
				editable = highlight(editable2);
			} else {
				editable = editable2;
			}
			convertTabs(editable2, firstColoredIndex, lastVisibleIndex - firstColoredIndex);
			if (isWhitespaces)
				convertWhitespaces(editable2, firstColoredIndex, lastVisibleIndex - firstColoredIndex);
		}

		@Override
		protected Void doInBackground(String... params) {
			Editable editable2 = textToUpdate == null
					? new SpannableStringBuilder(editable.toString())
					: new SpannableStringBuilder(textToUpdate);

			doMagic(editable2);

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			int selectionStart = getSelectionStart();
			int selectionEnd = getSelectionEnd();

			setText(editable, TextView.BufferType.SPANNABLE);

			int newCursorPos;

			// Detect if cursor or whole selection visible
			boolean isSelectionStartVisible = selectionStart >= firstVisibleIndex && selectionStart <= lastVisibleIndex;
			boolean isSelectionEndVisible = selectionEnd >= firstVisibleIndex && selectionEnd <= lastVisibleIndex;

			// TODO: подумать над курсором и выделением, сделать так, чтобы при установке выделения не сбрасывался скролл
			if (isSelectionStartVisible) {
				// if the cursor is on screen
				// we don't change its position
				newCursorPos = selectionStart;
			} else {
				// else we set it to the first visible pos
				newCursorPos = selectionEnd;//firstVisibleIndex;
			}

			if (newCursorPos > -1 && newCursorPos <= length()) {
				if (selectionEnd != selectionStart && isSelectionStartVisible)
					setSelection(selectionStart, selectionEnd);
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

	private void clearSpans(@NonNull Editable e) {
		// remove foreground color spans
		{
			ForegroundColorSpan[] spans = e.getSpans(
					0,
					e.length(),
					ForegroundColorSpan.class);

			for (int n = spans.length; --n > 0; )
				e.removeSpan(spans[n]);
		}

		// remove background color spans
		{
			BackgroundColorSpan[] spans = e.getSpans(
					0,
					e.length(),
					BackgroundColorSpan.class);

			for (int n = spans.length; --n > 0; )
				e.removeSpan(spans[n]);
		}
	}
}
