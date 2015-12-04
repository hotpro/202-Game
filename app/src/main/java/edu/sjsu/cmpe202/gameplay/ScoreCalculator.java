package edu.sjsu.cmpe202.gameplay;

/**
 * Created by yunlongxu on 11/16/15. Strategy Pattern
 */
public interface ScoreCalculator {
    public int scoreCalculation(int mScore, int temp);
}

//**commented by ivan, this class is separated into a class file under package component_i for decorator usage
//
//class BijouxCalculation implements ScoreCalculator {
//
//    //**ivan s
//    private  ScoreCalculator scoreCalcomp;
//
//    public BijouxCalculation(ScoreCalculator com){
//        scoreCalcomp=com;
//    }
//    //**ivan e
//
//    @Override
//    public int scoreCalculation(int mScore, int temp) {
//       //**ivan s  here is replaced by a decorator******
//
//        return scoreCalcomp.scoreCalculation(mScore,temp);
//        //**ivan  return mScore;
//    }
//}

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
