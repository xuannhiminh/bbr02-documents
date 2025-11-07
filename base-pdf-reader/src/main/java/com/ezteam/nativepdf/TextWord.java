package com.ezteam.nativepdf;

import android.graphics.RectF;

import com.ezteam.nativepdf.TextChar;
import androidx.annotation.Keep;

@Keep
public class TextWord extends RectF {
	public String w;

	public TextWord() {
		super();
		w = new String();
	}

	public void Add(TextChar tc) {
		super.union(tc);
		w = w.concat(new String(new char[]{tc.c}));
	}
}
