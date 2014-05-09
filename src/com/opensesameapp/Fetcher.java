package com.opensesameapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.opensesameapp.Tuple;

public class Fetcher {
	public static ArrayList<Double> List2Angles(int n, List<Tuple> tuples) {
		ArrayList<Double> angles = new ArrayList<Double>();
		for (int i = 0; i < n; i++) {
			int[] pos = getRandomInt(tuples.size(), 3);
			Double angle = getAngleFrom3Points(tuples.get(pos[0]),
					tuples.get(pos[1]), tuples.get(pos[2]));
			if (angle.doubleValue() != angle.doubleValue()) {
				i--;
				continue;
			}
			angles.add(angle);
		}
		return angles;
	}

	public static ArrayList<Double> List2Distances(int n, ArrayList<Tuple> tuples) {
		ArrayList<Double> distances = new ArrayList<Double>();

		for (int i = 0; i < n; i++) {
			int[] pos = getRandomInt(tuples.size(), 2);
			Double distance = Tuple.dis(tuples.get(pos[0]), tuples.get(pos[1]));
			distances.add(distance);
		}

		return distances;
	}

	private static Double getAngleFrom3Points(Tuple B, Tuple A, Tuple C) {
		Tuple AB = Tuple.sub(A, B);
		Tuple AC = Tuple.sub(A, C);
		return Math.acos(Tuple.mul(AB, AC) / Tuple.mod(AC) / Tuple.mod(AB));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static int[] getRandomInt(int range, int n) {
		if (range == 0)
			return null;
		HashMap map = new HashMap();
		int[] result = new int[n];
		for (int i = 0; i < n; i++) {
			Random r = new Random();
			int pos = Math.abs(r.nextInt()) % (range - i) + i;
			if (map.get(pos) == null) {
				map.put(pos, i);
				result[i] = pos;
			} else {
				result[i] = (Integer) map.get(pos);
				map.put(pos, i);
			}
		}
		return result;
	}
}