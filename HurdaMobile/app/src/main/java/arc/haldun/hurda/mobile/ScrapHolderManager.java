package arc.haldun.hurda.mobile;

import java.util.ArrayList;

import arc.haldun.hurda.database.objects.Mixture;
import arc.haldun.hurda.database.objects.Scrap;

public class ScrapHolderManager {

    private static final ArrayList<ScrapHolder> scrapHolders = new ArrayList<>();

    public static OnEnergyCalculatedListener onEnergyCalculatedListener;

    public static int totalPercentage = 0;

    public static void addScrapHolder(ScrapHolder scrapHolder) {
        scrapHolders.add(scrapHolder);
    }

    public static void removeScrapHolder(ScrapHolder scrapHolder) {
        scrapHolders.remove(scrapHolder);
    }

    public static ScrapHolder getScrapHolder(int position) {
        return scrapHolders.get(position);
    }

    public static int getSize() {
        return scrapHolders.size();
    }

    public static void calculateEnergy() {

        double energy = 0;
        for (ScrapHolder scrapHolder : scrapHolders) {
            energy += calculateScrapEnergy(scrapHolder.getScrap()) * scrapHolder.getPercentage() / 100;
        }

        if (onEnergyCalculatedListener != null) onEnergyCalculatedListener.onEnergyCalculated(energy);

        totalPercentage = 0;
        for (ScrapHolder scrapHolder : scrapHolders) {
            totalPercentage += scrapHolder.getPercentage();
        }
    }

    public static Mixture getMixture() {
        Mixture mixture = new Mixture();
        for (ScrapHolder scrapHolder : scrapHolders) {
            if (scrapHolder.getPercentage() == 0) continue;
            mixture.addScrap(scrapHolder.getScrap(), scrapHolder.getPercentage());
        }
        return mixture;
    }

    private static double calculateScrapEnergy(Scrap scrap) {

        double energy =
                scrap.getP09_Fe() * 0.357 * 10
                + scrap.getP15_SiO2() * 0.483 * 10
                + scrap.getP12_CaO() * 0.375 * 10
                + (scrap.getP10_O() * 56/16) * 0.444 * 10
                + (1610 - 1527) * 0.3
                + (scrap.getP10_O() * 56/16) * 58/1000
                + (0*34);

        energy = ((int) energy * 100) / 100.0;

        return energy;
    }

    public static void calculateTotalPercentage() {
        totalPercentage = 0;
        for (ScrapHolder scrapHolder : scrapHolders) {
            totalPercentage += scrapHolder.getPercentage();
        }
    }
}
