package com.opensesameapp;

import java.util.ArrayList;

import android.R.integer;
import android.support.v4.content.Loader.ForceLoadContentObserver;

public class DTW {
	private ArrayList<Tuple> allTrainingData;//ѵ�����е���������
	private ArrayList<Tuple> allTestData;//ѵ�����е���������
	private static int samplingInterval = 10;//����ʱ����
    private static int minimumDuration = 400;//����Ҫ������ʱ��
    private static int DTWCoefficientForWidth = 10;//DTW����ͨ�������ռ����
    private static double DTWCoefficientForMatch = 1.2;//DTW��ر���ϵ�������໥ƥ���й�
    private static double DTWCoefficientForAmplitude = 0.8;//DTW��ر���ϵ�������������й�
    private static double DTWCoefficientForDefault = 0.6;//DTW��ر���ϵ�������������й�
    
    enum DTWSource { Insertion, Deletion, Match }
    public static double DTW_Dist[][] = new double[4][4];
    //�����������е�DTW����
    public static double getDTWDistance(ArrayList<Tuple> trainingData, ArrayList<Tuple> testData)
    {
        if (trainingData.size() == 0 || testData.size() == 0)
            return 0;
        //������߳��Ȳ�������ƥ��ʧ��
        if (trainingData.size() * samplingInterval < minimumDuration || testData.size() * samplingInterval < minimumDuration)
            return 1000000;
        double res[][] = new double[trainingData.size() + 1][testData.size() + 1];
        DTWSource path[][] = new DTWSource[trainingData.size() + 1][testData.size() + 1];
        //��ʼ�������������
        for (int i = 0; i <= trainingData.size(); i++)
            for (int j = 0; j <= testData.size(); j++)
                res[i][j] = 1000000;
        res[0][0] = 0;
        //���㡰��������ѵ���ļ���i-1��ʸ���������ļ���j-1��ʸ���ľ���
        double slope = (double)testData.size() / (double)trainingData.size();
        for (int i = 1; i <= trainingData.size(); i++)
            for (int j = Math.max(1, (int)((i - trainingData.size() * DTWCoefficientForWidth / 100) * slope));
                j <= Math.min(testData.size(), (int)((i + trainingData.size() * DTWCoefficientForWidth / 100) * slope));
                j++)
                res[i][j] = Tuple.dis(trainingData.get(i-1), testData.get(j-1));
        //�����ܾ���
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
        //ͳ��·������
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
        //���ؼ�����
        return res[trainingData.size()][testData.size()] / numOfSteps;
    }
    
    //�õ�Acc��������ÿ���û���Ӧ����ֵ
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
               //���Ե�������Ϊ������0�Ĳ���
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
