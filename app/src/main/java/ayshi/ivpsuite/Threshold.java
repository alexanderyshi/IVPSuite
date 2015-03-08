package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
public class Threshold extends ImageHandler {




    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Threshold() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ARG_ITEM_ID = "threshold_detail_fragment";
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey("imageSource")) {
            // get the imagePath to use for the live preview
            imageSourcePath = getArguments().getString("imageSource");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.threshold_item_detail, container, false);
        ((TextView) rootView.findViewById(R.id.item_detail_text)).setText(ARG_ITEM_ID);
        imagePreview = (ImageView) rootView.findViewById(R.id.image_preview);
        if (imageSourcePath != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            imageSource = BitmapFactory.decodeFile(imageSourcePath, options);
            imagePreview.setImageBitmap(threshold(imageSource, 4));
            //TODO: reassign the modified image to the imageSourcePath
            ((ItemListActivity) getActivity()).setImageSourcePath(imageSourcePath);
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
