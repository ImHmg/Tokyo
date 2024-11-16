package io.github.imhmg.tokyo.commons;

import io.github.imhmg.tokyo.commons.assertions.Operator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionParserTest {

    @Test
    public void parseHeader_1() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@header Authorization");
        assertEquals("@header", result.getSource());
        assertEquals("Authorization", result.getKey());
    }

    @Test
    public void parseHeader_2() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@header Spaced Header");
        assertEquals("@header", result.getSource());
        assertEquals("Spaced Header", result.getKey());
    }

    @Test
    public void parseHeader_3() {
        assertThrows(Exception.class, () -> {
            ExpressionParser.parseExpression("@headerSpaced Header");
        });
    }

    @Test
    public void parseHeader_4() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@header Authorization [==] Value");
        assertEquals("@header", result.getSource());
        assertEquals("Authorization", result.getKey());
        assertEquals(Operator.EQ, result.getOperator());
        assertEquals("Value", result.getExpectedValue());
    }

    @Test
    public void parseHeader_5() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@header Spaced Header [==] Spaced Value");
        assertEquals("@header", result.getSource());
        assertEquals("Spaced Header", result.getKey());
        assertEquals(Operator.EQ, result.getOperator());
        assertEquals("Spaced Value", result.getExpectedValue());
    }

    @Test
    public void parseHeader_6() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@header Spaced Header [<<<] Value");
        assertEquals("@header", result.getSource());
        assertEquals("Spaced Header [<<<] Value", result.getKey());
    }

    @Test
    public void parseHeader_7() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@header Spaced Header [==] Spaced Value [==] Some Value");
        assertEquals("@header", result.getSource());
        assertEquals("Spaced Header", result.getKey());
        assertEquals(Operator.EQ, result.getOperator());
        assertEquals("Spaced Value [==] Some Value", result.getExpectedValue());
    }

    @Test
    public void parseStatus_1() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@status");
        assertEquals("@status", result.getSource());
    }

    @Test
    public void parseStatus_2() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@status [==] 200");
        assertEquals("@status", result.getSource());
        assertEquals(Operator.EQ, result.getOperator());
        assertEquals("200", result.getExpectedValue());
    }

    @Test
    public void parseStatus_3() {
        assertThrows(Exception.class, () -> {
            ExpressionParser.parseExpression("@status Some value");
        });
    }

    @Test
    public void parseStatus_4() {
        assertThrows(Exception.class, () -> {
            ExpressionParser.parseExpression("@statusSome Value");
        });
    }

    @Test
    public void parseBody_1() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@body raw [==] Some Value");
        assertEquals("@body", result.getSource());
        assertEquals("raw", result.getType());
        assertEquals(Operator.EQ, result.getOperator());
        assertEquals("Some Value", result.getExpectedValue());
    }

    @Test
    public void parseBody_2() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@body raw");
        assertEquals("@body", result.getSource());
        assertEquals("raw", result.getType());
    }

    @Test
    public void parseBody_3() {
        assertThrows(Exception.class, () -> {
            ExpressionParser.parseExpression("@body raw some value");
        });
    }

    @Test
    public void parseBody_4() {
        assertThrows(Exception.class, () -> {
            ExpressionParser.parseExpression("@body rawsome value");
        });
    }

    @Test
    public void parseBody_5() {
        assertThrows(Exception.class, () -> {
            ExpressionParser.parseExpression("@body jsonsome value");
        });
    }

    @Test
    public void parseBody_6() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@body json.$.id [==] Some Value");
        assertEquals("@body", result.getSource());
        assertEquals("json", result.getType());
        assertEquals("$.id", result.getKey());
        assertEquals(Operator.EQ, result.getOperator());
        assertEquals("Some Value", result.getExpectedValue());
    }


    @Test
    public void parseBody_7() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@body json.$.id=='some' [==] Some Value");
        assertEquals("@body", result.getSource());
        assertEquals("json", result.getType());
        assertEquals("$.id=='some'", result.getKey());
        assertEquals(Operator.EQ, result.getOperator());
        assertEquals("Some Value", result.getExpectedValue());
    }

    @Test
    public void parseBody_8() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@body xml.$.id=='some' [==] Some Value");
        assertEquals("@body", result.getSource());
        assertEquals("xml", result.getType());
        assertEquals("$.id=='some'", result.getKey());
        assertEquals(Operator.EQ, result.getOperator());
        assertEquals("Some Value", result.getExpectedValue());
    }

    @Test
    public void parseBody_9() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@body xml.$.id=='some'");
        assertEquals("@body", result.getSource());
        assertEquals("xml", result.getType());
        assertEquals("$.id=='some'", result.getKey());
    }

    @Test
    public void parseBody_10() {
        assertThrows(Exception.class, () -> {
            ExpressionParser.parseExpression("@body xml:$.id=='some'");
        });
    }

    @Test
    public void parseBody_11() {
        ExpressionParser.Result result = ExpressionParser.parseExpression("@body xml.$.id=='some' [<<>>] Some");
        assertEquals("@body", result.getSource());
        assertEquals("xml", result.getType());
        assertEquals("$.id=='some' [<<>>] Some", result.getKey());
    }

}