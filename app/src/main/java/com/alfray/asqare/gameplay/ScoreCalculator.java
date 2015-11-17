package com.alfray.asqare.gameplay;

import com.alfray.asqare.component_i.timer;

/**
 * Created by yunlongxu on 11/16/15. Strategy Pattern
 */
public interface ScoreCalculator {
    public int scoreCalculation(int mScore, int temp);
}

class BijouxCalculation implements ScoreCalculator {

    @Override
    public int scoreCalculation(int mScore, int temp) {
       //**ivan s
        if(timer.current_cnt>0)
            temp=temp*2;
        //**ivan e
        mScore += temp;

        return mScore;
    }
}

class BoucheBeeCalculation implements ScoreCalculator {

    @Override
    public int scoreCalculation(int mScore, int temp) {
        for(; temp > 0; temp--) {
            mScore += temp;
        }
        return mScore;
    }
}

class PenaltyCalculation implements ScoreCalculator {

    @Override
    public int scoreCalculation(int mScore, int temp) {
        mScore -= (mScore * temp) / 100;
        return mScore;
    }
}
