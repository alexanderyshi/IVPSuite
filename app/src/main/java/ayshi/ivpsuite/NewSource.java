package ayshi.ivpsuite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 *
 * Camera handling taken from //http://www.linux.com/learn/tutorials/722038-android-calling-the-camera
 */
public class NewSource extends ImageHandler{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewSource() {
    }

    public void onCreate(Bundle savedInstanceState) {
        ARG_ITEM_ID = "new_source_detail_fragment";
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View rootView = inflater.inflate(R.layout.new_source_item_detail, container, false);
        ((TextView) rootView.findViewById(R.id.item_detail_text)).setText(ARG_ITEM_ID);

        imagePreview = (ImageView) rootView.findViewById(R.id.image_preview);
        if (previewBitmap!=null){
            imagePreview.setImageBitmap(previewBitmap);
        }

        final Button callCameraButton = (Button) rootView.findViewById(R.id.button_call_camera);
        final Button grayscaleButton = (Button) rootView.findViewById(R.id.button_grayscale);
        final Button restoreBitmapButton = (Button) rootView.findViewById(R.id.button_argb_8888);
        saveButton = (Button) rootView.findViewById(R.id.button_save);

        callCameraButton.setOnClickListener(this);
        grayscaleButton.setOnClickListener(this);
        restoreBitmapButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        return rootView;
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.button_call_camera){
            takePhoto();
        }
        else if (view.getId() == R.id.button_argb_8888){
            loadBitmap();
            imagePreview.setImageBitmap(previewBitmap);
            Log.e(ARG_ITEM_ID, "argb8888");
        }
        else if (view.getId() == R.id.button_grayscale){
            previewBitmap = ARGBtoGrayScale();
            imagePreview.setImageBitmap(previewBitmap);
            Log.e(ARG_ITEM_ID, "grayscale");
        }
    }

    private void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            imageSourceFile = null;
            try {
                imageSourceFile = createImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            Log.e("NewSource", imageSourcePath);
            if (imageSourceFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageSourceFile));
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imageSourcePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IVP_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //TODO: saving to custom directory (ayshi.ivpsuite)
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        imageSourcePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            Log.e("NewSource", "data null!");
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (imageSourcePath != null) {
                //TODO: find out why imagePreview isn't getting set in onCreateView()
                imagePreview = (ImageView) getActivity().findViewById(R.id.image_preview);
                ((ItemListActivity) getActivity()).setImageSourcePath(imageSourcePath);
                loadBitmap();
                imagePreview.setImageBitmap(previewBitmap);

                //set local variables via ImageHandler mutators
                setImageWidth(previewBitmap.getWidth());
                setImageHeight(previewBitmap.getHeight());
                setBitmapConfig(previewBitmap.getConfig());
                int[] tempByteArray = new int[previewBitmap.getWidth() * previewBitmap.getHeight()];
                previewBitmap.getPixels(tempByteArray, 0, previewBitmap.getWidth(), 0, 0,
                        previewBitmap.getWidth(), previewBitmap.getHeight());
                setByteArray(tempByteArray);
                //send values to fragment handler
                saveBitmap();
                galleryAddPic();
            } else {
                Toast.makeText(getActivity().getBaseContext(), "Please capture again", Toast.LENGTH_LONG).show();
            }


        }
    }

}

