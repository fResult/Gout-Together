package dev.fResult.goutTogether.helpers;

import dev.fResult.goutTogether.common.ResponseAdviceHandler;
import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.common.exceptions.*;
import dev.fResult.goutTogether.common.utils.StringUtil;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHelper {
  private final Logger logger;

  /**
   * Rethrow error for the multi-thread to works with {@link ResponseAdviceHandler}'s methods.
   */
  public static RuntimeException throwMatchedException(
      Throwable cause, String defaultErrorMessage) {

    return switch (cause) {
      case ConstraintViolationException cve -> cve;
      case ValidationException ve -> ve;
      case EntityNotFoundException enfe -> enfe;
      case CredentialExistsException cee -> cee;
      case RefreshTokenExpiredException rtee -> rtee;
      default -> new RuntimeException(defaultErrorMessage, cause);
    };
  }

  public ErrorHelper(Class<?> classToLog) {
    logger = LoggerFactory.getLogger(classToLog);
  }

  public Supplier<EntityNotFoundException> entityNotFound(
      String methodName, Class<?> entityClass, Integer id) {
    return () -> {
      logger.warn("[{}] {} id: {} not found", methodName, entityClass.getSimpleName(), id);
      return new EntityNotFoundException(
          String.format("%s id [%s] not found", entityClass.getSimpleName(), id));
    };
  }

  public Supplier<EntityNotFoundException> entityWithSubResourceNotFound(
      String methodName, Class<?> entityClass, String subResourceName, String subResourceValue) {
    return () -> {
      logger.warn(
          "[{}] {} with {} id: {} not found",
          methodName,
          entityClass.getSimpleName(),
          subResourceName,
          subResourceValue);
      return new EntityNotFoundException(
          String.format(
              "%s with %s [%s] not found",
              entityClass.getSimpleName(), subResourceName, subResourceValue));
    };
  }

  public Supplier<EntityNotFoundException> someEntitiesMissing(
      String methodName, Class<?> entityClass, Collection<Integer> ids) {

    var uniqueIds = new HashSet<>(ids);
    var idsToDisplay = uniqueIds.toString().replaceAll("[\\[\\]]", "");

    return () -> {
      logger.warn(
          "[{}] {} ids: {} not found",
          methodName,
          StringUtil.pluralize(entityClass.getSimpleName()),
          idsToDisplay);
      return new EntityNotFoundException(
          String.format("%s ids [%s] not found", entityClass.getSimpleName(), idsToDisplay));
    };
  }

  public Supplier<InsufficientBalanceException> insufficientBalance(
      String transferMoney, BigDecimal userWalletBalance, BigDecimal amount) {

    return () -> {
      logger.warn(
          "[{}] {} balance: {} is insufficient for amount: {}",
          transferMoney,
          UserWallet.class.getSimpleName(),
          userWalletBalance,
          amount);

      return new InsufficientBalanceException(
          String.format(
              "%s balance is insufficient for this operation", UserWallet.class.getSimpleName()));
    };
  }

  public Supplier<InsufficientTourCountException> insufficientTourCount(
      String methodName, int amount) {

    return () -> {
      logger.warn(
          "[{}] {} is insufficient for amount: {}",
          methodName,
          TourCount.class.getSimpleName(),
          amount);
      return new InsufficientTourCountException(
          String.format(
              "%s amount is insufficient for this operation", TourCount.class.getSimpleName()));
    };
  }

  public Supplier<UnsupportedTransactionTypeException> unsupportedTransactionType(
      String methodName, TransactionType transactionType) {

    return () -> {
      logger.warn(
          "[{}] Transaction type: {} is not supported for this transferring method",
          methodName,
          transactionType);

      return new UnsupportedTransactionTypeException(
          String.format(
              "Transaction type [%s] is not supported for this transferring method",
              transactionType));
    };
  }
}
