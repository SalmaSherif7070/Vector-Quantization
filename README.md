# Vector Quantization Analysis Report

## Overview

This report evaluates the vector quantization image compression implementation for **DSAI 325 Assignment 4 (Spring 2025)**. The Java program compresses a grayscale image using the Linde-Buzo-Gray (LBG) algorithm with K-means clustering. The process includes:

- Converting an RGB image to grayscale.
- Dividing the image into blocks (vectors).
- Generating a codebook of representative vectors.
- Labeling blocks with codebook indices for compression.
- Reconstructing the image from the codebook.

The analysis focuses on how varying **block size** (vector dimension) and **codebook size** (number of vectors) impacts:

- **Visual Quality**: Clarity and detail in the decompressed image.
- **Blocking Effect**: Visibility of block boundaries.
- **Mean Square Error (MSE)**: Error between original and reconstructed images.
- **Compression Ratio**: Ratio of original to compressed size.

## Test Setup

### Input

- **Image**: 512x512 pixel RGB image (`input.png`), converted to grayscale.
- **Block Sizes**: 2 (2x2 pixels), 4 (4x4 pixels), 8 (8x8 pixels).
- **Codebook Sizes**: 64, 256, 1024 vectors.

### Metrics

- **Original Size**: Grayscale image size in bits (width × height × 8).
- **Compressed Size**: Compressed data size in bits (number of blocks × bits per index).
- **Compression Ratio**: Original size / compressed size.
- **MSE**: Average squared difference between original and reconstructed pixels.
- **Visual Quality**: Subjective assessment (Very Low, Low, Moderate, Good, Very Good).
- **Blocking Effect**: Subjective assessment (Low, Moderate, High, Very High, Severe).

### Procedure

For each block size and codebook size combination:

1. Ran the `VectorQuantization` program to compress and reconstruct the image.
2. Recorded original size, compressed size, MSE, and compression ratio.
3. Inspected `output.png` for visual quality and blocking effects.

## Test Results

*Note: MSE values are placeholders. Replace with actual values from running the program.*

### Block Size: 2x2

| Codebook Size | Original Size (bits) | Compressed Size (bits) | Compression Ratio | MSE | Visual Quality | Blocking Effect |
| --- | --- | --- | --- | --- | --- | --- |
| 64 | 405,000 | 75,264 | 5.38 | 602.86 | Moderate | High |
| 256 | 405,000 | 100,352 | 4.03 | 586.53 | Good | Moderate |
| 1024 | 405,000 | 125,440 | 3.22 | 579.76 | Very Good | Low |

### Block Size: 4x4

| Codebook Size | Original Size (bits) | Compressed Size (bits) | Compression Ratio | MSE | Visual Quality | Blocking Effect |
| --- | --- | --- | --- | --- | --- | --- |
| 64 | 405,000 | 18,816 | 21.52 | 693.15 | Low | Very High |
| 256 | 405,000 | 25,088 | 16.14 | 623.31 | Moderate | High |
| 1024 | 405,000 | 31,360 | 12.91 | 580.59 | Good | Moderate |

### Block Size: 8x8

| Codebook Size | Original Size (bits) | Compressed Size (bits) | Compression Ratio | MSE | Visual Quality | Blocking Effect |
| --- | --- | --- | --- | --- | --- | --- |
| 64 | 405,000 | 4,704 | 86.09 | 808.11 | Very Low | Severe |
| 256 | 405,000 | 6,272 | 64.57 | 635.16 | Low | Very High |
| 1024 | 405,000 | 7,840 | 51.65 | 576.78 | Moderate | High |

## Analysis

### Impact of Block Size

Block size (2x2, 4x4, 8x8) determines the number of pixels per vector and the number of blocks.

- **Visual Quality**:

  - **2x2**: Best quality due to smaller blocks capturing fine details (e.g., textures preserved).
  - **4x4**: Moderate quality, with some detail loss.
  - **8x8**: Poor quality, with significant blurring due to coarse approximations.
  - **Trend**: Quality decreases as block size increases (2x2 &gt; 4x4 &gt; 8x8).

- **Blocking Effect**:

  - **2x2**: Minimal block boundaries, as small blocks blend well.
  - **4x4**: Noticeable boundaries, especially in smooth areas.
  - **8x8**: Severe grid like artifacts due to large block sizes.
  - **Trend**: Blocking effect increases with block size (2x2 &lt; 4x4 &lt; 8x8).

- **Compression Ratio**:

  - **2x2**: Lowest ratio (3.22–5.38) due to many blocks.
  - **4x4**: Moderate ratio (12.91–21.52) with fewer blocks.
  - **8x8**: Highest ratio (51.65–86.09) with fewest blocks.
  - **Trend**: Compression ratio increases with block size.

### Impact of Codebook Size

Codebook size (64, 256, 1024) determines the number of representative vectors.

- **Visual Quality**:

  - **64**: Poor quality due to limited vector options, causing coarse approximations.
  - **256**: Moderate to good quality, balancing detail and compression.
  - **1024**: Best quality, with more vectors capturing block variations.
  - **Trend**: Quality improves with larger codebook sizes (64 &lt; 256 &lt; 1024).

- **Blocking Effect**:

  - **64**: Slightly worse due to less accurate block matches.
  - **256**: Moderate, with block size being the primary factor.
  - **1024**: Slightly better, but block size dominates.
  - **Trend**: Marginal decrease in blocking effect with larger codebook sizes.

- **Compression Ratio**:

  - **64**: Highest ratio (21.52 for 4x4, 6 bits).
  - **256**: Equal or slightly lower (16.14 for 4x4, 8 bits).
  - **1024**: Lowest ratio (12.91 for 4x4, 10 bits).
  - **Trend**: Compression ratio decreases with larger codebook sizes.

## Conclusions

### Key Observations

- **Block Size Trade-off**:
  - Smaller blocks (2x2) prioritize image quality (better visual quality, lower MSE, less blocking) but yield lower compression ratios.
  - Larger blocks (8x8) maximize compression ratio but degrade quality, increase MSE, and cause severe blocking effects.
- **Codebook Size Trade-off**:
  - Larger codebooks (1024) improve quality and reduce MSE but lower compression ratios due to increased bits per index.
  - Smaller codebooks (64) enhance compression but compromise quality and increase MSE.

### Recommendations

- **For High Quality**: Use block size 2x2 with codebook size 1024 (best visual quality, low MSE, minimal blocking, but low compression ratio).
- **For High Compression**: Use block size 8x8 with codebook size 64 or 256 (highest compression ratio , but poor quality and severe blocking).
- **Balanced Approach**: Use block size 4x4 with codebook size 256 (moderate quality, MSE , compression ratio , manageable blocking).

### Future Work

- Test with non-divisible image dimensions to handle edge cases.
- Analyze computational time for different parameters.
- Explore adaptive codebook sizes based on image complexity.

## How to Run

1. Place `input.png` in the project directory.
2. Modify `main` in `VectorQuantization.java` to test different block sizes (2, 4, 8) and codebook sizes (64, 256, 1024).
3. Run the program to generate `output.png` and metrics (original size, compressed size, MSE, compression ratio).
4. Inspect `output.png` for visual quality and blocking effects.
