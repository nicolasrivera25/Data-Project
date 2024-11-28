public class Asiento {
    private String section;
    private int row;
    private int seatNumber;

    public Asiento(String section, int row, int seatNumber) {
        this.section = section;
        this.row = row;
        this.seatNumber = seatNumber;
    }

    public String getSection() {
        return section;
    }

    public int getRow() {
        return row;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    @Override
    public String toString() {
        return section + " - Row " + row + " Seat " + seatNumber;
    }
}
