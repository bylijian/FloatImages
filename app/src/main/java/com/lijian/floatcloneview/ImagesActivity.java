package com.lijian.floatcloneview;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.chrisbanes.photoview.PhotoView;

/**
 * 图集
 * Created by lijian on 2017/11/21.
 */

public class ImagesActivity extends AppCompatActivity {

    private static final String TAG = "ImagesActivity";
    LinearLayout mYfTitleLayout;
    LinearLayout mYfBottomLayout;
    private View mBackground;
    FloatViewPager mViewPager;
    FrameLayout mYfCloneLayout;
    private PhotoView mCurrentPhoto;
    private int mSellectIndex = 0;
    int image[] = new int[]{R.mipmap.image01, R.mipmap.image02, R.mipmap.image03, R.mipmap.image04, R.mipmap.image05};
    private MotionEvent mLastMoveEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yf_act_images);
        mYfTitleLayout = findViewById(R.id.yf_title_layout);
        mYfBottomLayout = findViewById(R.id.yf_bottom_layout);
        mBackground = findViewById(R.id.background_view);
        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(new PagerAdapter() {

            public Object instantiateItem(ViewGroup container, int position) {
                View itemView = LayoutInflater.from(ImagesActivity.this).inflate(R.layout.item_image, container, false);
                PhotoView photoView = itemView.findViewById(R.id.image);
                int index = position % image.length;
                photoView.setImageResource(image[index]);
                photoView.setTag(position);
                container.addView(itemView);
                return itemView;
            }


            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }

            @Override
            public int getCount() {
                return image.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSellectIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setPositionListener(new FloatViewPager.OnPositionChangeListener() {
            @Override
            public void onPositionChange(int initTop, int nowTop, float ratio) {
                float alpha = 1 - Math.min(1, ratio * 5);
                mYfBottomLayout.setAlpha(alpha);
                mYfTitleLayout.setAlpha(alpha);
                mBackground.setAlpha(Math.max(0, 1 - ratio));
            }

            @Override
            public void onFlingOutFinish() {
                finish();
            }
        });
        mViewPager.setDisallowInterruptHandler(new FloatViewPager.DisallowInterruptHandler() {
            @Override
            public boolean disallowInterrupt() {
                PhotoView view = mViewPager.findViewWithTag(mSellectIndex);
                return view.getScale() != 1;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
