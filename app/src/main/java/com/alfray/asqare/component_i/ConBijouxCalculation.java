package com.alfray.asqare.component_i;

import com.alfray.asqare.gameplay.ScoreCalculator;

/**
 * Created by ivanybma on 11/19/15.
 */
public class ConBijouxCalculation extends BijouxCalculation {

    public ConBijouxCalculation(ScoreCalculator com){
        super(com);
    }

    @Override
    public int scoreCalculation(int mScore, int temp) {
        //**ivan s  here is replaced by a decorator******

        mScore=super.scoreCalculation(mScore, temp);
        mScore=extraCalculation(mScore,temp);

        return mScore;
    }

    private int extraCalculation(int mScore, int temp){

        //**ivan s  here will be replaced by a decorator******

        if(timer.current_cnt>0)
        {
            mScore+=temp;  //add one more time to achieve the double increasing points function
            ScoreBpOsv.lst_bonus_score+=temp*2;//this double increase bp not be included for next bonus chance
        }
        else if(BpPanelCmdExpRcv.bpincrease==1){
            mScore+=30;
            BpPanelCmdExpRcv.bpincrease=0;
            ScoreBpOsv.lst_bonus_score+=30; //this extra bp increase should not be included for next bonus chance
        }

        return mScore;
    }


}
