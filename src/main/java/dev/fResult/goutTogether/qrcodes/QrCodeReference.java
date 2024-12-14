package dev.fResult.goutTogether.qrcodes;

import dev.fResult.goutTogether.common.enumurations.QrCodeStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("qr_code_references")
public record QrCodeReference(@Id Integer id, Integer bookingId, String content, QrCodeStatus status) {
  public static QrCodeReference of(Integer id, Integer bookingId, String content, QrCodeStatus status) {
    return new QrCodeReference(id, bookingId, content, status);
  }
}
