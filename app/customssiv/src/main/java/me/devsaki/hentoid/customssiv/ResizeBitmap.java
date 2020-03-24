package me.devsaki.hentoid.customssiv;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicResize;
import android.renderscript.Type;

// Credits go to https://medium.com/@petrakeas/alias-free-resize-with-renderscript-5bf15a86ce3
class ResizeBitmap {

    static Bitmap successiveResize(Bitmap src, int resizeNum) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Bitmap output = src;
        for (int i = 0; i < resizeNum; i++) {
            srcWidth /= 2;
            srcHeight /= 2;
            Bitmap temp = Bitmap.createScaledBitmap(output, srcWidth, srcHeight, true);
            if (i != 0) { // don't recycle the src bitmap
                output.recycle();
            }
            output = temp;
        }
        return output;
    }

    static Bitmap successiveResizeRS(RenderScript rs, Bitmap src, int resizeNum) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Bitmap.Config config = src.getConfig();

        Allocation srcAllocation = Allocation.createFromBitmap(rs, src);
        ScriptIntrinsicResize resizeScript = ScriptIntrinsicResize.create(rs);

        Allocation outAllocation = null;
        for (int i = 0; i < resizeNum; i++) {
            srcWidth /= 2;
            srcHeight /= 2;

            Type t = Type.createXY(rs, srcAllocation.getElement(), srcWidth, srcHeight);
            outAllocation = Allocation.createTyped(rs, t);
            resizeScript.setInput(srcAllocation);
            resizeScript.forEach_bicubic(outAllocation);

            srcAllocation.destroy();
            srcAllocation = outAllocation;
        }

        Bitmap output = Bitmap.createBitmap(srcWidth, srcHeight, config);
        outAllocation.copyTo(output);

        resizeScript.destroy();
        outAllocation.destroy();

        return output;
    }

    static Bitmap resizeBitmap2(RenderScript rs, Bitmap src, float xScale, float yScale) {
        Bitmap.Config bitmapConfig = src.getConfig();
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int dstWidth = Math.round(srcWidth * xScale);
        int dstHeight = Math.round(srcHeight * yScale);

        /* Calculate gaussian's radius */
        float sigma = xScale / (float) Math.PI;
        // https://android.googlesource.com/platform/frameworks/rs/+/master/cpu_ref/rsCpuIntrinsicBlur.cpp
        float radius = 2.5f * sigma - 1.5f;
        radius = Math.min(25, Math.max(0.0001f, radius));

        /* Gaussian filter */
        Allocation tmpIn = Allocation.createFromBitmap(rs, src);
        Allocation tmpFiltered = Allocation.createTyped(rs, tmpIn.getType());
        ScriptIntrinsicBlur blurInstrinsic = ScriptIntrinsicBlur.create(rs, tmpIn.getElement());

        blurInstrinsic.setRadius(radius);
        blurInstrinsic.setInput(tmpIn);
        blurInstrinsic.forEach(tmpFiltered);

        tmpIn.destroy();
        blurInstrinsic.destroy();

        /* Resize */
        Bitmap dst = Bitmap.createBitmap(dstWidth, dstHeight, bitmapConfig);
        Type t = Type.createXY(rs, tmpFiltered.getElement(), dstWidth, dstHeight);
        Allocation tmpOut = Allocation.createTyped(rs, t);
        ScriptIntrinsicResize resizeIntrinsic = ScriptIntrinsicResize.create(rs);

        resizeIntrinsic.setInput(tmpFiltered);
        resizeIntrinsic.forEach_bicubic(tmpOut);
        tmpOut.copyTo(dst);

        tmpFiltered.destroy();
        tmpOut.destroy();
        resizeIntrinsic.destroy();

        return dst;
    }
}
