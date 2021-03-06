package com.pivotal.hawq.mapreduce.ft;

import com.google.common.collect.Lists;
import com.pivotal.hawq.mapreduce.HAWQTable;
import com.pivotal.hawq.mapreduce.SimpleTableClusterTester;
import com.pivotal.hawq.mapreduce.metadata.HAWQTableFormat;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test data type support for AO table.
 */
public class HAWQInputFormatFeatureTest_AO_Types extends SimpleTableClusterTester {

	@BeforeClass
	public static void setUpBeforeClass() {
		System.out.println("Executing test suite: AO_Types");
	}

	private void doSingleColumnAOTest(String columnType) throws Exception {
		String tableName = "test_ao_types_" + columnType;
		tableName = tableName.replace("(", "")
							 .replace(")", "")
							 .replace("[]", "_array");

		HAWQTable table = new HAWQTable.Builder(tableName, Lists.newArrayList(columnType))
				.storage(HAWQTableFormat.AO)
				.build();

		testSimpleTable(table);
	}

	@Test
	public void testBool() throws Exception {
		doSingleColumnAOTest("bool");
	}

	@Test
	public void testBit() throws Exception {
		doSingleColumnAOTest("bit");
	}

	@Test
	public void testBitN() throws Exception {
		doSingleColumnAOTest("bit(5)");
	}

	@Test
	public void testVarbit() throws Exception {
		doSingleColumnAOTest("varbit");
	}

	@Test
	public void testByteArray() throws Exception {
		doSingleColumnAOTest("bytea");
	}

	@Test
	public void testInt2() throws Exception {
		doSingleColumnAOTest("int2");
	}

	@Test
	public void testInt4() throws Exception {
		doSingleColumnAOTest("int4");
	}

	@Test
	public void testInt8() throws Exception {
		doSingleColumnAOTest("int8");
	}

	@Test
	public void testFloat4() throws Exception {
		doSingleColumnAOTest("float4");
	}

	@Test
	public void testFloat8() throws Exception {
		doSingleColumnAOTest("float8");
	}

	@Test
	public void testNumeric() throws Exception {
		doSingleColumnAOTest("numeric");
	}

	@Test
	public void testCharN() throws Exception {
		doSingleColumnAOTest("char(10)");
	}

	@Test
	public void testVarcharN() throws Exception {
		doSingleColumnAOTest("varchar(10)");
	}

	@Test
	public void testText() throws Exception {
		doSingleColumnAOTest("text");
	}

	@Test
	public void testDate() throws Exception {
		doSingleColumnAOTest("date");
	}

	@Test
	public void testTime() throws Exception {
		doSingleColumnAOTest("time");
	}

	@Test
	public void testTimetz() throws Exception {
		doSingleColumnAOTest("timetz");
	}

	@Test
	public void testTimestamp() throws Exception {
		doSingleColumnAOTest("timestamp");
	}

	@Test
	public void testTimestamptz() throws Exception {
		doSingleColumnAOTest("timestamptz");
	}

	@Test
	public void testInterval() throws Exception {
		doSingleColumnAOTest("interval");
	}

	@Test
	public void testPoint() throws Exception {
		doSingleColumnAOTest("point");
	}

	@Test
	public void testLseg() throws Exception {
		doSingleColumnAOTest("lseg");
	}

	@Test
	public void testBox() throws Exception {
		doSingleColumnAOTest("box");
	}

	@Test
	public void testCircle() throws Exception {
		doSingleColumnAOTest("circle");
	}

	@Test
	public void testPath() throws Exception {
		doSingleColumnAOTest("path");
	}

	@Test
	public void testPolygon() throws Exception {
		doSingleColumnAOTest("polygon");
	}

	@Test
	public void testMacaddr() throws Exception {
		doSingleColumnAOTest("macaddr");
	}

	@Test
	public void testInet() throws Exception {
		doSingleColumnAOTest("inet");
	}

	@Test
	public void testCidr() throws Exception {
		doSingleColumnAOTest("cidr");
	}

	@Test
	public void testXml() throws Exception {
		doSingleColumnAOTest("xml");
	}

	@Test
	public void testArray() throws Exception {
		doSingleColumnAOTest("int4[]");
	}
}
