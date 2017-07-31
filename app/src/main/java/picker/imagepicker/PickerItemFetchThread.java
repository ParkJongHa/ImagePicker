package picker.imagepicker;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Handler;
import android.provider.MediaStore;

import java.util.ArrayList;

class PickerItemFetchThread extends Thread {

    private final Context CONTEXT;
    private final String SELECTED_FOLDER;
    private final Handler HANDLER;

    private ArrayList<APickerItem> pickerItemList;
    private boolean isInterrupt = false;

    PickerItemFetchThread(Context _context, ArrayList<APickerItem> _pickerItemList, String _selectedFolder, Handler _handler) {
        this.CONTEXT = _context;
        this.SELECTED_FOLDER = _selectedFolder;
        this.HANDLER = _handler;
        pickerItemList = _pickerItemList;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        isInterrupt = true;
    }

    @Override
    public void run() {
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media._ID  };

        Cursor cursor = CONTEXT.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (null == cursor) HANDLER.sendEmptyMessage(-1);

        int filePathColumn = 0;
        int fileFolderColumn = 1;
        int fileIdColumn = 2;

        String filePath;
        String fileFolder;
        String fileId;
        String thumbnailPath;
        int position=0;

        while (null!=cursor && cursor.moveToNext()) {
            if (isInterrupt) return;

            filePath = cursor.getString(filePathColumn);
            fileFolder = cursor.getString(fileFolderColumn);
            fileId = cursor.getString(fileIdColumn);

            if (null==SELECTED_FOLDER || SELECTED_FOLDER.equals(fileFolder)) { // all folder or specified folder
                thumbnailPath = getThumbnailPath(fileId);

                if (null != thumbnailPath) {
                    pickerItemList.add(new APickerItem(filePath, fileFolder, fileId, position));
                    pickerItemList.get(position).setThumbnailPath(thumbnailPath);
                    HANDLER.sendEmptyMessage(position);
                    position++;
                }
            }
        }

        if (null != cursor) {
            cursor.close();
        }
        HANDLER.sendEmptyMessage(-1);
    }

    private String getThumbnailPath(String imageId) {
        String[] projection = { MediaStore.Images.Thumbnails.DATA };
        String thumbnailPath;

        Cursor cursor = CONTEXT.getContentResolver().query(
            MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // thumbnail table
            projection, // data output
            MediaStore.Images.Thumbnails.IMAGE_ID + "=?", // original image id
            new String[]{imageId},
            null);

        if (null == cursor) {
            return null;
        }

        if (cursor.moveToFirst()) {
            thumbnailPath = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
            if (null != thumbnailPath) return thumbnailPath;
        }

        try {
            MediaStore.Images.Thumbnails.getThumbnail(CONTEXT.getContentResolver(), Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
        } catch (Exception e) {
            return null;
        }

        Cursor thumbnailCursor = CONTEXT.getContentResolver().query(
            MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // thumbnail table
            projection, // column (thumbnail)
            MediaStore.Images.Thumbnails.IMAGE_ID + "=?", // original image id
            new String[]{imageId},
            null);

        if (null == thumbnailCursor) {
            return null;
        }

        try {
            thumbnailPath = thumbnailCursor.getString(thumbnailCursor.getColumnIndex(projection[0]));
            thumbnailCursor.close();
            return thumbnailPath;
        } catch (CursorIndexOutOfBoundsException e) {
            return null;
        }
    }

}
