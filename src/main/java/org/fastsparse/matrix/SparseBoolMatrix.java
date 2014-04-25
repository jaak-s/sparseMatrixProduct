package org.fastsparse.matrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.fastsparse.util.Timer;

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
	
	/**
	 * Very efficiently computes:
	 * 1) cross-product of two sparse boolean matrices 
	 * 2) sum along the rows of the resulting matrix.
	 * 
	 * @param b
	 * @return rowSums( t(a) %*% b ), matrix a is this object.
	 */
	public int[] prodSum(SparseBoolMatrix b) {
		int[] rowSum = new int[ cols.size() ];
		
		int[] fcounts = new int[ nrow ];
		for (int col = 0; col < cols.size(); col++) {
			ArrayList<Integer> colValues = cols.get(col);
			for (int i = 0; i < colValues.size(); i++) {
				fcounts[colValues.get(i)]++;
			}
		}
		
		// creating arrays for each feature, using the fcounts
		int[][] feat2cols = new int[ nrow ][];
		for (int row = 0; row < nrow; row++) {
			feat2cols[row] = new int[ fcounts[row] ];
		}
		
		for (int col = 0; col < cols.size(); col++) {
			for (int row : cols.get(col)) {
				feat2cols[row][ --fcounts[row] ] = col;
			}
		}
		
		// multiplying and summing the rows
		for (int col2 = 0; col2 < b.cols.size(); col2++) {
			for (int feat : b.cols.get(col2)) {
				int[] cols1 = feat2cols[feat];
				for (int c : cols1) {
					rowSum[c]++;
				}
			}
		}
		
		return rowSum;
	}
	
	/**
	 * Very efficiently computes cross-product between two sparse boolean matrices. 
	 * 
	 * @param b
	 * @return t(a) %*% b, stored in single int vector, column oriented. Matrix a is this object.
	 */
	public int[] prod(SparseBoolMatrix b) {
		int[] prod = new int[ cols.size() * b.cols.size() ];
		
		int[] fcounts = new int[ nrow ];
		for (int col = 0; col < cols.size(); col++) {
			ArrayList<Integer> colValues = cols.get(col);
			for (int i = 0; i < colValues.size(); i++) {
				fcounts[colValues.get(i)]++;
			}
		}
		
		// creating arrays for each feature, using the fcounts
		int[][] feat2cols = new int[ nrow ][];
		for (int row = 0; row < nrow; row++) {
			feat2cols[row] = new int[ fcounts[row] ];
		}
		
		for (int col = 0; col < cols.size(); col++) {
			for (int row : cols.get(col)) {
				feat2cols[row][ --fcounts[row] ] = col;
			}
		}
		
		// multiplying and summing the rows
		int add = 0;
		for (int col2 = 0; col2 < b.cols.size(); col2++) {
			for (int feat : b.cols.get(col2)) {
				int[] cols1 = feat2cols[feat];
				for (int c : cols1) {
					prod[add + c]++;
				}
			}
			add += cols.size();
		}
		
		return prod;
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

		// testing prod():
		Timer.tic();
		int[] prod = m1.prod(m2);
		Timer.toc(">>> m1.prod(m2)");
		System.out.println( 
			String.format("prod = [ %d, %d, %d, %d, %d, %d, ... ]", 
					prod[0], prod[1], prod[2], prod[3], prod[4], prod[5] )
			// should be prod = [ 14, 14, 11, 12, 12, 15... ]
		); 

		// testing prod() other way: (should not be used in practice because m1 is bigger than m2)
		Timer.tic();
		prod = m2.prod(m1);
		Timer.toc(">>> m2.prod(m1)");

		// testing prodSum():
		Timer.tic();
		int[] prodSum = m1.prodSum(m2);
		Timer.toc(">>> m1.prodSum( m2 )");
		System.out.println( "p[0] = " + prodSum[0] + " (should be "+ 38198 +")" ); 
		System.out.println( "p[1] = " + prodSum[1] + " (should be "+ 34573 +")" );
	}

}
