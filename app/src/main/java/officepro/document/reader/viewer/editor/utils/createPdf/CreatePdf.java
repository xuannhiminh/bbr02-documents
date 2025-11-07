package officepro.document.reader.viewer.editor.utils.createPdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import officepro.document.reader.viewer.editor.utils.createPdf.model.ImageToPDFOptions;
import officepro.document.reader.viewer.editor.utils.createPdf.model.Watermark;
import officepro.document.reader.viewer.editor.utils.createPdf.model.WatermarkPageEvent;
import com.ezteam.baseproject.utils.PathUtils;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * An async task that converts selected images to Pdf
 */
public class CreatePdf extends AsyncTask<String, String, String> {

    private final String mFileName;
    private final String mPassword;
    private final String mQualityString;
    private final ArrayList<String> mImagesUri;
    private final int mBorderWidth;
    private final OnPDFCreatedInterface mOnPDFCreatedInterface;
    private boolean mSuccess;
    private String mPath;
    private final String mPageSize;
    private final boolean mPasswordProtected;
    private Boolean mWatermarkAdded;
    private Watermark mWatermark;
    private int mMarginTop;
    private int mMarginBottom;
    private int mMarginRight;
    private int mMarginLeft;
    private String mImagescaleType;
    private String mPageNumStyle;
    private String mMasterPwd;
    private int mPageColor;
    private Context context;

    public CreatePdf(Context context, ImageToPDFOptions mImageToPDFOptions, String parentPath,
                     OnPDFCreatedInterface onPDFCreated) {
        this.context = context;
        this.mImagesUri = mImageToPDFOptions.getImagesUri();
        this.mFileName = mImageToPDFOptions.getOutFileName();
        this.mPassword = mImageToPDFOptions.getPassword();
        this.mQualityString = mImageToPDFOptions.getQualityString();
        this.mOnPDFCreatedInterface = onPDFCreated;
        this.mPageSize = mImageToPDFOptions.getPageSize();
        this.mPasswordProtected = mImageToPDFOptions.isPasswordProtected();
        this.mBorderWidth = mImageToPDFOptions.getBorderWidth();
        this.mWatermarkAdded = mImageToPDFOptions.isWatermarkAdded();
        this.mWatermark = mImageToPDFOptions.getWatermark();
        this.mMarginTop = mImageToPDFOptions.getMarginTop();
        this.mMarginBottom = mImageToPDFOptions.getMarginBottom();
        this.mMarginRight = mImageToPDFOptions.getMarginRight();
        this.mMarginLeft = mImageToPDFOptions.getMarginLeft();
        this.mImagescaleType = mImageToPDFOptions.getImageScaleType();
        this.mPageNumStyle = mImageToPDFOptions.getPageNumStyle();
        this.mMasterPwd = mImageToPDFOptions.getMasterPwd();
        this.mPageColor = mImageToPDFOptions.getPageColor();
        mPath = parentPath;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mSuccess = true;
        mOnPDFCreatedInterface.onPDFCreationStarted();
    }

