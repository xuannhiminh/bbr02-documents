package officepro.document.reader.viewer.editor.utils.createPdf;

public interface OnPDFCreatedInterface {
    void onPDFCreationStarted();
    void onPDFCreated(boolean success, String path);
}
