package ayshi.ivpsuite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
        exportButton = (Button) rootView.findViewById(R.id.button_export);

        callCameraButton.setOnClickListener(this);
        grayscaleButton.setOnClickListener(this);
        restoreBitmapButton.setOnClickListener(this);
        exportButton.setOnClickListener(this);

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
                imageSourcePath = imageSourceFile.getAbsolutePath();
                Log.e("NewSource", imageSourcePath);
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
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

                galleryAddPic();
            } else {
                Toast.makeText(getActivity().getBaseContext(), "Please capture again", Toast.LENGTH_LONG).show();
            }


        }
    }

    public Bitmap ARGBtoGrayScale(){
        Log.e("ImageHandler", "creating pseudo grayscale");
        //TODO: investigate null crash - may need to change fragments to recieve value properly
        for (int i = 0; i<byteArray.length; i++){
            //http://www.developer.com/ws/android/programming/Working-with-Images-in-Googles-Android-3748281-2.htm
            //http://www.mkyong.com/java/java-and-0xff-example/ - & 0xff grabs last 8 bits from the 32 bit signed int (2^8 values)
            //pointer* >> 16 shifts the value to the right by 16 bits, i.e.
            //11000000 10101000 00000001 00000010 becomes
            //00000000 00000000 11000000 10101000
            int red = ((byteArray[i] >> 16) & 0xff);
            int green = ((byteArray[i] >> 8) & 0xff);
            int blue = (byteArray[i] & 0xff);
            int newValue = (red+green+blue)/3;
            red = green = blue = newValue;
            byteArray[i] = 0xff000000 | (red << 16) | (green << 8) | blue;
        }
        return Bitmap.createBitmap(byteArray, imageWidth, imageHeight, config);
    }
}

