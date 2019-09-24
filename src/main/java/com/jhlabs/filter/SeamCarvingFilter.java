/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jhlabs.filter;

import com.jhlabs.image.AbstractBufferedImageOp;
import java.awt.image.BufferedImage;

/**
 *
 * @author manu
 */
public class SeamCarvingFilter extends AbstractBufferedImageOp {

    // Path class
    private final class Path {

        int energy = 0;            // total energy
        byte[] direction = null;    // direction of path (-1,0,+1) for each pixel
        int lowestoffset = 0;      // min, max relative offsets
        int highestoffset = 0;     // min, max relative offsets
    }
    //
    private int dstWidth;
    private int dstHeight;

    public SeamCarvingFilter(int width, int height) {
        this.dstWidth = width;
        this.dstHeight = height;
    }

    @Override
    public BufferedImage filterRGB32(BufferedImage src, BufferedImage dst) {

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        int[] inPixels = new int[srcWidth * srcHeight];
        getRGB(src, 0, 0, srcWidth, srcHeight, inPixels);

        if (dstWidth < srcWidth) {
            seamFilter(inPixels, srcWidth, srcHeight, dstWidth);

            int[] outPixels = new int[dstWidth * srcHeight];
            for (int y = 0; y < srcHeight; y++) {
                System.arraycopy(inPixels, y * srcWidth, outPixels, y * dstWidth, dstWidth);
            }
            inPixels = outPixels;
            srcWidth = dstWidth;
        }

        if (dstHeight < srcHeight) {
            int[] outPixels = transpose(inPixels, srcWidth, srcHeight, null);
            seamFilter(outPixels, srcHeight, srcWidth, dstHeight);

            inPixels = new int[srcWidth * dstHeight];
            for (int y = 0; y < srcWidth; y++) {
                for (int x = 0; x < dstHeight; x++) {
                    inPixels[y + x * dstWidth] = outPixels[y * srcHeight + x];
                }
            }
            srcHeight = dstHeight;
        }

        // create output image
        if (dst == null) {
            dst = createCompatibleDestImage(src, dstWidth, dstHeight);
        }
        setRGB(dst, 0, 0, dstWidth, dstHeight, inPixels);
        return dst;
    }

