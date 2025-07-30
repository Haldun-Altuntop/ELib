package arc.haldun.hurda.mobile;

import arc.haldun.hurda.database.objects.Scrap;

public class ScrapHolder {

    private Scrap scrap;
    private double percentage;

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
    }
}
