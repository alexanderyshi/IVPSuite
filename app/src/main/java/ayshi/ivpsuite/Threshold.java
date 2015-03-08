package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.NumberPicker;

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

    int levels;
    NumberPicker numberPicker;
    Bitmap mutableBitmap;

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

        levels = 0;
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
            mutableBitmap = imageSource.copy(Bitmap.Config.ARGB_8888, true);
            imagePreview.setImageBitmap(imageSource);
        }

        final Button thresholdButton = (Button) rootView.findViewById(R.id.button_threshold);
        thresholdButton.setOnClickListener(this);

        numberPicker = (NumberPicker) rootView.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(0);
        return rootView;
    }

    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.button_threshold){
            mutableBitmap = threshold(imageSource, numberPicker.getValue());
            imagePreview.setImageBitmap(mutableBitmap);
            //TODO: reassign the modified image to the imageSourcePath
            ((ItemListActivity) getActivity()).setImageSourcePath(imageSourcePath);
        }
    }
}
