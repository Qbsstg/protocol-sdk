package io.github.qbsstg.protocol.modbus;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModbusModelImmutabilityTest {

    private static final Class<?>[] PUBLIC_MODEL_TYPES = new Class<?>[]{
            ModbusAddressRange.class,
            ModbusBitValues.class,
            ModbusExceptionResponse.class,
            ModbusParserConfig.class,
            ModbusPdu.class,
            ModbusRawValue.class,
            ModbusRegisterValues.class,
            ModbusRequestResponseKey.class,
            ModbusSupport.class,
            ModbusTcpAdu.class,
            ModbusWriteMultipleBitsValue.class,
            ModbusWriteMultipleRegistersValue.class,
            ModbusWriteSingleValue.class
    };

    @Test
    public void publicModelClassesAreFinalAndInstanceFieldsArePrivateFinal() {
        for (int i = 0; i < PUBLIC_MODEL_TYPES.length; i++) {
            Class<?> type = PUBLIC_MODEL_TYPES[i];

            assertTrue(type.getName(), Modifier.isFinal(type.getModifiers()));

            Field[] fields = type.getDeclaredFields();
            for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
                Field field = fields[fieldIndex];
                int modifiers = field.getModifiers();
                if (field.isSynthetic()) {
                    continue;
                }
                if (Modifier.isStatic(modifiers)) {
                    assertTrue(type.getName() + "#" + field.getName(), Modifier.isFinal(modifiers));
                } else {
                    assertTrue(type.getName() + "#" + field.getName(), Modifier.isPrivate(modifiers));
                    assertTrue(type.getName() + "#" + field.getName(), Modifier.isFinal(modifiers));
                }
            }
        }
    }

    @Test
    public void tcpAduAndPduRawBytesAreDefensivelyCopied() {
        byte[] pduRaw = bytes(0x03, 0x02, 0x00, 0x01);
        ModbusPdu pdu = new ModbusPdu(0x03, new ModbusRawValue(bytes(0x02, 0x00, 0x01)), pduRaw);
        pduRaw[1] = 0x7F;

        assertArrayEquals(bytes(0x03, 0x02, 0x00, 0x01), pdu.getRawBytes());
        byte[] exportedPdu = pdu.getRawBytes();
        exportedPdu[2] = 0x55;
        assertArrayEquals(bytes(0x03, 0x02, 0x00, 0x01), pdu.getRawBytes());

        byte[] aduRaw = bytes(0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x11, 0x03, 0x02, 0x00, 0x01);
        ModbusTcpAdu adu = new ModbusTcpAdu(1, 0, 5, 0x11, pdu, aduRaw);
        aduRaw[0] = 0x7F;

        assertArrayEquals(bytes(0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x11, 0x03, 0x02, 0x00, 0x01),
                adu.getRawBytes());
        byte[] exportedAdu = adu.getRawBytes();
        exportedAdu[1] = 0x55;
        assertArrayEquals(bytes(0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x11, 0x03, 0x02, 0x00, 0x01),
                adu.getRawBytes());
    }

    @Test
    public void rawAndExceptionValuesDefensivelyCopyBytes() {
        byte[] rawPayload = bytes(0xAA, 0x55);
        ModbusRawValue rawValue = new ModbusRawValue(rawPayload);
        rawPayload[0] = 0x00;

        assertArrayEquals(bytes(0xAA, 0x55), rawValue.getRawBytes());
        byte[] exportedRawPayload = rawValue.getRawBytes();
        exportedRawPayload[1] = 0x00;
        assertArrayEquals(bytes(0xAA, 0x55), rawValue.getRawBytes());

        byte[] exceptionRaw = bytes(0x83, 0x02);
        ModbusExceptionResponse exception = new ModbusExceptionResponse(0x83, 0x02, exceptionRaw);
        exceptionRaw[1] = 0x03;

        assertArrayEquals(bytes(0x83, 0x02), exception.getRawBytes());
        byte[] exportedExceptionRaw = exception.getRawBytes();
        exportedExceptionRaw[0] = 0x00;
        assertArrayEquals(bytes(0x83, 0x02), exception.getRawBytes());
        assertEquals(ModbusExceptionCode.ILLEGAL_DATA_ADDRESS, exception.getExceptionCode());
    }

    @Test
    public void bitAndRegisterValuesDefensivelyCopyArrays() {
        boolean[] bits = new boolean[]{true, false, true};
        byte[] bitRawData = bytes(0x05);
        ModbusBitValues bitValues = new ModbusBitValues(1, bits, bitRawData);
        bits[0] = false;
        bitRawData[0] = 0x00;

        assertArrayEquals(new boolean[]{true, false, true}, bitValues.getValues());
        assertArrayEquals(bytes(0x05), bitValues.getRawData());
        boolean[] exportedBits = bitValues.getValues();
        byte[] exportedBitRawData = bitValues.getRawData();
        exportedBits[1] = true;
        exportedBitRawData[0] = 0x00;
        assertArrayEquals(new boolean[]{true, false, true}, bitValues.getValues());
        assertArrayEquals(bytes(0x05), bitValues.getRawData());

        int[] registers = new int[]{10, 258};
        byte[] registerRawData = bytes(0x00, 0x0A, 0x01, 0x02);
        ModbusRegisterValues registerValues = new ModbusRegisterValues(4, registers, registerRawData);
        registers[0] = 99;
        registerRawData[1] = 0x00;

        assertArrayEquals(new int[]{10, 258}, registerValues.getValues());
        assertArrayEquals(bytes(0x00, 0x0A, 0x01, 0x02), registerValues.getRawData());
        int[] exportedRegisters = registerValues.getValues();
        byte[] exportedRegisterRawData = registerValues.getRawData();
        exportedRegisters[1] = 99;
        exportedRegisterRawData[2] = 0x00;
        assertArrayEquals(new int[]{10, 258}, registerValues.getValues());
        assertArrayEquals(bytes(0x00, 0x0A, 0x01, 0x02), registerValues.getRawData());
    }

    @Test
    public void writeMultipleValuesDefensivelyCopyArrays() {
        boolean[] coilValues = new boolean[]{true, false, true, true};
        byte[] coilRawData = bytes(0x0D);
        ModbusWriteMultipleBitsValue coils =
                new ModbusWriteMultipleBitsValue(new ModbusAddressRange(0x0013, 4), 1, coilValues, coilRawData);
        coilValues[0] = false;
        coilRawData[0] = 0x00;

        assertArrayEquals(new boolean[]{true, false, true, true}, coils.getValues());
        assertArrayEquals(bytes(0x0D), coils.getRawData());
        boolean[] exportedCoils = coils.getValues();
        byte[] exportedCoilRawData = coils.getRawData();
        exportedCoils[2] = false;
        exportedCoilRawData[0] = 0x00;
        assertArrayEquals(new boolean[]{true, false, true, true}, coils.getValues());
        assertArrayEquals(bytes(0x0D), coils.getRawData());

        int[] registerValues = new int[]{10, 258};
        byte[] registerRawData = bytes(0x00, 0x0A, 0x01, 0x02);
        ModbusWriteMultipleRegistersValue registers =
                new ModbusWriteMultipleRegistersValue(new ModbusAddressRange(0x0001, 2),
                        4, registerValues, registerRawData);
        registerValues[0] = 99;
        registerRawData[0] = 0x7F;

        assertArrayEquals(new int[]{10, 258}, registers.getValues());
        assertArrayEquals(bytes(0x00, 0x0A, 0x01, 0x02), registers.getRawData());
        int[] exportedRegisters = registers.getValues();
        byte[] exportedRegisterRawData = registers.getRawData();
        exportedRegisters[1] = 99;
        exportedRegisterRawData[3] = 0x7F;
        assertArrayEquals(new int[]{10, 258}, registers.getValues());
        assertArrayEquals(bytes(0x00, 0x0A, 0x01, 0x02), registers.getRawData());
    }

    private static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
