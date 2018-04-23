package org.jaxws.wsdl2bytecodes.service;

import static org.apache.commons.lang3.ClassUtils.getPackageName;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jaxws.util.lang.RandomStringUtils;
import org.jaxws.util.os.SystemProcessException;
import org.jaxws.util.os.SystemProcessUtils;
import org.jaxws.wsdl2bytecodes.model.ByteCodePackage;

/**
 * 
 * @author chenjianjx
 * 
 */
public class Wsdl2ByteCodes {

	public static ByteCodePackage generate(String byteCodesDirParent, String wsdlUrl, boolean useOrigPkg) throws WsdlImportException {
		String currentTime = formatDate(new Date(), "yyyyMMddHHmmssSSS");
		File byteCodeDir = createByteCodesDir(byteCodesDirParent, currentTime);
		String packageName = null;
		if (!useOrigPkg) {
			packageName = generatePakcageName(currentTime);
		}

		byteCodeDir.mkdirs();
		doWsImport(byteCodeDir.getAbsolutePath(), wsdlUrl, packageName);
		System.out.println("Java files generated at: " + byteCodeDir);
		return new ByteCodePackage(byteCodeDir, packageName);
	}

	private static File createByteCodesDir(String byteCodeDirParent, String currentTime) {
		String nextDir = RandomStringUtils.getRandomLetters(10);
		return new File(byteCodeDirParent + "/" + currentTime + "/" + nextDir);
	}

	static String generatePakcageName(String currentTime) {

		List<String> fragments = new ArrayList<String>();
		fragments.add("wsdl2bytecodes" + currentTime); // first level
		for (int i = 0; i < 9; i++) { // the next 9 levels
			// each level may have 3 to 13 words
			fragments.add(RandomStringUtils.getRandomLetters(3 + RandomUtils.nextInt(0, 10)));
		}
		return StringUtils.join(fragments, ".");
	}

	private static void doWsImport(String outputDir, String wsdlUrl, String packageName) throws WsdlImportException {

		File jaxbFile = copyDefaultJaxbFile(outputDir);

		List<String> cmdList = new ArrayList<String>();
		if (isWindows()) {
			cmdList.add("cmd.exe");
			cmdList.add("/c");
		}

		cmdList.add("wsimport");
		cmdList.add("-B-XautoNameResolution");
		cmdList.add("-b");
		cmdList.add(jaxbFile.getAbsolutePath());
		cmdList.add("-s");
		cmdList.add(outputDir);
		cmdList.add("-d");
		cmdList.add(outputDir);
		if (packageName != null) {
			cmdList.add("-p");
			cmdList.add(packageName);
		}
		cmdList.add("-verbose");
		cmdList.add(wsdlUrl);

		String[] cmdArray = new String[cmdList.size()];
		cmdList.toArray(cmdArray);
		try {
			String consoleOutput = SystemProcessUtils.exec(cmdArray);
			if (consoleOutput.contains("Two declarations cause a collision in the ObjectFactory class")) {
				throw new DeclarationCollisionException(consoleOutput);
			}

		} catch (SystemProcessException e) {
			throw new WsdlImportException(e.getConsoleOutput());
		}

	}

	private static File copyDefaultJaxbFile(String outputDir) {
		final String default_jaxb="<jxb:bindings version=\"2.0\" xmlns:jxb=\"http://java.sun.com/xml/ns/jaxb\""+
				" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"+
				" <jxb:globalBindings generateElementProperty=\"false\">"+
				" <jxb:javaType name=\"java.lang.Integer\" xmlType=\"xs:int\" />"+
				" <jxb:javaType name=\"java.lang.Integer\" xmlType=\"xs:unsignedShort\" />"+
				" <jxb:javaType name=\"java.lang.Long\" xmlType=\"xs:long\" />"+
				" <jxb:javaType name=\"java.lang.Long\" xmlType=\"xs:unsignedInt\" />"+
				" <jxb:javaType name=\"java.lang.Short\" xmlType=\"xs:short\" />"+
				" <jxb:javaType name=\"java.lang.Short\" xmlType=\"xs:unsignedByte\" />"+
				" <jxb:javaType name=\"java.lang.Double\" xmlType=\"xs:double\" />"+
				" <jxb:javaType name=\"java.lang.Float\" xmlType=\"xs:float\" />"+
				" <jxb:javaType name=\"java.lang.Boolean\" xmlType=\"xs:boolean\" />"+
				" <jxb:javaType name=\"java.lang.Byte\" xmlType=\"xs:byte\" />"+
				"</jxb:globalBindings></jxb:bindings>";

		File generatedJaxbFile = new File(outputDir, "jaxb.xml");
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = new ByteArrayInputStream(default_jaxb.getBytes(Charset.forName("UTF-8")));
			outputStream = new FileOutputStream(generatedJaxbFile);
			IOUtils.copy(inputStream, outputStream);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}

		return generatedJaxbFile;
	}

	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	static String formatDate(Date date, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(date);
	}

	public static final class DeclarationCollisionException extends WsdlImportException {
		public DeclarationCollisionException(String readableReason) {
			super(readableReason);
		}

		private static final long serialVersionUID = 7625306326132684237L;
	}
}