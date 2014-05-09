package com.opensesameapp;

import java.util.ArrayList;

import android.R.integer;
import android.support.v4.content.Loader.ForceLoadContentObserver;

public class DTW {
	private ArrayList<Tuple> allTrainingData;//训练集中的所有数据
	private ArrayList<Tuple> allTestData;//训练集中的所有数据
	private static int samplingInterval = 10;//采样时间间隔
    private static int minimumDuration = 400;//采样要求的最短时间
    private static int DTWCoefficientForWidth = 10;//DTW矩阵通道宽度所占比例
    private static double DTWCoefficientForMatch = 1.2;//DTW相关倍乘系数，与相互匹配有关
    private static double DTWCoefficientForAmplitude = 0.8;//DTW相关倍乘系数，与最大振幅有关
    private static double DTWCoefficientForDefault = 0.6;//DTW相关倍乘系数，与最大振幅有关
    
    enum DTWSource { Insertion, Deletion, Match }
    public static double DTW_Dist[][] = new double[4][4];
    //计算两个序列的DTW距离
    public static double getDTWDistance(ArrayList<Tuple> trainingData, ArrayList<Tuple> testData)
    {
        if (trainingData.size() == 0 || testData.size() == 0)
            return 0;
        //如果两者长度差距过大，则匹配失败
        if (trainingData.size() * samplingInterval < minimumDuration || testData.size() * samplingInterval < minimumDuration)
            return 1000000;
        double res[][] = new double[trainingData.size() + 1][testData.size() + 1];
        DTWSource path[][] = new DTWSource[trainingData.size() + 1][testData.size() + 1];
        //初始化，填充正无穷
        for (int i = 0; i <= trainingData.size(); i++)
            for (int j = 0; j <= testData.size(); j++)
                res[i][j] = 1000000;
        res[0][0] = 0;
        //计算“中心区”训练文件第i-1个矢量到测试文件第j-1个矢量的距离
        double slope = (double)testData.size() / (double)trainingData.size();
        for (int i = 1; i <= trainingData.size(); i++)
            for (int j = Math.max(1, (int)((i - trainingData.size() * DTWCoefficientForWidth / 100) * slope));
                j <= Math.min(testData.size(), (int)((i + trainingData.size() * DTWCoefficientForWidth / 100) * slope));
                j++)
                res[i][j] = Tuple.dis(trainingData.get(i-1), testData.get(j-1));
        //计算总距离
        for (int i = 1; i <= trainingData.size(); i++)
        {
            for (int j = 1; j <= testData.size(); j++)
            {
                if (res[i - 1][j] <= res[i - 1][j - 1] && res[i - 1][j] <= res[i][j - 1])
                {
                    res[i][j] += res[i - 1][j];//insertion
                    path[i][j] = DTWSource.Insertion;
                }
                else if (res[i][j - 1] <= res[i - 1][ j - 1] && res[i][j - 1] <= res[i - 1][j])
                {
                    res[i][j] += res[i][j - 1];//deletion
                    path[i][j] = DTWSource.Deletion;
                }
                else
                {
                    res[i][j] += res[i - 1][j - 1];//match
                    path[i][j] = DTWSource.Match;
                }
            }
        }
        //统计路径长度
        int rows = trainingData.size();
        int columns = testData.size();
        int numOfSteps = 0;
        while (rows > 0 && columns > 0)
        {
            if (path[rows][columns] == DTWSource.Insertion)
                rows--;
            else if (path[rows][columns] == DTWSource.Deletion)
                columns--;
            else
            {
                rows--;
                columns--;
            }
            numOfSteps++;
        }
        //返回计算结果
        return res[trainingData.size()][testData.size()] / numOfSteps;
    }
    
    //得到Acc传感器下每个用户对应的阈值
    public static double getAccThreshold(ArrayList<Tuple>[] accUserList, int length)
    {
    	//double result = 1000000;
    	double amplitude = 0;
    	int count = 0;
    	for (int i = 0; i < length - 1; i++) {
    		if (DTW_Dist[i][length - 1] == 0) {
    			for (int j = 0; j < accUserList[i].size(); j++) 
    				DTW_Dist[i][length - 1] += Tuple.dis(accUserList[i].get(j), new Tuple(0, 0, 0));
    			DTW_Dist[length - 1][i] = DTW_Dist[i][length - 1];
			}
    	}
    	for (int i = 0; i < length - 1; i++) {
    		for (int j = i; j < length; j++) {
    			amplitude += DTW_Dist[i][j];
    			count++;
			}
		}
    	amplitude /= (double)count;
        ArrayList<Double> matchRes = new ArrayList<Double>();
        for (int j = 0; j < length; j++)
        {
            for(int k = j + 1; k < length; k++)
            {
               double res = getDTWDistance(accUserList[k], accUserList[j]);
               //忽略掉计算结果为无穷大和0的部分
               if (res < 1000000 && res > 0.001)
                   matchRes.add(res);
            }
        }
        return Math.min(average(matchRes)*DTWCoefficientForMatch, amplitude*DTWCoefficientForAmplitude);
    }
    
    public static double average(ArrayList<Double> array){
    	double result = 0;
    	for (int i = 0; i < array.size(); i++) {
			result += array.get(i);
		}
    	return result /(double)array.size();
    }
}
