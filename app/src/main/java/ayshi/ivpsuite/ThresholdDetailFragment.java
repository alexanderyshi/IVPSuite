package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ThresholdDetailFragment extends android.app.Fragment {

    public static final String ARG_ITEM_ID = "threshold_detail_fragment";
    private String sourceImagePath;
    ImageView sourcePreview;
    Bitmap sourceImage;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ThresholdDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey("sourceImage")) {
            // get the imagePath to use for the live preview
            sourceImagePath = getArguments().getString("sourceImage");
            Log.e("ThresholdDetailFragment", "Bundle argument");
        }
        else{
            sourceImagePath = ((ItemListActivity)getActivity()).getSourceImagePath();
            Log.e("ThresholdDetailFragment", "ItemListActivityAccessor");
        }
        Log.e("ThresholdDetailFragment", ""+sourceImagePath);
//        if (getArguments().containsKey(ARG_ITEM_ID)) {
//            // Load the dummy content specified by the fragment
//            // arguments. In a real-world scenario, use a Loader
//            // to load content from a content provider.
//            mItem = MenuContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
//        }
//        createCaptureSession(List, CameraCaptureSession.StateCallback, Handler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.threshold_item_detail, container, false);
        ((TextView) rootView.findViewById(R.id.item_detail_text)).setText(ARG_ITEM_ID);
        sourcePreview = (ImageView) rootView.findViewById(R.id.image_preview);
        if (sourceImagePath != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            sourceImage = BitmapFactory.decodeFile(sourceImagePath, options);
            sourcePreview.setImageBitmap(threshold(sourceImage, 4));
            ((ItemListActivity) getActivity()).setSourceImagePath(sourceImagePath);
        }
        return rootView;
    }

    private Bitmap threshold(Bitmap image, int levels){
        int[] byteArray = new int[image.getWidth() * image.getHeight()];
        image.getPixels(byteArray, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        int divisor = 256/levels;
        for (int i = 0; i<byteArray.length; i++){
            byteArray[i] = byteArray[i]/divisor*divisor;
        }
        return Bitmap.createBitmap(byteArray, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    }
}
