package tw.skyarrow.ehreader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import tw.skyarrow.ehreader.BaseApplication;
import tw.skyarrow.ehreader.Constant;
import tw.skyarrow.ehreader.R;
import tw.skyarrow.ehreader.db.DaoMaster;
import tw.skyarrow.ehreader.db.DaoSession;
import tw.skyarrow.ehreader.db.Photo;
import tw.skyarrow.ehreader.db.PhotoDao;

/**
 * Created by SkyArrow on 2014/2/2.
 */
public class PhotoBookmarkDialog extends DialogFragment {
    public static final String TAG = "PhotoBookmarkDialog";

    private SQLiteDatabase db;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private PhotoDao photoDao;

    private long galleryId;
    private List<Photo> photoList;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        galleryId = args.getLong("id");

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getActivity(), Constant.DB_NAME, null);
        db = helper.getReadableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        photoDao = daoSession.getPhotoDao();

        QueryBuilder qb = photoDao.queryBuilder();
        qb.where(qb.and(PhotoDao.Properties.GalleryId.eq(galleryId), PhotoDao.Properties.Bookmarked.eq(true)));
        photoList = qb.list();

        if (photoList.size() > 0) {
            int size = photoList.size();
            String[] menuItems = new String[size];

            for (int i = 0; i < size; i++) {
                menuItems[i] = String.format(getString(R.string.bookmark_list_item),
                        photoList.get(i).getPage());
            }

            dialog.setItems(menuItems, onItemClick);
        } else {
            dialog.setMessage(R.string.no_bookmarks);
        }

        dialog.setTitle(R.string.bookmark_list);

        MapBuilder builder = MapBuilder.createAppView();
        builder.set(Fields.SCREEN_NAME, TAG);

        BaseApplication.getTracker().send(builder.build());

        return dialog.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        db.close();
    }

    private DialogInterface.OnClickListener onItemClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Photo photo = photoList.get(i);

            if (photo == null) return;

            ((PhotoActivity) getActivity()).setCurrent(photo.getPage() - 1, false);
        }
    };
}
