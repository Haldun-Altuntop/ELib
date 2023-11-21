package arc.haldun.mylibrary.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import arc.haldun.mylibrary.R;

public class ViewSettingItem extends RelativeLayout {

    public static final String TITTLE = "tittle";
    public static final String SUB_TITTLE = "sub_tittle";

    private String tittle, subTittle;

    public ViewSettingItem(Context context) {
        super(context);
        init();
    }

    public ViewSettingItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ViewSettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public ViewSettingItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init() {
        inflate(getContext(), R.layout.setting_item, this);
    }

    private void init(AttributeSet attrs) {
        init();
/*
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ViewSettingItem);

        //category = typedArray.getString(R.styleable.ViewSettingItem_category);
        //setCategoryTittle(category);

        tittle = typedArray.getString(R.styleable.ViewSettingItem_tittle);
        setSettingTittle(tittle);

        subTittle = typedArray.getString(R.styleable.ViewSettingItem_sub_title);
        setSubTittle(subTittle);

        typedArray.recycle();

 */
    }

    @Deprecated
    public void setCategoryTittle(CharSequence category) {
        //TextView categoryTittle = findViewById(R.id.setting_item_category_tittle);
        //categoryTittle.setText(category);
    }

    public void setSettingTittle(CharSequence category) {
        TextView settingTittle = findViewById(R.id.setting_item_tittle);
        settingTittle.setText(category);
    }

    public void setSubTittle(CharSequence category) {
        TextView subTittle = findViewById(R.id.setting_item_sub_tittle);
        subTittle.setText(category);
    }
}
