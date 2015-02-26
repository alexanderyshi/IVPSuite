package ayshi.ivpsuite;

import android.hardware.camera2.CameraCaptureSession;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

import ayshi.ivpsuite.MenuContent;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class NewSourceDetailFragment extends android.app.Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "new_source_detail_fragment";

    /**
     * The dummy content this fragment is presenting.
     */
    private MenuContent.MenuItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewSourceDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = MenuContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
//        createCaptureSession(List, CameraCaptureSession.StateCallback, Handler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.item_detail_text)).setText(mItem.content);
        }

        //note: this activity will only serve to get an image path for use in the other activities
        //TODO: button for create camera session
        //TODO: button for choosing bitmap from sd card


        return rootView;
    }
}
