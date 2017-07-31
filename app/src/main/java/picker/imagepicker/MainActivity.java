package picker.imagepicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tMessage = (TextView) findViewById(R.id.tMessage);
    }

    public void onClickOpenImagePicker(View _view) {
        startActivityForResult(new Intent(this, ImagePickerActivity.class), ImagePickerActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RESULT_OK != resultCode) return;

        if (ImagePickerActivity.REQUEST_CODE == requestCode) {
            onActivityResult_imagePicker((Object[]) data.getSerializableExtra(ImagePickerActivity.RESULT_PARAM_CANONICAL_FILE_NAMES));
        }
    }

    private void onActivityResult_imagePicker(Object[] _selectedImageNames) {
        tMessage.setText("");

        if (null==_selectedImageNames || 1>_selectedImageNames.length) {
            tMessage.setText(R.string.empty);
            return;
        }

        for (Object aSelectedImageFileName : _selectedImageNames) {
            tMessage.setText(tMessage.getText() + "\r\n\n\n" + aSelectedImageFileName.toString());
        }
    }

}
