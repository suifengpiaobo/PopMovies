package br.com.tiagohs.popmovies.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import br.com.tiagohs.popmovies.R;
import br.com.tiagohs.popmovies.model.dto.ImageDTO;
import br.com.tiagohs.popmovies.util.ImageUtils;
import br.com.tiagohs.popmovies.util.enumerations.ImageSize;
import br.com.tiagohs.popmovies.util.enumerations.TypeShowImage;
import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoView;

public class WallpaperPagerAdapter extends PagerAdapter implements View.OnClickListener {

    @BindView(R.id.wallpaper_img)           PhotoView mWallpaperImage;
    @BindView(R.id.image_item_progress)     ProgressWheel mProgress;

    private Context mContext;
    private List<ImageDTO> mImageDTOs;
    private ImageDTO mCurrentImage;
    private ActionBar mToolbar;
    private boolean mIsVisible;
    private TypeShowImage mTypeShowImage;

    public WallpaperPagerAdapter(Activity context, List<ImageDTO> imageDTOs, ActionBar toolbar, TypeShowImage typeShowImage) {
        mContext = context;
        mImageDTOs = imageDTOs;
        this.mToolbar = toolbar;
        mIsVisible = true;
        this.mTypeShowImage = typeShowImage;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_wallpaper_detail, container, false);

        ButterKnife.bind(this, view);
        view.setOnClickListener(this);

        mCurrentImage = mImageDTOs.get(position);
        Log.i("Walll", "Path: " + mCurrentImage.getImagePath());

        if (mTypeShowImage.equals(TypeShowImage.WALLPAPER_IMAGES))
            ImageUtils.load(mContext, mCurrentImage.getImagePath(), mWallpaperImage, R.drawable.placeholder_images_default, R.drawable.placeholder_images_default, ImageSize.BACKDROP_1280, mProgress);
        else
            ImageUtils.load(mContext, mCurrentImage.getImagePath(), R.drawable.placeholder_images_default, R.drawable.placeholder_images_default,  mWallpaperImage, mProgress);

        container.addView(view);

        return view;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    public PhotoView getCurrentImageView() {
        return mWallpaperImage;
    }

    public ImageDTO getCurrentImage() {
        return mCurrentImage;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mImageDTOs.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onClick(View view) {
        mIsVisible = !mIsVisible;
        if (mIsVisible)
            mToolbar.show();
        else
            mToolbar.hide();
    }
}
