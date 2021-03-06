package ayshi.ivpsuite;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;



/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details
 * (if present) is a {@link NewSource}.
 * <p/>
 * This activity also implements the required
 * {@link ItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity
        implements ItemListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private String currentItemDetailFragmentID;
    private ImageHandler currentItemDetailFragment;
    private String sourceImagePath;
    private int imageWidth = -1;
    private int imageHeight = -1;
    private Bitmap.Config config;
    private int[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);

            currentItemDetailFragmentID = R.id.item_list + "";
        }

        // If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        Log.i("onItemSelected", id);
        if (currentItemDetailFragmentID != id){
            if (mTwoPane) {
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.

                //save Bitmap before switching out
                if (currentItemDetailFragment != null){
                    currentItemDetailFragment.saveBitmap();
                }else{
                    Log.e("ItemListActivity", "getByteArray returned null");
                }

                Bundle arguments = new Bundle();
                if(byteArray != null){
                    arguments.putIntArray("byteArray", byteArray);
                }
                if(imageWidth != -1){
                    arguments.putInt("imageWidth", imageWidth);
                }
                if(imageHeight != -1){
                    arguments.putInt("imageHeight", imageHeight);
                }
                if(config != null){
                    String configString = "";
                    switch (config){
                        case ARGB_8888:
                            configString = "ARGB_8888";
                            break;
                    }
                    arguments.putString("config", configString);
                }

                switch(id){
                    case "new_source":
                        currentItemDetailFragment = new NewSource();
                        break;
                    case "threshold":
                        currentItemDetailFragment = new Threshold();
                        break;
                    case "gamma_correction":
                        currentItemDetailFragment = new GammaCorrection();
                        break;
                    case "histogram_transform":
                        currentItemDetailFragment = new HistogramTransformer();
                        break;
                    default:
                        id = "INVALID";
                        break;
                }
                if (id != "INVALID"){
                    currentItemDetailFragment.setArguments(arguments);
                    try{
                        getFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, currentItemDetailFragment)
                                .commit();
                        currentItemDetailFragmentID = id;
                    }
                    catch(Exception e){
                        Log.i("ItemListActivity", e.toString());
                    }

                } else {
                    // In single-pane mode, simply start the detail activity
                    // for the selected item ID.
//                    Intent detailIntent = new Intent(this, ItemDetailActivity.class);
//                    detailIntent.putExtra(NewSource.ARG_ITEM_ID, id);
//                    startActivity(detailIntent);
                }
            }
        }
    }

    //accessors and mutators

    public void setImageSourcePath(String path){
        sourceImagePath = path;
        Log.e("ItemListActivity", sourceImagePath);
    }

    public String getSourceImagePath(){
        return sourceImagePath;
    }

    public void setByteArray(int[] _byteArray){
        byteArray = _byteArray;
    }

    public int[] getByteArray(){
        return byteArray;
    }

    public void setImageWidth(int _imageWidth){
        imageWidth = _imageWidth;
    }

    public int getImageWidth(){
        return imageWidth;
    }

    public void setImageHeight(int _imageHeight){
        imageHeight = _imageHeight;
    }

    public int getImageHeight(){
        return imageHeight;
    }

    public void setBitmapConfig(Bitmap.Config _config){
        config = _config;
    }

    public Bitmap.Config getBitmapConfig(){
        return config;
    }
}
