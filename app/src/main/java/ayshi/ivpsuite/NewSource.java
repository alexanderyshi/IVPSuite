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


        // get the imagePath to use for the live preview
        if (getArguments().containsKey("imageSource")) {
            imageSourcePath = getArguments().getString("imageSource");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View rootView = inflater.inflate(R.layout.new_source_item_detail, container, false);
        ((TextView) rootView.findViewById(R.id.item_detail_text)).setText(ARG_ITEM_ID);

        imagePreview = (ImageView) rootView.findViewById(R.id.image_preview);
        if (imageSourcePath != null) {
            imagePreview.setImageBitmap(BitmapFactory.decodeFile(imageSourcePath));
        }
        final Button callCameraButton = (Button) rootView.findViewById(R.id.button_call_camera);

        callCameraButton.setOnClickListener(this);

        return rootView;
    }
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.button_call_camera){
            takePhoto();
        }
    }


    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        imageSourcePath = image.getAbsolutePath();
        return image;
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(imageSourceFile));
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            Log.e("NewSource", "data null!");
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (imageSourcePath != null) {
                //TODO: find out why imagePreview isn't getting set in onCreateView()
                imagePreview = (ImageView) getActivity().findViewById(R.id.image_preview);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                imageSource = BitmapFactory.decodeFile(imageSourcePath, options);
                imagePreview.setImageBitmap(imageSource);
                ((ItemListActivity) getActivity()).setImageSourcePath(imageSourcePath);
            } else {
                Toast.makeText(getActivity().getBaseContext(), "Please capture again", Toast.LENGTH_LONG).show();
            }


        }
    }

}
