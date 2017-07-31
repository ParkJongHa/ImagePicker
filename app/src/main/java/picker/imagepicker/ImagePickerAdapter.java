package picker.imagepicker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

class ImagePickerAdapter extends RecyclerView.Adapter<ImagePickerAdapter.Holder> {

    private final static BitmapFactory.Options bitmapOptions =  new BitmapFactory.Options();
    private ArrayList<APickerItem> pickerItemList;

    private static LinearLayout.LayoutParams rootViewParams;
    private static LinearLayout.LayoutParams getRootViewParams(int _length) {
        if (null == rootViewParams) {
            rootViewParams = new LinearLayout.LayoutParams(_length, _length);
        }
        return rootViewParams;
    }

    private static FrameLayout.LayoutParams pickerParams;
    private static FrameLayout.LayoutParams getPickerParams(int _length) {
        if (null == pickerParams) {
            pickerParams = new FrameLayout.LayoutParams(_length, _length);
        }
        return pickerParams;
    }

    private static FrameLayout.LayoutParams pickerImageParams;
    private static FrameLayout.LayoutParams getPickerImageParams(int _length) {
        if (null == pickerImageParams) {
            pickerImageParams = new FrameLayout.LayoutParams(_length, _length);
        }
        return pickerImageParams;
    }

    ImagePickerAdapter(ArrayList<APickerItem> _pickerList, int _length){
        this.pickerItemList = _pickerList;
        getRootViewParams(_length);

        int innerLength = _length - 2;

        getPickerParams(innerLength);
        getPickerImageParams(innerLength);

        bitmapOptions.outHeight = innerLength;
        bitmapOptions.outWidth = innerLength;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup _viewGroup, int _viewType) {
        return new ImagePickerAdapter
            .Holder(LayoutInflater.from(_viewGroup.getContext())
            .inflate(R.layout.a_picker_item, _viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ImagePickerAdapter.Holder _holder, int i) {
        if (RecyclerView.NO_POSITION == _holder.getAdapterPosition()
         || pickerItemList.size() <= i || i < 0) {
            return;
        }

        _holder.flRootView.setLayoutParams(rootViewParams);
        _holder.flPicker.setLayoutParams(pickerParams);
        _holder.ivPickerImage.setLayoutParams(pickerImageParams);
        _holder.flPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EVENT)_holder.flPicker.getContext()).itemClick(pickerItemList.get(_holder.getAdapterPosition()), _holder.flRootView);
            }
        });

        if (pickerItemList.get(_holder.getAdapterPosition()).isSelected()) {
            _holder.flCheck.setVisibility(View.VISIBLE);
        } else {
            _holder.flCheck.setVisibility(View.INVISIBLE);
        }

        new Thread() {
            @Override public void run() {
                try {
                    _holder.ivPickerImage.setTag(BitmapFactory.decodeFile(pickerItemList.get(_holder.getAdapterPosition()).getThumbnailPath(), bitmapOptions));
                    Message msg = new Message();
                    msg.obj = _holder.ivPickerImage;
                    imageBitmapHandler.sendMessage(msg);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static ImageBitmapHandler imageBitmapHandler = new ImageBitmapHandler();
    private static class ImageBitmapHandler extends Handler {
        @Override public void handleMessage(Message msg) {
            if (null == msg || null == msg.obj || !(msg.obj instanceof ImageView)) {
                return;
            }

            ImageView imageView = (ImageView) msg.obj;

            if (null==imageView.getTag() || !(imageView.getTag() instanceof Bitmap)) {
                imageView.setImageResource(R.drawable.broken);
            } else {
                imageView.setImageBitmap((Bitmap) imageView.getTag());
            }
        }
    }

    @Override
    public int getItemCount() {
        return pickerItemList.size();
    }

    static class Holder extends RecyclerView.ViewHolder{
        FrameLayout flRootView;
        FrameLayout flPicker;

        ImageView ivPickerImage;
        FrameLayout flCheck;

        Holder(View _baseView) {
            super(_baseView);

            flRootView = (FrameLayout) _baseView;
            flPicker = flRootView.findViewById(R.id.flPicker);
            ivPickerImage = flRootView.findViewById(R.id.ivPickerImage);
            flCheck = flRootView.findViewById(R.id.flCheck);
        }
    }

    interface EVENT {
        void itemClick(APickerItem _aPickerItem, FrameLayout _rootView);
    }

}
