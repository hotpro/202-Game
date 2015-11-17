package com.alfray.asqare.component_i;

import com.alfray.asqare.gameplay.Observer;

/**
 * Created by ivanybma on 11/16/15.
 */
public class ScoreBpOsv implements Observer {

    private int lst_bonus_score=0;
    @Override
    public void update(int mMoves, int mScore) {
        if(mScore-lst_bonus_score>tier)
        {
            timer countdown = new timer(parenthandler, getContext());
            Thread timethread = new Thread(countdown); //assign the new runnable object to the thread class and create a new thread object
            //with it

            parenthandler.post(new Runnable()	{
                @Override
                public void run()
                {

                    getContext().showbonuspanel();
                }
            });



            lst_bonus_score=mScore;
        }
    }
}
