package arc.haldun.hurda.mobile;

import arc.haldun.hurda.database.objects.Scrap;

public class ScrapHolder {

    private Scrap scrap;
    private int percentage;

    public Scrap getScrap() {
        return scrap;
    }

    public void setScrap(Scrap scrap) {
        this.scrap = scrap;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
