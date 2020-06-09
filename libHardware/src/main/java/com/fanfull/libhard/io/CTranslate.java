package com.fanfull.libhard.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/******************************************
 * Class CTranslate Description
 * 1. a hashtable saves the byte array and the name
 * 2. translate the string to byte array
 *
 * ****************************************/

public class CTranslate {
    public static final byte RowsOnce = 2;   //һ��Ĭ�ϴ�ӡ2�� ����2�еĲ�1�пհ�
    public static final byte ColumnByte = 16;  //printer most print 48 bytes(48*8 pixels)
    private CByteRecord m_recordNumber;
    private Paint paint;
    private Context mContext;

    public enum PaperDeep_t {
        One,   //�
        Two,
        Three,
        Four,
        Five
    }

    ;

    public enum PaperType_t {
        Bo,
        Hou
    }

    ;

    public enum FontSize_t {
        NORMAL,
        MIDDLE,
        BIG
    }

    ;

    public enum FontType_t {
        SONGTI,
        HEITI
    }

    ;


    public CTranslate(Context context) {
        this.mContext = context;
        m_recordNumber = null;
        paint = new Paint();
        //Init
        Typeface font = Typeface.createFromAsset(this.mContext.getAssets(), "fonts/songti.TTF");
        paint.setColor(Color.BLACK);
        paint.setStyle(Style.FILL_AND_STROKE);
        //		paint.setStrokeWidth(0.3f);
        paint.setTextSize(14.0f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(font);
        paint.setAntiAlias(false);
        paint.setDither(false);
    }

    public void setFontSize(FontSize_t size) {
        float fontsize = 0.0f;
        if (size == FontSize_t.NORMAL) {
            fontsize = 16.0f;
        } else if (size == FontSize_t.MIDDLE) {
            fontsize = 24.0f;
        } else {
            fontsize = 32.0f;
        }
        paint.setTextSize(fontsize);
    }

    public void setZiTi(FontType_t type) {
        String fonttype = "";
        if (type == FontType_t.SONGTI) {
            fonttype = "songti.TTF";
        } else {
            fonttype = "heiti.TTF";
        }
        Typeface font = Typeface.createFromAsset(this.mContext.getAssets(), "fonts/" + fonttype);
        paint.setTypeface(font);
    }

    private void saveMyBitmap(Bitmap mBitmap, String bitName) {
        File f = new File("/mnt//sdcard/" + bitName + ".png");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CByteRecord getString(String number, int x, int y) {
        String temp = number;
        if (m_recordNumber == null) {
            m_recordNumber = new CByteRecord();
        }
        Rect rect = new Rect();

        if (temp.trim().length() == 0) {  //��ӡ�ո�    ��ֵΪ0��ֻ��Ϊ�˻�ȡ��͸�
            number = "0";
        }

        //���ذ�Χ�����ַ�������С��һ��Rect����
        paint.getTextBounds(number, 0, number.length(), rect);

        int iHeight = rect.bottom + rect.height() + y;
        int iWidth = rect.left + rect.width() + x;

        if ((iWidth & 0x07) != 0) { //8����
            iWidth = ((iWidth >> 3) + 1) << 3;
        }
        if (iWidth > (ColumnByte * 8)) {
            iWidth = ColumnByte * 8;
        }

        if (temp.trim().length() == 0) {  //��ӡ�ո�
            number = " ";
        }

        Bitmap bitmap = Bitmap.createBitmap(iWidth, iHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawText(number, x, rect.height() - rect.bottom + y, paint);
        //		saveMyBitmap(bitmap,"ff"+System.currentTimeMillis());
        return getCByteRecord(bitmap);
    }


    public CByteRecord getBitmap(Bitmap data, int x, int y) {
        if (m_recordNumber == null) {
            m_recordNumber = new CByteRecord();
        }
        int iHeight = data.getHeight() + y;
        int iWidth = data.getWidth() + x;
        if ((iWidth & 0x07) != 0) { //8����
            iWidth = ((iWidth >> 3) + 1) << 3;
        }
        if (iWidth > (ColumnByte * 8)) {
            iWidth = ColumnByte * 8;
        }
        Bitmap bitmap = Bitmap.createBitmap(iWidth, iHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(data, x, y, paint);
        //		saveMyBitmap(bitmap,"bb"+System.currentTimeMillis());
        return getCByteRecord(bitmap);
    }

    private CByteRecord getCByteRecord(Bitmap bitmap) {
        if (m_recordNumber == null) {
            m_recordNumber = new CByteRecord();
        }
        byte[] bImage = new byte[bitmap.getHeight() * (bitmap.getWidth() >> 3)];
        m_recordNumber.m_iHeight = bitmap.getHeight();
        m_recordNumber.m_iWidth = (bitmap.getWidth() >> 3);
        m_recordNumber.m_bData = bImage;

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int iIndexB = 0;
        int iValue = 0;
        int iIndex = 0;
        int ixWidth = m_recordNumber.m_iWidth;
        int ixHeight = m_recordNumber.m_iHeight;
        for (int j = 0; j < ixHeight; j++) {
            for (int k = 0; k < ixWidth; k++) {
                iValue = 0;
                for (int l = 0; l < 8; l++) {
                    iValue = iValue << 1;
                    iValue = iValue | (pixels[iIndex] & 0x01);
                    iIndex++;
                }
                m_recordNumber.m_bData[iIndexB++] = (byte) ((~iValue) & 0xFF); //������1��ʾ��ɫ������ӡ��1��ӡ������ȡ��
            }
        }
        bImage = null;
        pixels = null;
        return m_recordNumber;
    }
}



