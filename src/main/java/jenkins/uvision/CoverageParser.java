/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jenkins.uvision;

import hudson.util.IOException2;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 *
 * @author prasad
 */
public class CoverageParser {
    /** Do not instantiate CloverCoverageParser. */
    private CoverageParser() {
    }

    public static TestRun parse(File inFile) throws IOException {
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            fileInputStream = new FileInputStream(inFile);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            CoverageParser parser = new CoverageParser();
            TestRun result = parse(bufferedInputStream);
            if (result == null) throw new NullPointerException();
            return result;
        } finally {
            try {
                if (bufferedInputStream != null)
                    bufferedInputStream.close();
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public static TestRun parse(InputStream in) throws IOException {
        if (in == null) throw new NullPointerException();
        Digester digester = new Digester();
        digester.setClassLoader(CoverageParser.class.getClassLoader());
        
        digester.addObjectCreate("TestRun", TestRun.class);
        digester.addObjectCreate("TestRun/SuccessfulTests", SuccessfulTests.class);
        
        digester.addObjectCreate("TestRun/SuccessfulTests/Test", Test.class);
        digester.addBeanPropertySetter("TestRun/SuccessfulTests/Test/File", "file");
        digester.addBeanPropertySetter("TestRun/SuccessfulTests/Test/Name", "name");
        digester.addBeanPropertySetter("TestRun/SuccessfulTests/Test/Coverage", "coverage");
        digester.addBeanPropertySetter("TestRun/SuccessfulTests/Test/Instructions", "instructions");
        digester.addSetProperties("TestRun/SuccessfulTests/Test","id", "id");
        digester.addSetProperties("TestRun/SuccessfulTests/Test", "duration","duration");    
        
        digester.addSetNext("TestRun/SuccessfulTests/Test", "addTest");
        digester.addSetNext("TestRun/SuccessfulTests", "addSuccessfullTests");
        
        digester.addObjectCreate("TestRun/Statistics", Statistics.class);
        digester.addBeanPropertySetter("TestRun/Statistics/Tests", "tests");
        digester.addBeanPropertySetter("TestRun/Statistics/FailuresTotal","failuresTotal");
        digester.addBeanPropertySetter("TestRun/Statistics/Errors", "errors");
        digester.addBeanPropertySetter("TestRun/Statistics/Ignored", "ignored");
        digester.addBeanPropertySetter("TestRun/Statistics/Failures", "failures");
        digester.addBeanPropertySetter("TestRun/Statistics/Duration", "duration");
        
        digester.addSetNext("TestRun/Statistics", "addStatistics");
        digester.addSetNext("TestRun/Statistics", "setStatistics");
        
        try {
            return (TestRun) digester.parse(in);
        } catch (SAXException e) {
            throw new IOException2("Cannot parse coverage results", e);
        }
    }
}
