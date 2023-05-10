package labCollection;

import data.LabWork;

import java.util.Comparator;

/**
 * Compare LabWorks by the amount of points per difficulty.
 */
public class PointsPerDifficultyComparator implements Comparator<LabWork> {

    @Override
    public int compare(LabWork o1, LabWork o2) {
        return o1.countPointsPerDifficulty() - o2.countPointsPerDifficulty();
    }

}
