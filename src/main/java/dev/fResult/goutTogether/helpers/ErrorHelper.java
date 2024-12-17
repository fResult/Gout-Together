package dev.fResult.goutTogether.helpers;

import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.exceptions.InsufficientBalanceException;
import dev.fResult.goutTogether.common.exceptions.UnsupportedTransactionTypeException;
import dev.fResult.goutTogether.common.utils.StringUtil;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHelper {
  private final Logger logger;

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
          "[{}] User wallet balance: {} is insufficient for amount: {}",
          transferMoney,
          userWalletBalance,
          amount);

      return new InsufficientBalanceException(
          "User wallet balance is insufficient for this operation");
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
