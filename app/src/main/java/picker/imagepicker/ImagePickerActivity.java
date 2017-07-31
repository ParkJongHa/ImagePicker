package picker.imagepicker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ImagePickerActivity extends AppCompatActivity implements ImagePickerAdapter.EVENT {

    public final static int REQUEST_CODE = 10000;
    public final static int PERMISSION_STORAGE_REQUEST_CODE = 20000;
    public final static int MAX_COUNT_SELECT_ITEM = 20;
    public final static float MIN_ITEM_LENGTH_FOR_COLUMN_COUNT = 100f;
    public final static String RESULT_PARAM_CANONICAL_FILE_NAMES = "RESULT_PARAM_CANONICAL_FILE_NAMES";

    private ImagePickerAdapter imagePickerAdapter;
    private ArrayList<APickerItem> pickerItemList;
    private ArrayList<String> selectedItemList;
    private RecyclerView rvContentArea;
    private FolderPicker folderPicker;

    private TextView tFolder;
    private TextView tSelectedDesc;

    private PickerItemFetchThread pickerItemFetchThread;
    private PickerItemFetchHandler pickerItemFetchHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null != getSupportActionBar()) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.image_picker_activity);

        rvContentArea = (RecyclerView) findViewById(R.id.rvContentArea);
        tFolder = (TextView) findViewById(R.id.tFolder);
        tSelectedDesc = (TextView) findViewById(R.id.tSelectedDesc);

        folderPicker = new FolderPicker(this);
        selectedItemList = new ArrayList<>();
        tFolder.setText(R.string.All);

        rvContentArea = (RecyclerView) findViewById(R.id.rvContentArea);
        rvContentArea.setHasFixedSize(true);
        rvContentArea.setItemViewCacheSize(40);
        rvContentArea.setDrawingCacheEnabled(true);
        rvContentArea.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        rvContentArea.setItemAnimator(new DefaultItemAnimator());

        if (permissionCheck_storage()) {
            rvContentArea.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() {
                    rvContentArea.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    selectFolder(null);
                }
            });
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, R.string.need_permission, Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PERMISSION_STORAGE_REQUEST_CODE == requestCode) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0] && PackageManager.PERMISSION_GRANTED == grantResults[1]) {
                selectFolder(null);
            } else {
                Toast.makeText(this, R.string.need_permission, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private int getColumnCount() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float dpWidth = outMetrics.widthPixels / getResources().getDisplayMetrics().density;

        return (int) (dpWidth / MIN_ITEM_LENGTH_FOR_COLUMN_COUNT);
    }

    public void onClickClose(View _view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onClickSelect(View _view) {
        if (0 < selectedItemList.size() && selectedItemList.size() <= MAX_COUNT_SELECT_ITEM) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent.putExtra(RESULT_PARAM_CANONICAL_FILE_NAMES, selectedItemList.toArray()));
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    public void removeItemInSelectedList(String _filePath) {
        if (null ==  _filePath) return;

        for (int i=0; i<selectedItemList.size(); i++) {
            if (_filePath.equals(selectedItemList.get(i))) {
                selectedItemList.remove(i);
                i = -1;
            }
        }
    }

    @Override
    public void itemClick(APickerItem _aPickerItem, FrameLayout _itemRootView) {
        // un select
        if (_aPickerItem.isSelected()) {
            _aPickerItem.setSelected(false);
            _itemRootView.findViewById(R.id.flCheck).setVisibility(View.INVISIBLE);
            removeItemInSelectedList(_aPickerItem.filePath);
            tSelectedDesc.setText(String.valueOf(selectedItemList.size()));
            return;
        }

        // max select
        if (MAX_COUNT_SELECT_ITEM <= selectedItemList.size()) {
            Toast.makeText(this, R.string.Max, Toast.LENGTH_SHORT).show();
            return;
        }

        // select
        _aPickerItem.setSelected(true);
        _itemRootView.findViewById(R.id.flCheck).setVisibility(View.VISIBLE);
        selectedItemList.add(_aPickerItem.filePath);
        tSelectedDesc.setText(String.valueOf(selectedItemList.size()));
    }

    public void onClickFolder(View _view) {
        if (folderPicker.isOpen()) {
            folderPicker.close();
        } else {
            folderPicker.open();
        }
    }

    public void selectFolder(String _folder) {
        folderPicker.close();
        selectedItemList.clear();
        tSelectedDesc.setText(String.valueOf(selectedItemList.size()));

        if (null != pickerItemFetchThread) {
            pickerItemFetchThread.interrupt();
            pickerItemFetchThread = null;
        }

        if (null != pickerItemFetchHandler) {
            pickerItemFetchHandler = null;
        }

        if (null == _folder) {
            tFolder.setText(R.string.All);
        } else {
            tFolder.setText(_folder);
        }

        if (null == pickerItemList) {
            pickerItemList = new ArrayList<>();
        } else {
            pickerItemList.clear();
        }

        if (null == imagePickerAdapter) {
            imagePickerAdapter = new ImagePickerAdapter(pickerItemList, (rvContentArea.getWidth() / getColumnCount()));
        } else {
            imagePickerAdapter.notifyDataSetChanged();
        }

        if (null == rvContentArea.getLayoutManager()) {
            rvContentArea.setLayoutManager(new SafeGridLayoutManager(this, getColumnCount()));
        }
        if (null == rvContentArea.getAdapter()) {
            rvContentArea.setAdapter(imagePickerAdapter);
        }

        pickerItemFetchHandler = new PickerItemFetchHandler();
        pickerItemFetchThread = new PickerItemFetchThread(ImagePickerActivity.this, pickerItemList, _folder, pickerItemFetchHandler);
        pickerItemFetchThread.start();

    }

    public boolean permissionCheck_storage() {
        return !(PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }

    private class PickerItemFetchHandler extends Handler {
        @Override public void handleMessage(Message msg) {
            if (0 <= msg.what && null!=imagePickerAdapter) {
                imagePickerAdapter.notifyItemInserted(msg.what);
            }
        }
    }

    private class SafeGridLayoutManager extends GridLayoutManager {
        private SafeGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

}
