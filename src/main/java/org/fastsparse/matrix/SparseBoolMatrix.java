package org.fastsparse.matrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SparseBoolMatrix {
	int nrow;
	
	ArrayList<ArrayList<Integer>> cols;
	
	public SparseBoolMatrix() {
		cols = new ArrayList<ArrayList<Integer>>();
	}
	
	private ArrayList<Integer> getOrCreateCol(int col) {
		for (int i = cols.size() - 1; i < col; i++) {
			cols.add( new ArrayList<Integer>() );
		}
		return cols.get(col);
	}
	
	public void readFromFile(String file) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("\"")) {
					continue;
				}
				String[] elems = line.split(",");
				if (elems.length != 2) {
					continue;
				}
				int col = Integer.parseInt(elems[0]) - 1;
				int row = Integer.parseInt(elems[1]) - 1;
				if (row >= nrow) {
					nrow = row + 1;
				}
				getOrCreateCol(col).add(row);
			}
			br.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public long sum() {
		long sum = 0;
		for (int i = 0; i < cols.size(); i++) {
			sum += cols.get(i).size();
		}
		return sum;
	}
	
	public int nrow() {
		return nrow;
	}
	
	public int ncol() {
		return cols.size();
	}
	
	public SparseBoolMatrix extractCols(int startCol, int endColExclusive) {
		SparseBoolMatrix m = new SparseBoolMatrix();
		m.nrow = this.nrow;
		for (int col = startCol; col < endColExclusive; col++) {
			m.cols.add( this.cols.get(col) );
		}
		return m;
	}
	
	public String getStats() {
		return String.format("[ %d x %d ] with %d nonzeros.", nrow(), ncol(), sum() );
	}

	public static void main(String[] args) {
		SparseBoolMatrix m = new SparseBoolMatrix();
		m.readFromFile("data/P56817.X.csv");
		System.out.println( "m = " + m.getStats() );
		
		SparseBoolMatrix m1 = m.extractCols(0, 2759);
		SparseBoolMatrix m2 = m.extractCols(2759, m.ncol());
		System.out.println( "m1 = " + m1.getStats() );
		System.out.println( "m2 = " + m2.getStats() );
		
	}

}
