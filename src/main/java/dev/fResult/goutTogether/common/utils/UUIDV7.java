package dev.fResult.goutTogether.common.utils;

import java.util.Random;
import java.util.UUID;

public class UUIDV7 {
  public static UUID randomUUID() {
    long timestamp = System.currentTimeMillis();
    long randomBits = new Random().nextLong();

    long mostSigBits =
        (timestamp << 32) | ((timestamp & 0xFFFF00000000L) >> 16) | 0x0000000000007000L;
    long leastSigBits = (randomBits & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

    return new UUID(mostSigBits, leastSigBits);
  }
}
