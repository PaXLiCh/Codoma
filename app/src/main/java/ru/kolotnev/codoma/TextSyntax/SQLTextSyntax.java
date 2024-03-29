package ru.kolotnev.codoma.TextSyntax;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SQL highlighting.
 */
public class SQLTextSyntax extends TextSyntax {
	public static final Pattern SQL_KEYWORDS = Pattern.compile(
			"(?<=\\b)((ADD)|(EXCEPT)|(PERCENT)|(ALL)|(EXEC)|(PLAN)|(ALTER)|(EXECUTE)|(PRECISION)" +
					"|(AND)|(EXISTS)|(PRIMARY)|(ANY)|(EXIT)|(PRINT)|(AS)|(FETCH)|(PROC)|(ASC)" +
					"|(FILE)|(PROCEDURE)|(AUTHORIZATION)|(FILLFACTOR)|(PUBLIC)|(BACKUP)|(FOR)" +
					"|(RAISERROR)|(BEGIN)|(FOREIGN)|(READ)|(BETWEEN)|(FREETEXT)|(READTEXT)" +
					"|(BREAK)|(FREETEXTTABLE)|(RECONFIGURE)|(BROWSE)|(FROM)|(REFERENCES)|(BULK)" +
					"|(FULL)|(REPLICATION)|(BY)|(FUNCTION)|(RESTORE)|(CASCADE)|(GOTO)|(RESTRICT)" +
					"|(CASE)|(GRANT)|(RETURN)|(CHECK)|(GROUP)|(REVOKE)|(CHECKPOINT)|(HAVING)" +
					"|(RIGHT)|(CLOSE)|(HOLDLOCK)|(ROLLBACK)|(CLUSTERED)|(IDENTITY)|(ROWCOUNT)" +
					"|(COALESCE)|(IDENTITY_INSERT)|(ROWGUIDCOL)|(COLLATE)|(IDENTITYCOL)|(RULE)" +
					"|(COLUMN)|(IF)|(SAVE)|(COMMIT)|(IN)|(SCHEMA)|(COMPUTE)|(INDEX)|(SELECT)" +
					"|(CONSTRAINT)|(INNER)|(SESSION_USER)|(CONTAINS)|(INSERT)|(SET)" +
					"|(CONTAINSTABLE)|(INTERSECT)|(SETUSER)|(CONTINUE)|(INTO)|(SHUTDOWN)" +
					"|(CONVERT)|(IS)|(SOME)|(CREATE)|(JOIN)|(STATISTICS)|(CROSS)|(KEY)" +
					"|(SYSTEM_USER)|(CURRENT)|(KILL)|(TABLE)|(CURRENT_DATE)|(LEFT)|(TEXTSIZE)" +
					"|(CURRENT_TIME)|(LIKE)|(THEN)|(CURRENT_TIMESTAMP)|(LINENO)|(TO)" +
					"|(CURRENT_USER)|(LOAD)|(TOP)|(CURSOR)|(NATIONAL)|(TRAN)|(DATABASE)|(NOCHECK)" +
					"|(TRANSACTION)|(DBCC)|(NONCLUSTERED)|(TRIGGER)|(DEALLOCATE)|(NOT)|(TRUNCATE)" +
					"|(DECLARE)|(NULL)|(TSEQUAL)|(DEFAULT)|(NULLIF)|(UNION)|(DELETE)|(OF)" +
					"|(UNIQUE)|(DENY)|(OFF)|(UPDATE)|(DESC)|(OFFSETS)|(UPDATETEXT)|(DISK)|(ON)" +
					"|(USE)|(DISTINCT)|(OPEN)|(USER)|(DISTRIBUTED)|(OPENDATASOURCE)|(VALUES)" +
					"|(DOUBLE)|(OPENQUERY)|(VARYING)|(DROP)|(OPENROWSET)|(VIEW)|(DUMMY)|(OPENXML)" +
					"|(WAITFOR)|(DUMP)|(OPTION)|(WHEN)|(ELSE)|(OR)|(WHERE)|(END)|(ORDER)|(WHILE)" +
					"|(ERRLVL)|(OUTER)|(WITH)|(ESCAPE)|(OVER)|(WRITETEXT))(?=\\b)",
			Pattern.CASE_INSENSITIVE);

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", SQL_KEYWORDS));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