    private void setFilePath() {
        File folder = new File(mPath);
        if (!folder.exists())
            folder.mkdir();
        mPath = mPath + mFileName + ".pdf";
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            setFilePath();

            Log.v("stage 1", "store the pdf in sd card");

            // Ensure mPageSize is valid, default to "A4" if not
            String pageSizeName = getValidPageSizeName(mPageSize);

            Rectangle pageSize = new Rectangle(PageSize.getRectangle(pageSizeName));
            pageSize.setBackgroundColor(getBaseColor(mPageColor));
            Document document = new Document(pageSize,
                    mMarginLeft, mMarginRight, mMarginTop, mMarginBottom);
            Log.v("stage 2", "Document Created");
            document.setMargins(mMarginLeft, mMarginRight, mMarginTop, mMarginBottom);
            Rectangle documentRect = document.getPageSize();

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mPath));

            Log.v("Stage 3", "Pdf writer");

            if (mPasswordProtected) {
                writer.setEncryption(mPassword.getBytes(), String.valueOf(System.currentTimeMillis()).getBytes(),
                        PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                        PdfWriter.ENCRYPTION_AES_128);

                Log.v("Stage 3.1", "Set Encryption");
            }

            if (mWatermarkAdded) {
                WatermarkPageEvent watermarkPageEvent = new WatermarkPageEvent();
                watermarkPageEvent.setWatermark(mWatermark);
                writer.setPageEvent(watermarkPageEvent);
            }

            document.open();

            Log.v("Stage 4", "Document opened");

            for (int i = 0; i < mImagesUri.size(); i++) {
                int quality;
                quality = 30;
                if (!TextUtils.isEmpty(mQualityString)) {
                    quality = Integer.parseInt(mQualityString);
                }
                Image image = Image.getInstance(convertToUriCache(context, mImagesUri.get(i)).toString());
                // compressionLevel is a value between 0 (best speed) and 9 (best compression)
                double qualtyMod = quality * 0.09;
                image.setCompressionLevel((int) qualtyMod);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(mBorderWidth);

                Log.v("Stage 5", "Image compressed " + qualtyMod);

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(mImagesUri.get(i), bmOptions);

                Log.v("Stage 6", "Image path adding");

                float pageWidth = document.getPageSize().getWidth() - (mMarginLeft + mMarginRight);
                float pageHeight = document.getPageSize().getHeight() - (mMarginBottom + mMarginTop);
                if (mImagescaleType.equals(Config.PdfLib.IMAGE_SCALE_TYPE_ASPECT_RATIO))
                    image.scaleToFit(pageWidth, pageHeight);
                else
                    image.scaleAbsolute(pageWidth, pageHeight);

                image.setAbsolutePosition(
                        (documentRect.getWidth() - image.getScaledWidth()) / 2,
                        (documentRect.getHeight() - image.getScaledHeight()) / 2);

                Log.v("Stage 7", "Image Alignments");
                addPageNumber(documentRect, writer);
                document.add(image);

                document.newPage();
            }

            Log.v("Stage 8", "Image adding");

            document.close();

            Log.v("Stage 7", "Document Closed" + mPath);

            Log.v("Stage 8", "Record inserted in database");

        } catch (Exception e) {
            e.printStackTrace();
            mSuccess = false;
        }

        return null;
    }

    private Uri convertToUriCache(Context context, String uri) throws Exception {
        InputStream input = context.getContentResolver().openInputStream(Uri.parse(uri));
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        String realPath = PathUtils.getRealPathFromUri(context, Uri.parse(uri));
        if (realPath != null && !realPath.isEmpty()) {
            bitmap = ImageGenerator.INSTANCE.rotate(context, bitmap, Uri.fromFile(new File(realPath)));
        }
        if (input != null) {
            input.close();
        }
        File fileSave = new File(context.getCacheDir().getPath(), System.currentTimeMillis() + "");
        FileOutputStream outputStream = new FileOutputStream(fileSave);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.flush();
        outputStream.close();
        return Uri.fromFile(fileSave);
    }

    private void addPageNumber(Rectangle documentRect, PdfWriter writer) {
        if (mPageNumStyle != null) {
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_BOTTOM,
                    getPhrase(writer, mPageNumStyle, mImagesUri.size()),
                    ((documentRect.getRight() + documentRect.getLeft()) / 2),
                    documentRect.getBottom() + 25, 0);
        }
    }

    @NonNull
    private Phrase getPhrase(PdfWriter writer, String pageNumStyle, int size) {
        Phrase phrase;
        switch (pageNumStyle) {
            case Config.PdfLib.PG_NUM_STYLE_PAGE_X_OF_N:
                phrase = new Phrase(String.format("Page %d of %d", writer.getPageNumber(), size));
                break;
            case Config.PdfLib.PG_NUM_STYLE_X_OF_N:
                phrase = new Phrase(String.format("%d of %d", writer.getPageNumber(), size));
                break;
            default:
                phrase = new Phrase(String.format("%d", writer.getPageNumber()));
                break;
        }
        return phrase;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mOnPDFCreatedInterface.onPDFCreated(mSuccess, mPath);
    }

    /**
     * Read the BaseColor of passed color
     *
     * @param color value of color in int
     */
    private BaseColor getBaseColor(int color) {
        return new BaseColor(
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    /**
     * Returns a valid page size name for iText, defaults to "A4" if input is null/empty/invalid.
     */
    private String getValidPageSizeName(String pageSize) {
        try {
            // Try to get the rectangle, if fails, fallback to A4
            PageSize.getRectangle(pageSize);
            return pageSize;
        } catch (Exception e) {
            return "LETTER"; // Default to LETTER if A4 is invalid or not provided
        }
    }
}
