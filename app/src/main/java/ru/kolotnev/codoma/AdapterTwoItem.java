package ru.kolotnev.codoma;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class AdapterTwoItem extends ArrayAdapter<Pair<String, String>> {
	private final LayoutInflater inflater;
	private final List<Pair<String, String>> lines;

	AdapterTwoItem(Context context, List<Pair<String, String>> lines) {
		super(context, R.layout.item_two_lines, lines);
		this.lines = lines;
		this.inflater = LayoutInflater.from(context);
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
		if (convertView == null) {
			convertView = this.inflater.inflate(R.layout.item_two_lines, parent, false);
			new ViewHolder(convertView).update(position);
		} else {
			((ViewHolder) convertView.getTag()).update(position);
		}
		return convertView;
	}

	private class ViewHolder {
		private final TextView line1;
		private final TextView line2;

		ViewHolder(View v) {
			line1 = v.findViewById(android.R.id.text1);
			line2 = v.findViewById(android.R.id.text2);
			v.setTag(this);
		}

		private void update(int position) {
			Pair<String, String> line = lines.get(position);
			line1.setText(line.first);
			line2.setText(line.second);
		}
	}
}
