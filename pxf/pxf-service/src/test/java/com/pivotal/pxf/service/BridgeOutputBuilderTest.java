package com.pivotal.pxf.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.pivotal.pxf.api.BadRecordException;
import com.pivotal.pxf.api.OneField;
import com.pivotal.pxf.api.io.DataType;
import com.pivotal.pxf.service.io.GPDBWritable;
import com.pivotal.pxf.service.utilities.ProtocolData;

public class BridgeOutputBuilderTest {

    private static final int UN_SUPPORTED_TYPE = -1;
    private GPDBWritable output = null;

    @Test
    public void testFillGPDBWritable() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("X-GP-ATTRS", "14");

        addColumn(parameters, 0, DataType.INTEGER,   "col0");
        addColumn(parameters, 1, DataType.FLOAT8,    "col1");
        addColumn(parameters, 2, DataType.REAL,      "col2");
        addColumn(parameters, 3, DataType.BIGINT,    "col3");
        addColumn(parameters, 4, DataType.SMALLINT,  "col4");
        addColumn(parameters, 5, DataType.BOOLEAN,   "col5");
        addColumn(parameters, 6, DataType.BYTEA,     "col6");
        addColumn(parameters, 7, DataType.VARCHAR,   "col7");
        addColumn(parameters, 8, DataType.BPCHAR,    "col8");
        addColumn(parameters, 9, DataType.CHAR,      "col9");
        addColumn(parameters, 10, DataType.TEXT,     "col10");
        addColumn(parameters, 11, DataType.NUMERIC,  "col11");
        addColumn(parameters, 12, DataType.TIMESTAMP,"col12");
        addColumn(parameters, 13, DataType.DATE,     "col13");


        BridgeOutputBuilder builder = makeBuilder(parameters);
        output = builder.makeGPDBWritableOutput();

        List<OneField> recFields = Arrays.asList(new OneField(DataType.INTEGER.getOID(), 0),
                new OneField(DataType.FLOAT8.getOID(), (double) 0),
                new OneField(DataType.REAL.getOID(), (float) 0),
                new OneField(DataType.BIGINT.getOID(), (long) 0),
                new OneField(DataType.SMALLINT.getOID(), (short) 0),
                new OneField(DataType.BOOLEAN.getOID(), true),
                new OneField(DataType.BYTEA.getOID(), new byte[]{0}),
                new OneField(DataType.VARCHAR.getOID(), "value"),
                new OneField(DataType.BPCHAR.getOID(), "value"),
                new OneField(DataType.CHAR.getOID(), "value"),
                new OneField(DataType.TEXT.getOID(), "value"),
                new OneField(DataType.NUMERIC.getOID(), "0"),
                new OneField(DataType.TIMESTAMP.getOID(), new Timestamp(0)),
                new OneField(DataType.DATE.getOID(), new Date(1)));
        builder.fillGPDBWritable(recFields);

