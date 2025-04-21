import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VectorQuantization {
    private BufferedImage image;
    private int blockSize;
    private int codebookSize;
    private List<double[]> codebook;
    private int[][] labels;
    private BufferedImage reconstructedImage;

    // Constructor: Initialize with image path, block size, and codebook size
    public VectorQuantization(String imagePath, int blockSize, int codebookSize) throws IOException {
        this.image = ImageIO.read(new File(imagePath));
        this.blockSize = blockSize;
        this.codebookSize = codebookSize;
        this.codebook = new ArrayList<>();
    }

    public void convertToGrayscale() {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                grayImage.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        image = grayImage;
    }

    public List<double[]> getBlocks() {
        List<double[]> blocks = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height - blockSize + 1; y += blockSize) {
            for (int x = 0; x < width - blockSize + 1; x += blockSize) {
                double[] block = new double[blockSize * blockSize];
                int index = 0;
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        block[index++] = image.getRGB(x + j, y + i) & 0xFF;
                    }
                }
                blocks.add(block);
            }
        }
        return blocks;
    }

    public void generateCodebook(List<double[]> blocks) {
        double[] initialCentroid = averageBlock(blocks);
        codebook.add(initialCentroid);
        while (codebook.size() < codebookSize) {
            List<double[]> newCodebook = new ArrayList<>();
            for (double[] vector : codebook) {
                double[] vector1 = new double[vector.length];
                double[] vector2 = new double[vector.length];
                for (int i = 0; i < vector.length; i++) {
                    vector1[i] = vector[i] * 1.01;
                    vector2[i] = vector[i] * 0.99;
                }
                newCodebook.add(vector1);
                newCodebook.add(vector2);
            }
            codebook = newCodebook;
            refineCodebook(blocks);
        }
    }

    private double[] averageBlock(List<double[]> blocks) {
        double[] avg = new double[blockSize * blockSize];
        for (double[] block : blocks) {
            for (int i = 0; i < block.length; i++) {
                avg[i] += block[i];
            }
        }
        for (int i = 0; i < avg.length; i++) {
            avg[i] /= blocks.size();
        }
        return avg;
    }

    // K-means
    private void refineCodebook(List<double[]> blocks) {
        boolean changed = true;
        while (changed) {
            changed = false;
            List<List<double[]>> clusters = new ArrayList<>();
            for (int i = 0; i < codebook.size(); i++) {
                clusters.add(new ArrayList<>());
            }

            // Assign blocks to nearest vector
            for (double[] block : blocks) {
                int nearest = findNearestVector(block);
                clusters.get(nearest).add(block);
            }

            // Update codebook vectors
            for (int i = 0; i < codebook.size(); i++) {
                if (!clusters.get(i).isEmpty()) {
                    double[] newVector = averageBlock(clusters.get(i));
                    if (euclidean_distance(codebook.get(i), newVector) > 0.0001) {
                        codebook.set(i, newVector);
                        changed = true;
                    }
                }
            }
        }
    }

    private int findNearestVector(double[] block) {
        int nearest = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < codebook.size(); i++) {
            double distance = euclidean_distance(block, codebook.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                nearest = i;
            }
        }
        return nearest;
    }

    private double euclidean_distance(double[] v1, double[] v2) {
        double sum = 0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public void compressImage(List<double[]> blocks) {
        int width = image.getWidth() / blockSize;
        int height = image.getHeight() / blockSize;
        labels = new int[height][width];
        int blockIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                labels[y][x] = findNearestVector(blocks.get(blockIndex++));
            }
        }
    }

    // Reconstruct image from labels
    public void reconstructImage() {
        int width = image.getWidth();
        int height = image.getHeight();
        reconstructedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < labels.length; y++) {
            for (int x = 0; x < labels[0].length; x++) {
                double[] vector = codebook.get(labels[y][x]);
                int index = 0;
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        int pixel = (int)vector[index++];
                        reconstructedImage.setRGB(x * blockSize + j, y * blockSize + i,
                                (pixel << 16) | (pixel << 8) | pixel);
                    }
                }
            }
        }
    }

    public double calculateMSE() {
        double mse = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = image.getRGB(x, y) & 0xFF;
                int reconPixel = reconstructedImage.getRGB(x, y) & 0xFF;
                mse += Math.pow(originalPixel - reconPixel, 2);
            }
        }
        return mse / (width * height);
    }

    public int calculateOriginalSize() {
        return image.getWidth() * image.getHeight() * 8;
    }

    public int calculateCompressedSize() {
        return (image.getWidth() / blockSize) * (image.getHeight() / blockSize) *
                (int)Math.ceil(Math.log(codebookSize) / Math.log(2));
    }

    public double calculateCompressionRatio() {
        int originalBits = calculateOriginalSize();
        int compressedBits = calculateCompressedSize();
        return (double)originalBits / compressedBits;
    }

    public void saveImage(String outputPath) throws IOException {
        ImageIO.write(reconstructedImage, "png", new File(outputPath));
    }

    public static void main(String[] args) {
        try {
            VectorQuantization vq = new VectorQuantization("input.png", 8, 1024);
            vq.convertToGrayscale();
            List<double[]> blocks = vq.getBlocks();
            vq.generateCodebook(blocks);
            vq.compressImage(blocks);
            vq.reconstructImage();
            vq.saveImage("output.png");
            System.out.println("Original Size (bits): " + vq.calculateOriginalSize());
            System.out.println("Compressed Size (bits): " + vq.calculateCompressedSize());
            System.out.println("Mean Square Error: " + vq.calculateMSE());
            System.out.println("Compression Ratio: " + vq.calculateCompressionRatio());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}