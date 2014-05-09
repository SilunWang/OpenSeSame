package com.opensesameapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.opensesameapp.Tuple;

import android.R.integer;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

public class Analysis {
	// �û���һ��ʹ�ñ������ѵ��ѧϰ��ÿ�����ݳ���Ӧ��Ϊ5��
	// ֮��ʹ�ñ�������������ж��Ƿ�Ϊ�κϷ��û���ÿ�����ݳ���Ϊ1��

	public static Handler msgHandler;//��Ҫ��LockActivity��һ��
	public static ArrayList<Tuple> CurrentReceiving;//����SensorData
	public static ArrayList<Tuple> finalData[] = new ArrayList[4];
	public static int finalData_length;
	public static Activity context;//������
	private static int currentStatus;
	private final static int NOT_RECEIVING = 0;//��û��ʼ
	private final static int RECEIVING_NOT_START = 1;//��δ��ʼ
	private final static int RECEIVING_STARTED = 2;//�Ѿ���ʼ
	private final static int RECEIVING_FINISH = 3;//����
	//��д��XML�е��ַ���
	public static String TempShared = "";
	public static Runnable analyzing;//�߳�
	public static double thresholdCoeff = 0.85;
	public static double sensibility = 1.5;
	private static int ShakeJudgeLength = 40;
	private static double ShakeJudgeStart = 10.5;
	private static double ShakeJudgeEnd = 9;

	private static ExecutorService transThread = Executors.newSingleThreadExecutor();
	private static Future transPending;	//���߳̽��п���
	private static String lock = "LOCK";

	// ��������
	public static void receiveData(ArrayList<Tuple> sensorData, int flag) {
		// TO DO
		if (currentStatus == NOT_RECEIVING) {
			currentStatus = RECEIVING_NOT_START;
			CurrentReceiving.addAll(sensorData);
			initThread();
		} else if (currentStatus != RECEIVING_FINISH) {
			CurrentReceiving.addAll(sensorData);
			synchronized (analyzing) {		//ͬ��
				analyzing.notify();	//���� run()
			}
		} else if (currentStatus == RECEIVING_FINISH) {
			return;
		}
	}

	public static void initThread() {
		//analyzing��ʼ��
		analyzing = new Runnable() {
			int start_pos = 0;
			int end_pos = 0;
			List<Tuple> q = new ArrayList<Tuple>();
			//��ʼִ��
			public void run() {
				double EX = 0;
				double EX2 = 0;
				// ����ǰ����
				for (int i = 0; i < ShakeJudgeLength; i++) {
					q.add(CurrentReceiving.get(i));
					double tempMod = Tuple.mod(CurrentReceiving.get(i));
					EX += tempMod;
					EX2 += tempMod * tempMod;
				}
				EX /= ShakeJudgeLength;
				EX2 /= ShakeJudgeLength;
				int pos = ShakeJudgeLength;
				// �����㣬�ҵ���ʼ��ֹͣ��λ��
				while (true) {
					double var = EX2 - EX * EX;
					// double var = calcVar(q);
					// ��δ��ʼ��ʱ�򷽲����һ����ֵ
					if (var > ShakeJudgeStart*sensibility && currentStatus == RECEIVING_NOT_START) {
						currentStatus = RECEIVING_STARTED;
						start_pos = pos;
					}
					// �ڿ�ʼ֮�󷽲�С��ĳ����ֵ
					else if (var <= ShakeJudgeEnd*sensibility && currentStatus == RECEIVING_STARTED) {
						currentStatus = RECEIVING_FINISH;
						end_pos = pos;
						break;
					}
					// ����ﵽĩβ��������
					if (pos >= CurrentReceiving.size() - 1) {
						try {
							if (start_pos == 0) {
								for (int i = 0; i < pos; i++) {
									CurrentReceiving.remove(0);
								}
								pos = 0;
							}
							synchronized (analyzing) {//����
								this.wait();
							}
						} catch (InterruptedException e) {
							System.out.println("AnalyzerS.analyzing is notified!");
						}
					}
					EX -= (Tuple.mod(q.get(0)) - Tuple.mod(CurrentReceiving.get(pos))) / ShakeJudgeLength;
					EX2 -= (Tuple.mod(q.get(0)) * Tuple.mod(q.get(0)) - Tuple.mod(CurrentReceiving.get(pos))
							* Tuple.mod(CurrentReceiving.get(pos)))/ ShakeJudgeLength;
					q.remove(0);
					q.add(CurrentReceiving.get(pos));
					pos++;
				}
				if (end_pos - start_pos < 40) {
					returnResult(Basic.ILLEGAL_SAMPLE);
					return;
				}
				ArrayList<Tuple> submitTuples = new ArrayList<Tuple>();
				submitTuples.addAll(CurrentReceiving.subList(start_pos, end_pos));
				judgeAndSendMessage(submitTuples);//�ж���������Ϣ
			}
			
		};
		transPending = transThread.submit(analyzing);
		
	}

	private static void clearSharedPreference() {
		SharedPreferences preferences = context.getSharedPreferences("SETTING_Infos", 0);
		preferences.edit().remove("samples");
	}

