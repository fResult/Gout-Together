package dev.fResult.goutTogether.qrcodes;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface QrCodeReferenceRepository extends ListCrudRepository<QrCodeReference, Integer> {
  Optional<QrCodeReference> findOneByBookingId(int bookingId);
}
