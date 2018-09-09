package edu.uic.cs440.group1.dungeon_crafter.Models;


        import java.util.ArrayList;

        import static java.lang.Math.abs;
        import static java.lang.Math.pow;
        import static java.lang.Math.sqrt;

public class Position
{
    private int row;
    private int column;

    public Position(int row, int column)
    {
        this.row = row;
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public boolean equals(Position position)
    {
        return (this.row == position.row && this.column == position.column);
    }

    public boolean isWithinRange(Position position, int range)
    {
        return (abs(position.row - row) <= range) && (abs(position.column - column) <= range);
    }

    public double calculateDistanceTo(Position position)
    {
        int row2 = position.getRow();
        int column2 = position.getColumn();

        return sqrt(pow(column2-column, 2) + pow(row2-row, 2));
    }

    public ArrayList<Position> getSurroundingPositions()
    {
        ArrayList<Position> surroundingPositions = new ArrayList<>();

        for(int i = -1; i <= 1; ++i)
            for(int j = -1; j <= 1; ++j)
                if(i != 0 || j != 0)
                    surroundingPositions.add(new Position(row + i, column + j));

        return surroundingPositions;
    }
}
