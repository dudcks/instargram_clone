package com.example.mainapp.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class displaymet {
    public static int calculateColumnWidth(Context context, int numOfColumns) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidthPx = displayMetrics.widthPixels;
        int columnWidthPx = screenWidthPx / numOfColumns;
        return columnWidthPx;
    }
}