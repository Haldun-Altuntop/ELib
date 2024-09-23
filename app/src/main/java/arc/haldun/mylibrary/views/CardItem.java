package arc.haldun.mylibrary.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import arc.haldun.mylibrary.R;

public class CardItem extends RelativeLayout {

    TextView tv_tittle, tv_subTittle;

    public CardItem(Context context) {
        super(context);
        init();
    }

    public CardItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CardItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public CardItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init() {

        inflate(getContext(), R.layout.card_item, this);

    }

    private void init(AttributeSet attrs) {
        init();

        String tittle, subTittle;
        boolean showIcon;
        ImageView image;

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CardItem);

        tv_tittle = findViewById(R.id.card_item_tv_tittle);
        tv_subTittle = findViewById(R.id.card_item_tv_sub_tittle);
        image = findViewById(R.id.card_item_image);

        tittle = typedArray.getString(R.styleable.CardItem_tittle);
        subTittle = typedArray.getString(R.styleable.CardItem_subTitle);
        showIcon = typedArray.getBoolean(R.styleable.CardItem_showIcon, false);

        tv_tittle.setText(tittle);
        tv_subTittle.setText(subTittle);

        if (showIcon) image.setVisibility(VISIBLE);
        else image.setVisibility(GONE);
    }

    public void setTittle(CharSequence tittle) {
        this.tv_tittle.setText(tittle);
    }

    public void addTitle(CharSequence tittle) {
        String remainingTittle = tv_tittle.getText().toString();
        tv_tittle.setText(String.format("%s%s", remainingTittle, tittle));
    }

    public void addSubTittle(CharSequence subTittle) {
        String remainingSubTittle = tv_subTittle.getText().toString();
        tv_subTittle.setText(String.format("%s%s", remainingSubTittle, subTittle));
    }

    public void setSubTittle(CharSequence subTittle) {
        this.tv_subTittle.setText(subTittle);
    }
}