	protected static void judgeAndSendMessage(ArrayList<Tuple> list) {
		double m_distance = 0;
		if (finalData_length == 0) {
			finalData[finalData_length++] = list;
			returnResult(Basic.SUCCESS_AND_CONTINUE);
		}
		else {
			finalData[finalData_length++] = list;
			double threshold = DTW.getAccThreshold(finalData, finalData_length);
			for (int j = 0; j < finalData_length - 1; j++) {
				double distance = DTW.getDTWDistance(finalData[finalData_length - 1], finalData[j]);
				m_distance += distance;
				DTW.DTW_Dist[finalData_length-1][j] = distance;
				DTW.DTW_Dist[j][finalData_length-1] = distance;
			}
			
			if(m_distance/3.0 > thresholdCoeff * threshold){
				returnResult(Basic.UNMATCHED);
				return;
			}
			else if (finalData_length == 3) {
				int m_size = finalData[0].size()+finalData[1].size()+finalData[2].size();
				if (m_size < 180) {
					thresholdCoeff = 0.8;
				}
				else if (m_size < 240) {
					thresholdCoeff = 0.85;
				}
				else if (m_size < 300) {
					threshold = 0.95;
				}
				returnResult(Basic.SUCCESS_AND_OVER);
			}else if (finalData_length == 4) {
				reduction();
				returnResult(Basic.MATCHED);
			}else {
				returnResult(Basic.SUCCESS_AND_CONTINUE);
			}
		}
	}
	public static void reduction(){
		finalData_length--;
		for (int i = 0; i < 3; i++) {
			DTW.DTW_Dist[i][3] = 0;
			DTW.DTW_Dist[3][i] = 0;
		}
	}

	private static ArrayList<Tuple>[] String2TupleLists(String str) {
		String[] str_pdf = str.split("@");
		ArrayList<Tuple>[] lists = new ArrayList[str_pdf.length + 1];
		finalData_length = str_pdf.length;
		for (int i = 0; i < str_pdf.length; i++) {
			if (str_pdf[i].equals(""))
				continue;
			lists[i] = String2Tuple(str_pdf[i]);
		}
		return lists;
	}

	private static ArrayList<Tuple> String2Tuple(String str) {
		String[] ps = str.split("\n");
		ArrayList<Tuple> list = new ArrayList<Tuple>();
		for (int i = 0; i < ps.length; i++) {
			String[] xyz = ps[i].split(" ");
			double x = Double.parseDouble(xyz[0]);
			double y = Double.parseDouble(xyz[1]);
			double z = Double.parseDouble(xyz[2]);
			list.add(new Tuple(x, y, z));
		}
		return list;
	}
	
	private static String TupleList2String(ArrayList<Tuple> tupleData) {
		String str = "";
		for (int i = 0; i < tupleData.size(); i++) {
			str += tupleData.get(i).x + " " + tupleData.get(i).y + " " + tupleData.get(i).z + "\n";
		}
		return str;
	}
	
	private static String TupleLists2String(ArrayList<Tuple>[] lists) {
		String str = "";
		for (int i = 0; i < 3; i++) {
			str += TupleList2String(lists[i]) + "@";
		}
		return str;
	}

	// ���ؽ��
	private static void returnResult(int result) {
		Message msg = new Message();
		msg.what = result;
		msgHandler.sendMessage(msg);
	}

	// ȥ��sensorData�е�silent�Ĳ���
	private static void Filter() {
	}

	// �ϲ����sensorData
	private static void mergeData() {
	}

	public static void init(Activity context, Handler msgHandler) {
		Analysis.context = context;
		Analysis.msgHandler = msgHandler;
		Analysis.TempShared = "";
		Analysis.CurrentReceiving = new ArrayList<Tuple>();
		Analysis.currentStatus = Analysis.NOT_RECEIVING;
		if (Analysis.analyzing != null) {
			transPending.cancel(true);//����analyzing
		}
	}

	public static void startAnotherShake() {
		Analysis.CurrentReceiving = new ArrayList<Tuple>();
		Analysis.currentStatus = Analysis.NOT_RECEIVING;
		if (Analysis.analyzing != null) {
			transPending.cancel(true);
		}
	}

	public static void finalizeAnalyze() {
		if (Analysis.analyzing != null) {
			transPending.cancel(true);
		}
	}

	private static double calcVar(List<Tuple> q) {
		double avg = 0;
		for (int i = 0; i < ShakeJudgeLength; i++) {
			avg += Tuple.mod(q.get(i));
		}
		avg /= ShakeJudgeLength;
		double var = 0;
		for (int i = 0; i < ShakeJudgeLength; i++) {
			double mod = Tuple.mod(q.get(i));
			var += (mod - avg) * (mod - avg);
		}
		return var / ShakeJudgeLength;
	}

	public static void storeCurrentPDF() {
			TempShared += TupleLists2String(finalData);
	}

	public static void writeToShare() {
		SharedPreferences preferences = context.getSharedPreferences("SETTING_Infos", 0);
		preferences.edit().putString("samples", TempShared).commit();
	}

	public static void initSamples() {
		SharedPreferences preferences = context.getSharedPreferences("SETTING_Infos", 0);
		TempShared = preferences.getString("samples", "");
		finalData = String2TupleLists(TempShared);
	}
}
