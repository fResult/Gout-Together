package dev.fResult.goutTogether.qrcodes;

import dev.fResult.goutTogether.common.enumurations.QrStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("qr_code_references")
public record QrCodeReference(@Id Integer id, String content, QrStatus status) {
  public static QrCodeReference of(Integer id, String content, QrStatus status) {
    return new QrCodeReference(id, content, status);
  }
}
