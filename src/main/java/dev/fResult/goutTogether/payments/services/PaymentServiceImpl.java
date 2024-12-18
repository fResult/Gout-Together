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
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
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
      BookingService bookingService,
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

    // TODO: Handle existing Transaction By Idempotent Key (to avoid to create new Transaction)
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var futureTransferredMoney =
          CompletableFuture.runAsync(
              buildMoneyTransferRunnable(userWallet, tourCompanyWallet), executor);

      var futureCreatedTransaction =
          CompletableFuture.supplyAsync(
              buildTransactionCreationSupplier(idempotentKey, userWallet, tourCompanyWallet),
              executor);

      var futureIncrementedTourCount =
          CompletableFuture.runAsync(
              () ->
                  tourCountService.incrementTourCount(
                      Objects.requireNonNull(booking.tourId().getId())),
              executor);
      var futureExpiredQrCodeReference =
          CompletableFuture.supplyAsync(
              () -> qrCodeService.updateQrCodeRefStatusByBookingId(bookingId, QrCodeStatus.EXPIRED),
              executor);

      var futureCompletedBooking =
          CompletableFuture.supplyAsync(
              () -> {
                var bookingToBeComplete =
                    Booking.of(
                        bookingId,
                        booking.userId(),
                        booking.tourId(),
                        BookingStatus.COMPLETED.name(),
                        booking.bookingDate(),
                        Instant.now(),
                        idempotentKey);

                return bookingRepository.save(bookingToBeComplete);
              },
              executor);

      var allFutures =
          CompletableFuture.allOf(
              futureTransferredMoney,
              futureCreatedTransaction,
              futureIncrementedTourCount,
              futureExpiredQrCodeReference,
              futureCompletedBooking);
      allFutures.join();

      var expiredQrCodeReference = futureExpiredQrCodeReference.get();
      var completedBooking = futureCompletedBooking.get();

      return BookingInfoResponse.fromDao(completedBooking)
          .withQrReference(expiredQrCodeReference.id());
    } catch (ExecutionException | InterruptedException ex) {
      throw new RuntimeException("Failed to pay on booking", ex);
    }
  }

  @Override
  @Transactional
  public boolean refundBookingByBookingId(int bookingId, String idempotentKey) {
    throw new UnsupportedOperationException("Not Implement Yet");
  }

  @NotNull
  private Supplier<Transaction> buildTransactionCreationSupplier(
      String idempotentKey, UserWallet userWallet, TourCompanyWallet tourCompanyWallet) {
    return () -> {
      var newTransaction =
          TransactionHelper.buildBookingTransaction(
              idempotentKey,
              userWallet.userId().getId(),
              tourCompanyWallet.tourCompanyId().getId(),
              BigDecimal.valueOf(tourPrice));

      return transactionService.createTransaction(newTransaction);
    };
  }

  @NotNull
  private Runnable buildMoneyTransferRunnable(
      UserWallet userWallet, TourCompanyWallet tourCompanyWallet) {
    return () ->
        walletService.transferMoney(
            userWallet, tourCompanyWallet, BigDecimal.valueOf(tourPrice), TransactionType.BOOKING);
  }
}
