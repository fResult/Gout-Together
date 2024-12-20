package dev.fResult.goutTogether.payments.services;

import com.google.zxing.WriterException;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;
import dev.fResult.goutTogether.bookings.services.BookingService;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.enumurations.QrCodeStatus;
import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.qrcodes.QrCodeService;
import dev.fResult.goutTogether.tours.services.TourCountService;
import dev.fResult.goutTogether.transactions.Transaction;
import dev.fResult.goutTogether.transactions.TransactionHelper;
import dev.fResult.goutTogether.transactions.TransactionService;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {
  private final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
  private final ErrorHelper errorHelper = new ErrorHelper(PaymentServiceImpl.class);

  private final double tourPrice;
  private final BookingRepository bookingRepository;

  private final QrCodeService qrCodeService;
  private final BookingService bookingService;
  private final WalletService walletService;
  private final TourCountService tourCountService;
  private final TransactionService transactionService;

  public PaymentServiceImpl(
      @Value("${booking.tour-price}") double tourPrice,
      BookingRepository bookingRepository,
      QrCodeService qrCodeService,
      @Lazy BookingService bookingService,
      @Lazy WalletService walletService,
      TourCountService tourCountService,
      TransactionService transactionService) {
    this.qrCodeService = qrCodeService;
    this.bookingService = bookingService;
    this.walletService = walletService;

    this.tourPrice = tourPrice;
    this.bookingRepository = bookingRepository;
    this.tourCountService = tourCountService;
    this.transactionService = transactionService;
  }

  @Override
  public BufferedImage generatePaymentQr(int id) throws WriterException {
    return qrCodeService.generateQrCodeImageById(id);
  }

  // TODO: Handle Idempotent Key
  @Override
  @Transactional
  public BookingInfoResponse payByBookingId(int bookingId, String idempotentKey) {
    var booking =
        bookingService
            .findBookingById(bookingId)
            .orElseThrow(errorHelper.entityNotFound("payByBookingId", Booking.class, bookingId));

    var wallets = walletService.getConsumerAndTourCompanyWallets(booking);
    var userWallet = wallets.getFirst();
    var tourCompanyWallet = wallets.getSecond();

    walletService.transferMoney(
        userWallet, tourCompanyWallet, BigDecimal.valueOf(tourPrice), TransactionType.BOOKING);
    tourCountService.incrementTourCount(Objects.requireNonNull(booking.tourId().getId()));
    qrCodeService.updateQrCodeRefStatusByBookingId(bookingId, QrCodeStatus.EXPIRED);

    var transactionToCreate =
        TransactionHelper.buildBookingTransaction(
            idempotentKey,
            userWallet.userId().getId(),
            bookingId,
            tourCompanyWallet.tourCompanyId().getId(),
            BigDecimal.valueOf(tourPrice));

    var createdTransaction = transactionService.createTransaction(transactionToCreate);

    logger.info(
        "[payByBookingId] Created {} id [{}]",
        Transaction.class.getSimpleName(),
        createdTransaction.id());

    var bookingToBeCompleted =
        Booking.of(
            bookingId,
            booking.userId(),
            booking.tourId(),
            BookingStatus.COMPLETED.name(),
            booking.bookingDate(),
            Instant.now(),
            idempotentKey);

    var completedBooking = bookingRepository.save(bookingToBeCompleted);

    return BookingInfoResponse.fromDao(completedBooking);
  }

  @Override
  @Transactional
  public boolean refundBooking(Booking booking, String idempotentKey) {
    var wallets = walletService.getConsumerAndTourCompanyWallets(booking);
    var userWallet = wallets.getFirst();
    var tourCompanyWallet = wallets.getSecond();

    walletService.transferMoney(
        userWallet, tourCompanyWallet, BigDecimal.valueOf(tourPrice), TransactionType.REFUND);
    qrCodeService.deleteQrCodeRefByBookingId(booking.id());

    var transactionToRefund =
        TransactionHelper.buildRefundTransaction(
            idempotentKey,
            userWallet.userId().getId(),
            booking.id(),
            tourCompanyWallet.tourCompanyId().getId(),
            BigDecimal.valueOf(tourPrice));

    var refundedTransaction = transactionService.createTransaction(transactionToRefund);

    logger.info(
        "[refundBookingByBookingId] Refunded {} id [{}]",
        Transaction.class.getSimpleName(),
        refundedTransaction.id());

    return true;
  }
}
