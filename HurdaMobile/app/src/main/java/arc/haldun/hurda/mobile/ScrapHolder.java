package arc.haldun.hurda.mobile;

import arc.haldun.hurda.database.objects.Scrap;

public class ScrapHolder {

    private Scrap scrap;
    private double percentage;

    private OnPercentageChangeListener onPercentageChangeListener;

    public Scrap getScrap() {
        return scrap;
    }

    public void setScrap(Scrap scrap) {
        this.scrap = scrap;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
        if (onPercentageChangeListener != null)
            onPercentageChangeListener.onPercentageChange(percentage);
    }

    public OnPercentageChangeListener getOnPercentageChangeListener() {
        return onPercentageChangeListener;
    }

    public void setOnPercentageChangeListener(OnPercentageChangeListener onPercentageChangeListener) {
        this.onPercentageChangeListener = onPercentageChangeListener;
    }
}
