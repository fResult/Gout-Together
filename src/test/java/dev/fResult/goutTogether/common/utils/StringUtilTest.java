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
      var expectedOutput2 = "";

      // Act
      var result1 = StringUtil.pluralize(null);
      var result2 = StringUtil.pluralize("");

      // Assert
      assertEquals(expectedOutput1, result1);
      assertEquals(expectedOutput2, result2);
    }

    @Test
    void whenInputEndsWithY_thenReturnInputWithoutYAndWithIes() {
      // Arrange
      var input1 = "party";
      var expectedOutput1 = "parties";
      var input2 = "Ally";
      var expectedOutput2 = "Allies";

      // Act
      var result1 = StringUtil.pluralize(input1);
      var result2 = StringUtil.pluralize(input2);

      // Assert
      assertEquals(expectedOutput1, result1);
      assertEquals(expectedOutput2, result2);
    }

    @Test
    void whenInputEndsWithSOrShOrChOrXOrZ_thenReturnInputWithEs() {
      // Arrange
      var input1 = "bus";
      var expectedOutput1 = "buses";
      var input2 = "brush";
      var expectedOutput2 = "brushes";
      var input3 = "church";
      var expectedOutput3 = "churches";
      var input4 = "Box";
      var expectedOutput4 = "Boxes";
      var input5 = "Buzz";
      var expectedOutput5 = "Buzzes";

      // Act
      var result1 = StringUtil.pluralize(input1);
      var result2 = StringUtil.pluralize(input2);
      var result3 = StringUtil.pluralize(input3);
      var result4 = StringUtil.pluralize(input4);
      var result5 = StringUtil.pluralize(input5);

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
      var input1 = "car";
      var expectedOutput1 = "cars";
      var input2 = "Apple";
      var expectedOutput2 = "Apples";
      var input3 = "dog";
      var expectedOutput3 = "dogs";

      // Act
      var result1 = StringUtil.pluralize(input1);
      var result2 = StringUtil.pluralize(input2);
      var result3 = StringUtil.pluralize(input3);

      // Assert
      assertEquals(expectedOutput1, result1);
      assertEquals(expectedOutput2, result2);
      assertEquals(expectedOutput3, result3);
    }
  }
}
