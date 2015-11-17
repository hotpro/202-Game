package com.alfray.asqare.component_i;

import com.alfray.asqare.AsqareContext;
import com.alfray.asqare.gameplay.Observer;
import android.os.Handler;

/**
 * Created by ivanybma on 11/16/15.
 */
public class ScoreBpOsv implements Observer {

    private int lst_bonus_score=0;
    private Handler parenthandler;
    private AsqareContext destcontext;
    public static Integer tier=70;

    public ScoreBpOsv(Handler parenthandler,AsqareContext destcontext){

        this.parenthandler=parenthandler;
        this.destcontext=destcontext;

    }

    @Override
    public void update(int mMoves, int mScore) {
        if(mScore-lst_bonus_score>=tier && timer.current_cnt==0)
        {
            parenthandler.post(new Runnable()	{
                @Override
                public void run()
                {
                    destcontext.showbonuspanel();
                }
            });
            lst_bonus_score=mScore;
        }
    }
}
