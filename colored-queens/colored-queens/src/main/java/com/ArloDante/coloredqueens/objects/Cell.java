package com.ArloDante.coloredqueens.objects;

//Merepresentasikan 1 cell/kotak dalam permainan colored queens untuk memudahkan memasukkan data ke dalam Map di dalam object Board
public class Cell {
    //menyimpan posisi dan warna dari kotak
    private int row;
    private int col;
    private int r;
    private int g;
    private int b;

    public Cell(int row, int col, int r, int g, int b) {
        this.row = row;
        this.col = col;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    // Getter dan setter
    public int getRow() { 
        return row; 
    }

    public void setRow(int row) { 
        this.row = row; 
    }

    public int getCol() { 
        return col; 
    }

    public void setCol(int col) { 
        this.col = col; 
    }

    public int getR() { 
        return r; 
    }

    public void setR(int r) { 
        this.r = r; 
    }

    public int getG() { 
        return g; 
    }

    public void setG(int g) { 
        this.g = g; 
    }

    public int getB() { 
        return b; 
    }
    public void setB(int b) { 
        this.b = b; 
    }

    @Override
    public String toString() {
        return String.format("Cell[row=%d, col=%d, rgb=(%d,%d,%d)]", row, col, r, g, b);
    }
}