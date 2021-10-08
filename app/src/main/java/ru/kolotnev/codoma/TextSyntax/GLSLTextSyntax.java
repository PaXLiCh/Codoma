package ru.kolotnev.codoma.TextSyntax;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * GLSL syntax highlight.
 */
public class GLSLTextSyntax extends TextSyntax {
	private static final Pattern keywords = Pattern.compile(
			"\\b(attribute|const|uniform|varying|break|continue|" +
					"do|for|while|if|else|in|out|inout|float|int|void|bool|true|false|" +
					"lowp|mediump|highp|precision|invariant|discard|return|mat2|mat3|" +
					"mat4|vec2|vec3|vec4|ivec2|ivec3|ivec4|bvec2|bvec3|bvec4|sampler2D|" +
					"samplerCube|struct|gl_Vertex|gl_FragCoord|gl_FragColor)\\b");
	private static final Pattern builtIns = Pattern.compile(
			"\\b(radians|degrees|sin|cos|tan|asin|acos|atan|pow|" +
					"exp|log|exp2|log2|sqrt|inversesqrt|abs|sign|floor|ceil|fract|mod|" +
					"min|max|clamp|mix|step|smoothstep|length|distance|dot|cross|" +
					"normalize|faceforward|reflect|refract|matrixCompMult|lessThan|" +
					"lessThanEqual|greaterThan|greaterThanEqual|equal|notEqual|any|all|" +
					"not|dFdx|dFdy|fwidth|texture2D|texture2DProj|texture2DLod|" +
					"texture2DProjLod|textureCube|textureCubeLod)\\b");
	private static final Pattern comments = Pattern.compile(
			"/\\*(?:.|[\\n\\r])*?\\*/|//.*");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", keywords));
		patterns.add(new AbstractMap.SimpleEntry<>("support.function.builtin", builtIns));
		patterns.add(new AbstractMap.SimpleEntry<>("comment", comments));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
