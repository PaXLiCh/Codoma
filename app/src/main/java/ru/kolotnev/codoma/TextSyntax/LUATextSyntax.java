package ru.kolotnev.codoma.TextSyntax;

import android.support.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * LUA syntax highlight.
 */
public class LUATextSyntax extends TextSyntax {

	public static final Pattern LUA_KEYWORDS = Pattern.compile(
			"@[A-Za-z0-9_\\.]*|\\b(local|global|boolean|number|userdata)\\b|\\b(true|false|nil)" +
					"\\b|\\b(return|then|while|and|break|do|else|elseif|end|for|function|if|in" +
					"|not|or|repeat|until|thread|table)\\b|(?i)\\b(editsetText|editText|inkey" +
					"|touch|system.exit|system.expCall|system.getAppPath|system.getCardMnt" +
					"|system.getSec|system.impCallActionSend|system.impCallActionView" +
					"|system.setrun|system.setScreen|system.version|El_Psy_Congroo" +
					"|canvas.drawCircle|canvas.drawCls|canvas.drawLine|canvas.drawRect" +
					"|canvas.getBmpSize|canvas.getColor|canvas.getg|canvas.getviewSize" +
					"|canvas.loadBmp|canvas.putCircle|canvas.putCls|canvas.putflush|canvas.putg" +
					"|canvas.putLine|canvas.putRect|canvas.putrotg|canvas.putWork|canvas.saveBmp" +
					"|canvas.setMainBmp|canvas.setWorkBmp|canvas.workCls|canvas.workflush|color" +
					"|canvas.drawText|canvas.drawTextBox|canvas.drawTextCenter" +
					"|canvas.drawTextRotate|canvas.putText|canvas.putTextBox" +
					"|canvas.putTextRotate|http.addHeader|http.addParam|http.clrHeader" +
					"|http.clrParam|http.get|http.post|http.setContentType|http.setPostFile" +
					"|http.status|dialog|item.add|item.check|item.clear|item.list|item.radio" +
					"|toast|sensor.getAccel|sensor.setdevAccel|sensor.setdevMagnet" +
					"|sensor.setdevOrient|sensor.getGdirection|sensor.getMagnet|sensor.getOrient" +
					"|sound.beep|sound.isPlay|sound.pause|sound.restart|sound.setSoundFile" +
					"|sound.start|sound.stop|zip.addFile|zip.exec|zip.status|sock.close" +
					"|sock.connectOpen|sock.getAddress|sock.listenOpen|sock.recv|sock.send" +
					"|sprite.clear|sprite.define|sprite.init|sprite.move|sprite.put)\\b" +
					"|(?i)\\b(assert|collectgarbage|coroutine.create|coroutine.resume" +
					"|coroutine.running|coroutine.status|coroutine.wrap|coroutine.yield" +
					"|debug.debug|debug.getfenv|debug.gethook|debug.getinfo|debug.getlocal" +
					"|debug.getmetatable|debug.getregistry|debug.getupvalue|debug.setfenv" +
					"|debug.sethook|debug.setlocal|debug.setmetatable|debug.setupvalue" +
					"|debug.traceback|dofile|error|file:close|file:flush|file:lines|file:read" +
					"|file:seek|file:setvbuf|file:write|getfenv|getmetatable|io.close|io.flush" +
					"|io.input|io.lines|io.open|io.output|io.popen|io.read|io.tmpfile|io.type" +
					"|io.write|ipairs|load|loadfile|loadstring|math.abs|math.acos|math.asin" +
					"|math.atan2|math.atan|math.ceil|math.cosh|math.cos|math.deg|math.exp" +
					"|math.floor|math.fmod|math.frexp|math.ldexp|math.log10|math.log|math.max" +
					"|math.min|math.modf|math.pow|math.rad|math.random|math.randomseed|math.sinh" +
					"|math.sin|math.sqrt|math.tanh|math.tan|module|next|os.clock|os.date" +
					"|os.difftime|os.execute|os.exit|os.getenv|os.remove|os.rename|os.setlocale" +
					"|os.time|os.tmpname|package.cpath|package.loaded|package.loadlib" +
					"|package.path|package.preload|package.seeal|pairs|pcall|print|rawequal" +
					"|rawget|rawset|require|select|setfenv|setmetatable|string.byte|string.char" +
					"|string.dump|string.find|string.format|string.gmatch|string.gsub|string.len" +
					"|string.lower|string.match|string.rep|string.reverse|string.sub" +
					"|string.upper|table.concat|table.insert|table.maxn|table.remove|table.sort" +
					"|tonumber|tostring|type|unpack|xpcall)\\b"
	);

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", LUA_KEYWORDS));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
