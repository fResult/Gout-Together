package dev.fResult.goutTogether.qrcodes;

import static dev.fResult.goutTogether.common.Constants.API_PAYMENT_PATH;

import com.google.zxing.WriterException;
import dev.fResult.goutTogether.common.enumurations.QrCodeStatus;
import dev.fResult.goutTogether.common.helpers.QrCodeHelper;
import dev.fResult.goutTogether.common.helpers.ErrorHelper;
import java.awt.image.BufferedImage;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {
  private final Logger logger = LoggerFactory.getLogger(QrCodeService.class);
  private final ErrorHelper errorHelper = new ErrorHelper(QrCodeService.class);

  private final QrCodeReferenceRepository qrCodeReferenceRepository;

  public QrCodeService(QrCodeReferenceRepository qrCodeReferenceRepository) {
    this.qrCodeReferenceRepository = qrCodeReferenceRepository;
  }

  public BufferedImage generateQrCodeImageById(int id) throws WriterException {
    logger.debug(
        "[generateQrById] New {} is generating by id [{}]",
        QrCodeReference.class.getSimpleName(),
        id);

    var qrCodeRef =
        qrCodeReferenceRepository
            .findById(id)
            .orElseThrow(errorHelper.entityNotFound("generateQrById", QrCodeReference.class, id));

    return QrCodeHelper.generateQrCodeImage(qrCodeRef.content());
  }

  public QrCodeReference getQrCodeRefByBookingId(Integer bookingId) {
    return findQrCodeRefByBookingId(bookingId)
        .orElseThrow(
            errorHelper.entityWithSubResourceNotFound(
                "getQrCodeRefByBookingId",
                QrCodeReference.class,
                "bookingId",
                String.valueOf(bookingId)));
  }

  public QrCodeReference createQrCodeRefForBooking(int bookingId) {
    var qrCodeRefOpt = findQrCodeRefByBookingId(bookingId);
    if (qrCodeRefOpt.isPresent()) {
      logger.info(
          "[createQrCodeRefForBooking] {} for bookingId [{}] exists, return the existing one",
          QrCodeReference.class.getSimpleName(),
          bookingId);

      return qrCodeRefOpt.get();
    }

    logger.debug(
        "[createQrCodeRefForBooking] New {} is creating by bookingId [{}]",
        QrCodeReference.class.getSimpleName(),
        bookingId);

    var paymentApiPath = String.format("%s/%d", API_PAYMENT_PATH, bookingId);
    var qrCodeToCreate =
        QrCodeReference.of(null, bookingId, paymentApiPath, QrCodeStatus.ACTIVATED);

    var createdQrCode = qrCodeReferenceRepository.save(qrCodeToCreate);
    logger.info(
        "[createQrCodeRefForBooking] New {} is created: {}",
        QrCodeReference.class.getSimpleName(),
        createdQrCode);

    return createdQrCode;
  }

  public QrCodeReference updateQrCodeRefStatusByBookingId(
      int bookingId, QrCodeStatus statusToUpdate) {

    logger.debug(
        "[updateQrCodeRefStatusByBookingId] {} by bookingId [{}] is updating",
        QrCodeReference.class.getSimpleName(),
        bookingId);

    var qrCodeRef = getQrCodeRefByBookingId(bookingId);
    var qrCodeToUpdate =
        QrCodeReference.of(
            qrCodeRef.id(), qrCodeRef.bookingId(), qrCodeRef.content(), statusToUpdate);

    var updatedQrCodeRef = qrCodeReferenceRepository.save(qrCodeToUpdate);
    logger.info(
        "[updateQrCodeRefStatusByBookingId] {} bookingId [{}] is updated: {}",
        QrCodeReference.class.getSimpleName(),
        bookingId,
        updatedQrCodeRef);

    return updatedQrCodeRef;
  }

  public boolean deleteQrCodeRefByBookingId(int bookingId) {
    logger.debug(
        "[deleteQrCodeRefByBookingId] {} by bookingId [{}] is deleting",
        QrCodeReference.class.getSimpleName(),
        bookingId);

    var qrCodeRef = getQrCodeRefByBookingId(bookingId);
    qrCodeReferenceRepository.delete(qrCodeRef);

    logger.info(
        "[deleteQrCodeRefByBookingId] {} by bookingId [{}] is deleted",
        QrCodeReference.class.getSimpleName(),
        bookingId);

    return true;
  }

  private Optional<QrCodeReference> findQrCodeRefByBookingId(Integer bookingId) {
    logger.debug(
        "[findQrByBookingId] Finding {} by bookingId [{}]",
        QrCodeReference.class.getSimpleName(),
        bookingId);

    return qrCodeReferenceRepository.findOneByBookingId(bookingId);
  }
}