    private int[] transpose(int[] inPixels, int width, int height, int[] outPixels) {
        if (outPixels == null) {
            outPixels = new int[height * width];
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                outPixels[y + x * height] = inPixels[y * width + x];
            }
        }
        return outPixels;
    }

    private void seamFilter(int[] inPixels, int srcWidth, int srcHeight, int dstWidth) {

        // compute the whole energy
        int[] energy = new int[srcWidth * srcHeight];
        computeEnergy(inPixels, energy, srcWidth, srcHeight, 0, srcWidth);

        // compute all path
        int w = srcWidth;
        Path[] pathArray = new Path[srcWidth];
        for (int x = 0; x < srcWidth; x++) {
            Path path = new Path();
            path.direction = new byte[srcHeight];
            computeVerticalPath(energy, srcWidth, srcHeight, path, x, w);
            pathArray[x] = path;
        }

        // reduce image, line by line
        int vcount = srcWidth - dstWidth;
        for (int vloop = 0; vloop < vcount; vloop++) {

            // find best path
            int pathid = findBestPathId(pathArray, w);
            Path path = pathArray[pathid];

            // construct the new image
            for (int y = 0, x = pathid; y < srcHeight; y++) {
                int length = w - x - 1;  // move up all elements of arrays below the path
                System.arraycopy(energy, y * srcWidth + x + 1, energy, y * srcWidth + x, length);
                System.arraycopy(inPixels, y * srcWidth + x + 1, inPixels, y * srcWidth + x, length);
                x += path.direction[y];
            }

            // resize the image
            w--;

            // resize hpath array
            System.arraycopy(pathArray, pathid + 1, pathArray, pathid, w - pathid);

            // recompute only the energy for the changed region
            int xmin = Math.max(pathid - path.lowestoffset - 1, 0);
            int xmax = Math.min(pathid + path.highestoffset + 1, w);
            computeEnergy(inPixels, energy, srcWidth, srcHeight, xmin, xmax);

            // recompute only the path that go through the changed region
            for (int x = 0; x < w; x++) {
                Path p = pathArray[x];
                if ((x + p.highestoffset) >= xmin && (x - p.lowestoffset) <= xmax) {
                    computeVerticalPath(energy, srcWidth, srcHeight, p, x, w);
                }
            }
        }

    }

    private int[] computeEnergy(int[] inPixels, int[] energy, int width, int height, int xmin, int xmax) {

        for (int y = 0; y < height; y++) {
            for (int x = xmin; x < xmax; x++) {

                // Coordinates of 8 neighbours
                int px = x - 1; // previous x
                int nx = x + 1; // next x
                int py = y - 1; // previous y
                int ny = y + 1; // next y

                // Limit to image dimension
                if (px < 0) {
                    px = 0;
                }
                if (nx >= width) {
                    nx = width - 1;
                }
                if (py < 0) {
                    py = 0;
                }
                if (ny >= height) {
                    ny = height - 1;
                }

                // All pixels
                int p0 = inPixels[py * width + px];
                int p1 = inPixels[py * width + x];
                int p2 = inPixels[py * width + nx];
                int p3 = inPixels[y * width + px];
                int p4 = inPixels[y * width + nx];
                int p5 = inPixels[ny * width + px];
                int p6 = inPixels[ny * width + x];
                int p7 = inPixels[ny * width + nx];

                // Run the Sobel filter for the 8 neighbours
                int r0 = (p0 >> 16) & 0xFF;
                int r1 = (p1 >> 16) & 0xFF;
                int r2 = (p2 >> 16) & 0xFF;
                int r3 = (p3 >> 16) & 0xFF;
                int r4 = (p4 >> 16) & 0xFF;
                int r5 = (p5 >> 16) & 0xFF;
                int r6 = (p6 >> 16) & 0xFF;
                int r7 = (p7 >> 16) & 0xFF;

                int rgrady = (r6 - r1) + (r6 - r1) + (r5 - r0) + (r7 - r2);
                int rgradx = (r4 - r3) + (r4 - r3) + (r2 - r0) + (r7 - r5);
                int rgrad = rgradx * rgradx + rgrady * rgrady;

                int g0 = (p0 >> 8) & 0xFF;
                int g1 = (p1 >> 8) & 0xFF;
                int g2 = (p2 >> 8) & 0xFF;
                int g3 = (p3 >> 8) & 0xFF;
                int g4 = (p4 >> 8) & 0xFF;
                int g5 = (p5 >> 8) & 0xFF;
                int g6 = (p6 >> 8) & 0xFF;
                int g7 = (p7 >> 8) & 0xFF;

                int ggrady = (g6 - g1) + (g6 - g1) + (g5 - g0) + (g7 - g2);
                int ggradx = (g4 - g3) + (g4 - g3) + (g2 - g0) + (g7 - g5);
                int ggrad = ggradx * ggradx + ggrady * ggrady;
                if (ggrad > rgrad) {
                    rgrad = ggrad;
                }

                int b0 = p0 & 0xFF;
                int b1 = p1 & 0xFF;
                int b2 = p2 & 0xFF;
                int b3 = p3 & 0xFF;
                int b4 = p4 & 0xFF;
                int b5 = p5 & 0xFF;
                int b6 = p6 & 0xFF;
                int b7 = p7 & 0xFF;

                int bgrady = (b6 - b1) + (b6 - b1) + (b5 - b0) + (b7 - b2);
                int bgradx = (b4 - b3) + (b4 - b3) + (b2 - b0) + (b7 - b5);
                int bgrad = bgradx * bgradx + bgrady * bgrady;
                if (bgrad > rgrad) {
                    rgrad = bgrad;
                }

                energy[y * width + x] = rgrad;
            }
        }
        return energy;
    }

    private void computeVerticalPath(int[] energy, int width, int height, Path path, int xstart, int w) {

        path.energy = 0;

        // go from left to right
        int x = xstart, minx = xstart, maxx = xstart;

        for (int y = 0; y < height - 1; y++) {

            // update min/max absolute value
            if (x < minx) {
                minx = x;
            }
            if (x > maxx) {
                maxx = x;
            }

            // update total energy of the path
            path.energy += energy[y * width + x];

            // the 3 next possible positions for the current path
            int left = (x > 0) ? energy[(y + 1) * width + x - 1] : Integer.MAX_VALUE;
            int center = energy[(y + 1) * width + x];
            int right = (x < (w - 1)) ? energy[(y + 1) * width + x + 1] : Integer.MAX_VALUE;

            // find the lowest energy for the 3 positions
            if (left < right && left < center) {
                x -= 1;
                path.direction[y] = -1;

            }
            else if (right < center) {
                x += 1;
                path.direction[y] = 1;
            }
            else {
                path.direction[y] = 0;
            }
        }

        path.lowestoffset = xstart - minx;
        path.highestoffset = maxx - xstart;
    }

    /**
     * Find the best Vertical Path (ie. the path with the minimum energy)
     *
     * @return the path index with minimum energy
     */
    private int findBestPathId(Path[] vpath, int w) {
        int bestX = 0;
        int bestEnergy = Integer.MAX_VALUE;
        for (int x = 0; x < w; x++) {
            int energy = vpath[x].energy;
            if (energy < bestEnergy) {
                //if (vpe = 0) return x;
                bestEnergy = energy;
                bestX = x;
            }
        }
        return bestX;
    }
}