        assertEquals(output.getInt(0), Integer.valueOf(0));
        assertEquals(output.getDouble(1), Double.valueOf(0));
        assertEquals(output.getFloat(2), Float.valueOf(0));
        assertEquals(output.getLong(3), Long.valueOf(0));
        assertEquals(output.getShort(4), Short.valueOf((short) 0));
        assertEquals(output.getBoolean(5), true);
        assertArrayEquals(output.getBytes(6), new byte[]{0});
        assertEquals(output.getString(7), "value\0");
        assertEquals(output.getString(8), "value\0");
        assertEquals(output.getString(9), "value\0");
        assertEquals(output.getString(10), "value\0");
        assertEquals(output.getString(11), "0\0");
        assertEquals(Timestamp.valueOf(output.getString(12)), new Timestamp(0));
        assertEquals(Date.valueOf(output.getString(13).trim()).toString(), new Date(1).toString());
    }

    @Test
    public void testFillOneGPDBWritableField() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("X-GP-ATTRS", "1");    	
        addColumn(parameters, 0, DataType.INTEGER, "col0");    	
        BridgeOutputBuilder builder = makeBuilder(parameters);
        output = builder.makeGPDBWritableOutput();

        OneField unSupportedField = new OneField(UN_SUPPORTED_TYPE, new Byte((byte) 0));
        try {
            builder.fillOneGPDBWritableField(unSupportedField, 0);
            fail("Unsupported data type should throw exception");
        } catch (UnsupportedOperationException e) {
            assertEquals(e.getMessage(), "Byte is not supported for HAWQ conversion");
        }
    }

    @Test
    public void testRecordSmallerThanSchema() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("X-GP-ATTRS", "4");

        addColumn(parameters, 0, DataType.INTEGER, "col0");
        addColumn(parameters, 1, DataType.INTEGER, "col1");
        addColumn(parameters, 2, DataType.INTEGER, "col2");
        addColumn(parameters, 3, DataType.INTEGER, "col3");

        BridgeOutputBuilder builder = makeBuilder(parameters);
        output = builder.makeGPDBWritableOutput();

        /* all four fields */
        List<OneField> complete = Arrays.asList(
                new OneField(DataType.INTEGER.getOID(), 10),
                new OneField(DataType.INTEGER.getOID(), 20),
                new OneField(DataType.INTEGER.getOID(), 30),
                new OneField(DataType.INTEGER.getOID(), 40));
        builder.fillGPDBWritable(complete);
        assertEquals(output.getColType().length, 4);
        assertEquals(output.getInt(0), Integer.valueOf(10));
        assertEquals(output.getInt(1), Integer.valueOf(20));
        assertEquals(output.getInt(2), Integer.valueOf(30));
        assertEquals(output.getInt(3), Integer.valueOf(40));

        /* two fields instead of four */
        List<OneField> incomplete = Arrays.asList(
        		new OneField(DataType.INTEGER.getOID(), 10),
    			new OneField(DataType.INTEGER.getOID(), 20));
        try {
            builder.fillGPDBWritable(incomplete);
            fail("testRecordBiggerThanSchema should have failed on - Record has 2 fields but the schema size is 4");
        } catch (BadRecordException e) {
            assertEquals(e.getMessage(), "Record has 2 fields but the schema size is 4");
        }
    }

    @Test
    public void testRecordBiggerThanSchema() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("X-GP-ATTRS", "4");

        addColumn(parameters, 0, DataType.INTEGER, "col0");
        addColumn(parameters, 1, DataType.INTEGER, "col1");
        addColumn(parameters, 2, DataType.INTEGER, "col2");
        addColumn(parameters, 3, DataType.INTEGER, "col3");

        BridgeOutputBuilder builder = makeBuilder(parameters);
        output = builder.makeGPDBWritableOutput();

        /* five fields instead of four */
        List<OneField> complete = Arrays.asList(
                new OneField(DataType.INTEGER.getOID(), 10),
                new OneField(DataType.INTEGER.getOID(), 20),
                new OneField(DataType.INTEGER.getOID(), 30),
                new OneField(DataType.INTEGER.getOID(), 40),
                new OneField(DataType.INTEGER.getOID(), 50));
        try {
            builder.fillGPDBWritable(complete);
            fail("testRecordBiggerThanSchema should have failed on - Record has 5 fields but the schema size is 4");
        } catch (BadRecordException e) {
            assertEquals(e.getMessage(), "Record has 5 fields but the schema size is 4");
        }
    }

    @Test
    public void testFieldTypeMismatch() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("X-GP-ATTRS", "4");

        addColumn(parameters, 0, DataType.INTEGER, "col0");
        addColumn(parameters, 1, DataType.INTEGER, "col1");
        addColumn(parameters, 2, DataType.INTEGER, "col2");
        addColumn(parameters, 3, DataType.INTEGER, "col3");

        BridgeOutputBuilder builder = makeBuilder(parameters);
        output = builder.makeGPDBWritableOutput();

        /* last field is REAL while schema requires INT */
        List<OneField> complete = Arrays.asList(
                new OneField(DataType.INTEGER.getOID(), 10),
                new OneField(DataType.INTEGER.getOID(), 20),
                new OneField(DataType.INTEGER.getOID(), 30),
                new OneField(DataType.REAL.getOID(), 40.0));
        try {
            builder.fillGPDBWritable(complete);
            fail("testFieldTypeMismatch should have failed on - For field 3 schema requires type INTEGER but input record has type REAL");
        } catch (BadRecordException e) {
            assertEquals(e.getMessage(), "For field col3 schema requires type INTEGER but input record has type REAL");
        }
    }   

    private void addColumn(Map<String, String> parameters, int idx, DataType dataType, String name) {
        parameters.put("X-GP-ATTR-NAME" + idx, name);
        parameters.put("X-GP-ATTR-TYPECODE" + idx, Integer.toString(dataType.getOID()));
        parameters.put("X-GP-ATTR-TYPENAME" + idx, dataType.toString());
    }

    private BridgeOutputBuilder makeBuilder(Map<String, String> parameters) throws Exception {

        parameters.put("X-GP-ALIGNMENT", "8");
        parameters.put("X-GP-SEGMENT-ID", "-44");
        parameters.put("X-GP-SEGMENT-COUNT", "2");
        parameters.put("X-GP-HAS-FILTER", "0");
        parameters.put("X-GP-FORMAT", "TEXT");
        parameters.put("X-GP-URL-HOST", "my://bags");
        parameters.put("X-GP-URL-PORT", "-8020");
        parameters.put("X-GP-ACCESSOR", "are");
        parameters.put("X-GP-RESOLVER", "packed");
        parameters.put("X-GP-DATA-DIR", "i'm/ready/to/go");
        parameters.put("X-GP-FRAGMENT-METADATA", "U29tZXRoaW5nIGluIHRoZSB3YXk=");
        parameters.put("X-GP-I'M-STANDING-HERE", "outside-your-door");

        ProtocolData protocolData = new ProtocolData(parameters);
        BridgeOutputBuilder builder = new BridgeOutputBuilder(protocolData);
        return builder;
    }
}
