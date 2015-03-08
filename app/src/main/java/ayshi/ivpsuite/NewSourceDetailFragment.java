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
public class NewSourceDetailFragment extends android.app.Fragment implements View.OnClickListener{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "new_source_detail_fragment";
    ImageView sourcePreview;
    File imageSource;
    String sourceImagePath;
    Bitmap sourceImage;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewSourceDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the imagePath to use for the live preview
        if (getArguments().containsKey("sourceImage")) {
            sourceImagePath = getArguments().getString("sourceImage");
            Log.e("NewSourceDetailFragment", "Bundle argument");
        }
        else{
            sourceImagePath = ((ItemListActivity)getActivity()).getSourceImagePath();
            Log.e("NewSourceDetailFragment", "ItemListActivityAccessor");
        }
        Log.e("NewSourceDetailFragment", ""+sourceImagePath);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.new_source_item_detail, container, false);
        ((TextView) rootView.findViewById(R.id.item_detail_text)).setText(ARG_ITEM_ID);

        sourcePreview = (ImageView) rootView.findViewById(R.id.source_preview);
        if (sourceImagePath != null) {
            sourcePreview.setImageBitmap(BitmapFactory.decodeFile(sourceImagePath));
        }
        final Button callCameraButton = (Button) rootView.findViewById(R.id.button_call_camera);

        callCameraButton.setOnClickListener(this);

        return rootView;
    }
    public void onClick(View view) {
        if (view.getId() == R.id.button_call_camera){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                imageSource = null;
                try {
                    imageSource = createImageFile();
                } catch (IOException e) {
                    // Error occurred while creating the File
                    e.printStackTrace();
                }
                // Continue only if the File was successfully created
                Log.e("NewSourceDetailFragment", sourceImagePath);
                if (imageSource != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(imageSource));
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        sourceImagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            Log.e("NewSourceDetailFragment", "data null!");
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Log.e("NewSourceDetailFragment", "requestCode and resultCode OK");
            if (sourceImagePath != null) {
                //TODO: find out why sourcePreview isn't getting set in onCreateView()
                sourcePreview = (ImageView) getActivity().findViewById(R.id.source_preview);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                sourceImage = BitmapFactory.decodeFile(sourceImagePath, options);
                sourcePreview.setImageBitmap(sourceImage);
                ((ItemListActivity) getActivity()).setSourceImagePath(sourceImagePath);

            } else {
                Toast.makeText(getActivity().getBaseContext(), "Please capture again", Toast.LENGTH_LONG).show();
            }


        }
    }

}
