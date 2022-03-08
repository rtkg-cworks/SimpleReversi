package sReversi.System;
 /**
 * 盤面の状態を保持するクラス
 * 0: no disc
 * 1: color1 (Black)
 * 2: color2 (White)
 */
public class BoardContainer {

    private int size;
    private int[][] data;
    private int[][] initState;

    public BoardContainer( int size ) { init( size ); }

    public void init( int size ) {

        this.size = size;
        initState = new int[size * 2][size * 2];

        initState[size - 1][size - 1] = 2;
        initState[size - 1][size] = 1;
        initState[size][size - 1] = 1;
        initState[size][size] = 2;

        data = initState;
    }

    public int getSize() { return size; }

    /**
    * 指定された座標が盤面の範囲外の場合 -1 を返します。
    * returns -1 if coordinates is out of bounds
    */
    public int get( int row, int column ) {

        if ( row >= 0 && row < size * 2 && column >= 0 && column < size * 2 ) {
            return data[row][column];
        }
        else { return -1; }
    }

    public int[][] get() { return data; }

    public void set( int value, int row, int column ) { data[row][column] = value; }

    public int getCount( int colorIndex ) {

        int cnt = 0;
        for (int[] i : data) {
            for (int j : i) {
                if ( j == colorIndex ) cnt++;
            }
        }
        return cnt;
    }

    public int getCount() {

        int cnt = 0;
        for (int[] i : data) {
            for (int j : i) {
                if ( j != 0 ) cnt++;
            }
        }
        return cnt;
    }
}
