package dev.fResult.goutTogether.common.helpers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;

public class QrCodeHelper {
  public static BufferedImage generateQrCodeImage(String barcodeText) throws WriterException {
    final var qrCodeWriter = new QRCodeWriter();
    final var bitMatrix = qrCodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 300, 300);

    return MatrixToImageWriter.toBufferedImage(bitMatrix);
  }
}
