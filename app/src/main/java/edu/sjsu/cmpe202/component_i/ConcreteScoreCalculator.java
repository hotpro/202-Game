package edu.sjsu.cmpe202.component_i;

import edu.sjsu.cmpe202.gameplay.ScoreCalculator;

/**
 * Created by ivanybma on 11/19/15.
 */
public class ConcreteScoreCalculator implements ScoreCalculator {

        @Override
        public int scoreCalculation(int mScore, int temp) {
            mScore += temp;
            return mScore;
            //**ivan e
        }
}
