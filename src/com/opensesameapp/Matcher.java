package com.opensesameapp;

import java.util.ArrayList;

import android.R.integer;

public class Matcher {
	public static double fineness = Math.PI / 180;
	public static double D = 0.45;

	public static double[] angles2PDF(ArrayList<Double> angles) {
		double[] pdf = new double[(int) (Math.PI / fineness)];
		for (Double angle : angles) {
			if ((int) (angle / fineness) == 180)
				pdf[179] += 1.0 / angles.size();
			else
				pdf[(int) (angle / fineness)] += 1.0 / angles.size();
		}
		return pdf;
	}

	public static double[] distances2PDF(ArrayList<Double> distances) {
		double[] pdf = new double[50];
		double maxDis = 0;

		for (Double distance : distances) {
			if (maxDis < distance)
				maxDis = distance;
		}

		for (Double distance : distances) {
			pdf[(int) (distance * 49 / maxDis)] += 1.0 / distances.size();
		}

		return pdf;
	}

	public static double getCharacteristicDistance(double[] pdf1, double[] pdf2) {
		double d = 0;

		for (int i = 0; i < pdf1.length; i++) {
			d += Math.abs(pdf1[i] - pdf2[i]);
		}

		return d;
	}
	
	public static double[] tuple2anglePDF(ArrayList<Tuple> tuples){
		ArrayList<Double> doubleArrayList = Fetcher.List2Angles(100000, tuples);
		return angles2PDF(doubleArrayList);
	}
	
	//对于两个波形进行整体特征分析，后两个参数只是调试用
    private double analyse(ArrayList<Tuple> testing, ArrayList<ArrayList<Tuple>> standard)
    {
        //待计算的常量，即绝对接受和绝对拒绝的阈值
        ArrayList<Double> total = new ArrayList<Double>();
        for(int i = 0; i < standard.size(); i++)
            for(int j = 0; j < standard.size(); j++)
                if (i != j)
                {
                	double[] iarray = tuple2anglePDF(standard.get(i));
                	double[] jarray = tuple2anglePDF(standard.get(j));
                    total.add(getCharacteristicDistance(iarray, jarray));
                }
        //相互匹配的结果设定为参考阈值，若没有则为经验常数0.15
        double reference;//待修正
        if (total.size() == 0 || DTW.average(total) > 100000)
            reference = 0.15;
        else
            reference = DTW.average(total);
        //计算testing指纹和standard的每一个指纹的特征差
        total.clear();
        double[] testArray = tuple2anglePDF(testing);
        for(int i = 0; i < standard.size(); i++)
        {
        	double[] data = tuple2anglePDF(standard.get(i));
        	double result = getCharacteristicDistance(testArray, data);
            if(result < 100)
                total.add(result);
        }
        double actual = DTW.average(total);
        return actual / reference;
    }
}
