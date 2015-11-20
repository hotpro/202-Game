package com.alfray.asqare.component_i;

import com.alfray.asqare.gameplay.ScoreCalculator;

/**
 * Created by ivanybma on 11/19/15.
 */
public class BijouxCalculation implements ScoreCalculator {

    //**ivan s
    private  ScoreCalculator scoreCalcomp;

    public BijouxCalculation(ScoreCalculator com){
        scoreCalcomp=com;
    }
    //**ivan e

    @Override
    public int scoreCalculation(int mScore, int temp) {
        //**ivan s  here is replaced by a decorator******

        return scoreCalcomp.scoreCalculation(mScore,temp);
        //**ivan  return mScore;
    }
}