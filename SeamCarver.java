import java.awt.Color;

/**
 * 
 */

/**
 * @author Adam Dziedzic
 * 
 */
public class SeamCarver {

    private Picture picture;
    private int width;
    private int height;
    private double[][] tab;

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("SeamCarver");
    }

    public SeamCarver(Picture picture) {
        this.picture = picture;
    }

    /**
     * current picture
     * 
     * @return
     */
    public Picture picture() {
        return picture;
    }

    /**
     * width of current picture
     * 
     * @return
     */
    public int width() {
        return picture.width();
    }

    /**
     * height of current picture
     * 
     * @return
     */
    public int height() {
        return picture.height();
    }

    private int getWidth() {
        return this.width;
    }

    private int getHeight() {
        return this.height;
    }

    /**
     * Energy of pixel at column x and row y in current picture.
     * 
     * Computing the energy of a pixel. We will use the dual gradient energy
     * function: The energy of pixel (x, y) is Δx2(x, y) + Δy2(x, y), where the
     * square of the x-gradient Δx2(x, y) = Rx(x, y)2 + Gx(x, y)2 + Bx(x, y)2,
     * and where the central differences Rx(x, y), Gx(x, y), and Bx(x, y) are
     * the absolute value in differences of red, green, and blue components
     * between pixel (x + 1, y) and pixel (x − 1, y). The square of the
     * y-gradient Δy2(x, y) is defined in an analogous manner. We define the
     * energy of pixels at the border of the image to be 2552 + 2552 + 2552 =
     * 195075.
     * 
     * @param x
     * @param y
     * @return
     */
    public double energy(int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height()) {
            throw new IndexOutOfBoundsException();
        }
        if (x == 0 || x == width() - 1 || y == 0 || y == height() - 1) {
            return 195075;
        }
        double xDelta = deltaSquared(x + 1, y, x - 1, y);
        double yDelta = deltaSquared(x, y + 1, x, y - 1);

        return xDelta + yDelta;
    }

    /**
     * the square of the x-gradient Δx2(x, y) = Rx(x, y)2 + Gx(x, y)2 + Bx(x,
     * y)2, and where the central differences Rx(x, y), Gx(x, y), and Bx(x, y)
     * are the absolute value in differences of red, green, and blue components
     * between pixel (x + 1, y) and pixel (x − 1, y) x1,y1 represents x, x2,y2
     * represents y
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    private double deltaSquared(int x1, int y1, int x2, int y2) {
        Color c1 = picture.get(x1, y1);
        Color c2 = picture.get(x2, y2);

        int red1 = c1.getRed();
        int red2 = c2.getRed();

        int green1 = c1.getGreen();
        int green2 = c2.getGreen();

        int blue1 = c1.getBlue();
        int blue2 = c2.getBlue();

        return differenceSquared(red1, red2)
                + differenceSquared(green1, green2)
                + differenceSquared(blue1, blue2);
    }

    private double differenceSquared(int x, int y) {
        int difference = x - y;
        return (double) (difference * difference);
    }

    /*
     * sequence of indices for horizontal seam in current picture
     */
    public int[] findHorizontalSeam() {
        this.width = height();
        this.height = width();
        tab = new double[height()][width()];
        for (int j = 0; j < height(); ++j) {
            for (int i = 0; i < width(); ++i) {
                tab[j][i] = energy(i, j);
            }
        }
        return findSeam();
    }

    /**
     * sequence of indices for vertical seam in current picture
     * 
     * @return
     */
    public int[] findVerticalSeam() {
        this.width = width();
        this.height = height();
        tab = new double[width()][height()];
        for (int j = 0; j < height(); ++j) {
            for (int i = 0; i < width(); ++i) {
                tab[i][j] = energy(i, j);
            }
        }
        return findSeam();
    }

    private int[] findSeam() {
        int[][] from = new int[getWidth()][getHeight()];
        double[][] cost = new double[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); ++i) {
            for (int j = 0; j < getHeight(); ++j) {
                cost[i][j] = Double.MAX_VALUE;
            }
        }
        for (int i = 0; i < getWidth(); ++i) {
            cost[i][0] = tab[i][0];
        }
        for (int j = 0; j < getHeight() - 1; ++j) {
            for (int i = 0; i < getWidth(); ++i) {
                for (int k = i - 1; k <= i + 1; ++k) {
                    if (k >= 0 && k < getWidth()) {
                        if (cost[k][j + 1] > cost[i][j] + tab[k][j + 1]) {
                            cost[k][j + 1] = cost[i][j] + tab[k][j + 1];
                            from[k][j + 1] = i;
                        }
                    }
                }
            }
        }
        double minCost = Double.MAX_VALUE;
        int lastIndex = -1;
        for (int i = 0; i < getWidth(); ++i) {
            if (cost[i][getHeight() - 1] < minCost) {
                minCost = cost[i][getHeight() - 1];
                lastIndex = i;
            }
        }
        int[] result = new int[getHeight()];
        result[getHeight() - 1] = lastIndex;
        for (int j = getHeight() - 1; j > 0; --j) {
            result[j - 1] = from[result[j]][j];
        }
        return result;
    }

    /**
     * remove horizontal seam from current picture
     * 
     * @param a
     */
    public void removeHorizontalSeam(int[] a) {
        if (height() <= 1) {
            throw new IllegalArgumentException("too small picture");
        }
        if (a.length != width()) {
            throw new IllegalArgumentException("Wrong length of the seam.");
        }
        checkSeam(a);
        Picture newPicture = new Picture(width(), height() - 1);
        for (int i = 0; i < width(); ++i) {
            if (a[i] < 0 || a[i] >= height())
                throw new IllegalArgumentException(
                        "an entry is outside its prescribed range");
            for (int j = 0; j < height(); ++j) {
                if (j < a[i])
                    newPicture.set(i, j, picture.get(i, j));
                else if (j > a[i])
                    newPicture.set(i, j - 1, picture.get(i, j));
            }
        }
        picture = newPicture;
        System.out.println("1 horizontal line removed.");
    }

    /**
     * remove vertical seam from current picture
     * 
     * @param a
     */
    public void removeVerticalSeam(int[] a) {
        if (width() <= 1) {
            throw new IllegalArgumentException("too small picture");
        }
        if (a.length != height()) {
            throw new IllegalArgumentException("Wrong length of the seam.");
        }
        checkSeam(a);
        Picture newPicture = new Picture(width() - 1, height());
        for (int j = 0; j < height(); ++j) {
            if (a[j] < 0 || a[j] >= width())
                throw new IllegalArgumentException(
                        "an entry is outside its prescribed range");
            for (int i = 0; i < width(); ++i) {
                if (i < a[j])
                    newPicture.set(i, j, picture.get(i, j));
                else if (i > a[j])
                    newPicture.set(i - 1, j, picture.get(i, j));
            }
        }
        picture = newPicture;
        System.out.println("1 vertical line removed.");
    }

    /**
     * throw an exception if two adjacent entries differ by more than 1
     * 
     * @param a
     *            a seam
     */
    private void checkSeam(int[] a) {
        for (int i = 1; i < a.length; ++i) {
            if (Math.abs(a[i - 1] - a[i]) > 1)
                throw new IllegalArgumentException(
                        "two adjacent entries differ by more than 1");
        }
    }

}
