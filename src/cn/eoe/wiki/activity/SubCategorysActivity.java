package cn.eoe.wiki.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.eoe.wiki.R;
import cn.eoe.wiki.json.CategoryChild;
import cn.eoe.wiki.json.CategoryJson;
import cn.eoe.wiki.listener.SubCategoryListener;
import cn.eoe.wiki.utils.WikiUtil;
import cn.eoe.wiki.view.SliderLayer;
import cn.eoe.wiki.view.SliderLayer.SliderListener;
/**
 * 用来处理第二层分类的界面
 * @author <a href="mailto:kris1987@qq.com">Kris.lee</a>
 * @data  2012-8-5
 * @version 1.0.0
 */
public class SubCategorysActivity extends CategorysActivity implements OnClickListener,SliderListener{
	public static final		String 	KEY_CATEGORY		= "category";
	public static final		String 	KEY_PARENT_TITLE	= "parent_title";
	
	private LinearLayout	mCategoryLayout;
	private LayoutInflater 	mInflater;
	private ImageView		mIvBack;
	private TextView		mTvParentName;
	private TextView		mTvTitleName;
	private TextView		mTvDescription;
	
	private boolean			mProgressVisible;
	private CategoryChild	mParentCategory;
	private String			mParentName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sub_categorys);
		mInflater = LayoutInflater.from(mContext);
		Intent intent  = getIntent();
		if(intent==null)
		{
			throw new NullPointerException("Must give a CategoryChild in the intent");
		}
		mParentCategory = intent.getParcelableExtra(KEY_CATEGORY);
		mParentName = intent.getStringExtra(KEY_PARENT_TITLE);
		if(mParentCategory==null || TextUtils.isEmpty(mParentName))
		{
			throw new NullPointerException("Must give a CategoryChild and the parent name in the intent");
		}
		getmMainActivity().getSliderLayer().addSliderListener(this);
		initComponent();
		initData();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	void initComponent() {
		mTvParentName = (TextView)findViewById(R.id.tv_title_parent);
		mTvTitleName = (TextView)findViewById(R.id.tv_title);
		mTvDescription = (TextView)findViewById(R.id.tv_description);
		mCategoryLayout = (LinearLayout)findViewById(R.id.layout_category);
		mIvBack=(ImageView)findViewById(R.id.iv_back);
		mIvBack.setOnClickListener(this);
	}

	void initData() {
		mTvParentName.setText(mParentName);
		mTvTitleName.setText(mParentCategory.getName());
		mTvDescription.setText(mParentCategory.getDescription());
		showProgressLayout();
	}
	
	protected void showProgressLayout()
	{
		View progressView = mInflater.inflate(R.layout.loading, null);
		mCategoryLayout.removeAllViews();
		mCategoryLayout.addView(progressView);
		mProgressVisible = true;
	}
	@Override
	protected void getCategorysError(String showText)
	{
		mCategoryLayout.removeAllViews();
		mProgressVisible = false;
		
		View viewError = mInflater.inflate(R.layout.loading_error, null);
		TextView tvErrorTip =  (TextView)viewError.findViewById(R.id.tv_error_tip);
		tvErrorTip.setText(showText);
		tvErrorTip.setTextColor(WikiUtil.getResourceColor(R.color.red, mContext));
		

		Button btnTryAgain =  (Button)viewError.findViewById(R.id.btn_try_again);
		btnTryAgain.setOnClickListener(this);
		mCategoryLayout.addView(viewError);
	}
	@Override
	protected void generateCategorys(CategoryJson responseObject)
	{
//		if(WikiConfig.isDebug()) return;
		List<CategoryChild> categorys =  responseObject.getContents();
		if(categorys!=null)
		{
			mCategoryLayout.removeAllViews();
			mProgressVisible = false;
			
			for(CategoryChild category:categorys)
			{
				LinearLayout categoryLayout = new LinearLayout(mContext);
				categoryLayout.setOrientation(LinearLayout.VERTICAL);
				LayoutParams titleParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				int paddind = WikiUtil.dip2px(mContext, 1);
				categoryLayout.setPadding(paddind, paddind, paddind, paddind);
				categoryLayout.setLayoutParams(titleParams);
				categoryLayout.setBackgroundResource(R.drawable.btn_grey_blue_stroke);
				mCategoryLayout.addView(categoryLayout);
				
				TextView tv = (TextView)mInflater.inflate(R.layout.category_title, null);
				tv.setText(category.getName());
				tv.setBackgroundResource(R.drawable.btn_grey_blue_nostroke_top);
				
				categoryLayout.addView(tv);
				List<CategoryChild> categorysChildren =  category.getChildren();
				if(categorysChildren!=null)
				{
					int size = categorysChildren.size();
					for (int i = 0; i < size; i++)
					{
						//add the line first
						View lineView = new View(mContext);
						LayoutParams blankParams = new LayoutParams(LayoutParams.MATCH_PARENT, WikiUtil.dip2px(mContext, 1));
						lineView.setLayoutParams(blankParams);
						lineView.setBackgroundResource(R.color.grey_stroke);
						categoryLayout.addView(lineView);
						//add the text
						CategoryChild categorysChild = categorysChildren.get(i);
						
						TextView tvChild = (TextView)mInflater.inflate(R.layout.category_item, null);
						tvChild.setText(categorysChild.getName());
						tvChild.setPadding(50, 0, 0, 0);
						tvChild.setTextColor(WikiUtil.getResourceColor(R.color.black, mContext));
						tvChild.setOnClickListener(new SubCategoryListener(categorysChild.getUri(), SubCategorysActivity.this));
						if(i==(size-1))
						{
							tvChild.setBackgroundResource(R.drawable.btn_white_blue_nostroke_bottom);
						}
						else
						{
							tvChild.setBackgroundResource(R.drawable.btn_white_blue_nostroke_nocorners);
						}
//						mCategoryLayout.addView(tvChild);
						categoryLayout.addView(tvChild);
					}
				}

				View blankView = new View(mContext);
				LayoutParams blankParams = new LayoutParams(LayoutParams.MATCH_PARENT, WikiUtil.dip2px(mContext, 8));
				blankView.setLayoutParams(blankParams);
				mCategoryLayout.addView(blankView);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_try_again:
			getCategory(mParentCategory.getUri());
			break;
		case R.id.iv_back:
			SliderLayer layer = getmMainActivity().getSliderLayer();
			layer.closeSidebar(layer.openingLayerIndex());
			break;
		default:
			break;
		}
	}

	@Override
	public void onSidebarOpened() {
		if(!mProgressVisible)
		{
			showProgressLayout();
		}
		getCategory(mParentCategory.getUri());
	}

	@Override
	public void onSidebarClosed() {
		
	}

	@Override
	public boolean onContentTouchedWhenOpening() {
		return false;
	}
}
