package picker.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

class FolderPicker {

    private Context context;
    private int DP_1;

    private LinearLayout llFolderOuter;
    private LinearLayout llFolderContentArea;
    private LinearLayout.LayoutParams folderTextViewLayoutParams;

    FolderPicker(Context _context) {
        context = _context;
        DP_1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());

        llFolderOuter = ((Activity)context).findViewById(R.id.llFolderOuter);
        llFolderContentArea = ((Activity)context).findViewById(R.id.llFolderContentArea);
        llFolderOuter.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                close();
                return true;
            }
        });

        folderTextViewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        folderTextViewLayoutParams.setMargins(0, 5*DP_1, 0, 5*DP_1);
    }

    void close() {
        llFolderOuter.setVisibility(View.GONE);
    }

    void open() {
        llFolderContentArea.removeAllViews();
        llFolderOuter.setVisibility(View.VISIBLE);

        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        HashMap<String, Integer> folderMap = new HashMap<>(); // folderName, fileCountInAFolder
        int allFileCount = 0;
        String fileFolder;

        while (null!=cursor && cursor.moveToNext()) {
            fileFolder = cursor.getString(1);// column == projection[1] MediaStore.Images.Media.BUCKET_DISPLAY_NAME

            if (null == folderMap.get(fileFolder)) {
                folderMap.put(fileFolder, 1); // init fileName, fileCount
            } else {
                folderMap.put(fileFolder, 1 + folderMap.get(fileFolder)); // fileCountUp
            }

            allFileCount++;
        }

        if (null!=cursor) {
            cursor.close();
        }

        for (final String aFolder : folderMap.keySet()) {
            llFolderContentArea.addView(getTextView(aFolder, aFolder, folderMap.get(aFolder)));
        }

        llFolderContentArea.addView(getTextView(null, context.getString(R.string.All), allFileCount), 0);
    }

    boolean isOpen() {
        return View.GONE != llFolderOuter.getVisibility();
    }

    private TextView getTextView(final String _folderValue, String _folderName, int _fileCountInAFolder) {
        TextView textView;

        textView = new TextView(context);
        textView.setText(_folderName + " (" + _fileCountInAFolder + ") ");
        textView.setLayoutParams(folderTextViewLayoutParams);
        textView.setPadding(10*DP_1, 10*DP_1, 10*DP_1, 10*DP_1);
        textView.setTextColor(Color.BLACK);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ImagePickerActivity)context).selectFolder(_folderValue);
            }
        });

        return textView;
    }
}
