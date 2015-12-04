package edu.sjsu.cmpe202.component_i;

import android.os.Handler;
import android.os.SystemClock;

import edu.sjsu.cmpe202.AsqareContext;

/**
 * Created by ivanybma on 11/16/15.
 */
public class timer implements Runnable {
    private Long starttime=0L;
    private Long timeinmillisecond = 0l;
    private Long timeswapbuff = 0L;
    private Long updatetime=0L;
    private int secs;
    private int mins;
    private Handler parenthandler;
    private AsqareContext destcontext;
    boolean stoprst=false;
    public static int current_cnt=0;

    public timer(Handler iphandler, AsqareContext destcontext){
        parenthandler = iphandler;
        starttime = SystemClock.uptimeMillis();
        this.destcontext = destcontext;
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
        int seco =0;
        current_cnt++;
        while(timeinmillisecond/60000<1 && stoprst==false)
        {
            timeinmillisecond = SystemClock.uptimeMillis() - starttime;

            updatetime = timeswapbuff + timeinmillisecond;
            secs = (int) (updatetime/2000);
            mins = secs/60;
            secs = secs % 60;
            if(secs>10)
                break;

            if(secs!=seco)
            {
                seco=secs;
                parenthandler.post(new Runnable()	{
                    @Override
                    public void run()
                    {

                        destcontext.refreshWindowTitle(String.valueOf(" bonus timer: "+ String.valueOf(10-secs)));
                    }
                });
            }

        }

        parenthandler.post(new Runnable()	{
            @Override
            public void run()
            {
                destcontext.refreshWindowTitle(String.valueOf(""));
            }
        });
        current_cnt--;

    }

}

