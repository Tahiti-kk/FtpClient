package ftp;

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After;
import util.Constant;

import java.text.DecimalFormat;

/** 
* FileFtp Tester. 
* 
* @author <Authors name> 
* @since <pre>3�� 2, 2020</pre> 
* @version 1.0 
*/ 
public class FileFtpTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: getSocket() 
* 
*/ 
@Test
public void testGetSocket() throws Exception { 
//TODO: Test goes here...
} 

/** 
* 
* Method: uploadFile(String filePath) 
* 
*/ 
@Test
public void testUploadFile() throws Exception { 
//TODO: Test goes here...
    FileFtp fileFtp = new FileFtp();
    fileFtp.uploadFile(Constant.FILE_PATH);
} 


/** 
* 
* Method: connect() 
* 
*/ 
@Test
public void testConnect() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = FileFtp.getClass().getMethod("connect"); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

} 
