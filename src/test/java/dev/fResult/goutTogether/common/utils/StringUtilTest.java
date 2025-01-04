package dev.fResult.goutTogether.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StringUtilTest {
  @Nested
  class PluralizeTest {
    @Test
    void whenInputIsNullOrEmpty_thenReturnInputItself() {
      // Arrange
      String expectedOutput1 = null;
      final var expectedOutput2 = "";

      // Act
      final var result1 = StringUtil.pluralize(null);
      final var result2 = StringUtil.pluralize("");

      // Assert
      assertEquals(expectedOutput1, result1);
      assertEquals(expectedOutput2, result2);
    }

    @Test
    void whenInputEndsWithY_thenReturnInputWithoutYAndWithIes() {
      // Arrange
      final var input1 = "party";
      final var expectedOutput1 = "parties";
      final var input2 = "Ally";
      final var expectedOutput2 = "Allies";

      // Act
      final var result1 = StringUtil.pluralize(input1);
      final var result2 = StringUtil.pluralize(input2);

      // Assert
      assertEquals(expectedOutput1, result1);
      assertEquals(expectedOutput2, result2);
    }

    @Test
    void whenInputEndsWithSOrShOrChOrXOrZ_thenReturnInputWithEs() {
      // Arrange
      final var input1 = "bus";
      final var expectedOutput1 = "buses";
      final var input2 = "brush";
      final var expectedOutput2 = "brushes";
      final var input3 = "church";
      final var expectedOutput3 = "churches";
      final var input4 = "Box";
      final var expectedOutput4 = "Boxes";
      final var input5 = "Buzz";
      final var expectedOutput5 = "Buzzes";

      // Act
      final var result1 = StringUtil.pluralize(input1);
      final var result2 = StringUtil.pluralize(input2);
      final var result3 = StringUtil.pluralize(input3);
      final var result4 = StringUtil.pluralize(input4);
      final var result5 = StringUtil.pluralize(input5);

      // Assert
      assertEquals(expectedOutput1, result1);
      assertEquals(expectedOutput2, result2);
      assertEquals(expectedOutput3, result3);
      assertEquals(expectedOutput4, result4);
      assertEquals(expectedOutput5, result5);
    }

    @Test
    void whenInput_thenAddS() {
      // Arrange
      final var input1 = "car";
      final var expectedOutput1 = "cars";
      final var input2 = "Apple";
      final var expectedOutput2 = "Apples";
      final var input3 = "dog";
      final var expectedOutput3 = "dogs";

      // Act
      final var result1 = StringUtil.pluralize(input1);
      final var result2 = StringUtil.pluralize(input2);
      final var result3 = StringUtil.pluralize(input3);

      // Assert
      assertEquals(expectedOutput1, result1);
      assertEquals(expectedOutput2, result2);
      assertEquals(expectedOutput3, result3);
    }
  }
}
