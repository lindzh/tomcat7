package org.apache.catalina.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;

import org.apache.catalina.LifecycleException;
/**
 * 
 * 
 * @author lindezhi
 * 2016年7月2日 上午11:26:56
 */
public class SimpleClassLoader_666 extends WebappClassLoaderBase{
	
    private static final int EOF = -1;
    
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	
    private static final org.apache.juli.logging.Log log = org.apache.juli.logging.LogFactory.getLog( SimpleClassLoader_666.class );
	
	private String signSuffix = ".classx";
	
	public SimpleClassLoader_666(){
		super();
	}
	
	public SimpleClassLoader_666(ClassLoader parent){
		super(parent);
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> result = null;
		try{
			result = super.findClass(name);
		}catch(Exception e){
		}
		if(result==null){
			if(this.findThis(name)){
				try{
					result = this.findClassInWebApp(name);
				}catch(Exception e){}
			}
		}
		if(result==null){
			throw new ClassNotFoundException(name);
		}
		return result;
	}
	
	private boolean findThis(String name){
		if(name.endsWith("_666")){
			return true;
		}else{
			return false;
		}
	}
	
	
	private byte[] encode(byte[] code){
		return code;
	}
	
	private byte[] decode(byte[] code){
		return code;
	}
	
	private Class<?> findClassInWebApp(String name) throws ClassNotFoundException{
		ResourceEntry entry = this.findClassResource(name, signSuffix);
		if(entry==null||entry.source==null){
			log.info("[LOADER] find class "+name+" "+signSuffix+" null");
			throw new ClassNotFoundException(name);
		}else{
			log.info("[LOADER] find class:"+name+" with classx "+entry.source.toString());
			
			try {
				this.doDecode(entry);
				return this.doDefindClass(name, entry);
			} catch (IOException e) {
				throw new ClassNotFoundException("read "+entry.source.toString(),e);
			}
		}
	}
	
	private void doDecode(ResourceEntry entry) throws IOException{
		if(entry.binaryContent==null){
			InputStream ins = entry.source.openStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			this.copy(ins, bos);
			ins.close();
			entry.binaryContent = bos.toByteArray();
			bos.close();
		}
		entry.binaryContent = new SimpleCipher().decodeBytes(entry.binaryContent);
	}
	
    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }
	
    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }
	
    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
	
	protected ResourceEntry findClassResource(String name,String suffix)throws ClassNotFoundException {
        if (!validate(name))
            throw new ClassNotFoundException(name);

        String tempPath = name.replace('.', '/');
        String classPath = tempPath + suffix;
        ResourceEntry entry = null;
        log.info("[LOADER] find:"+classPath);
        if (securityManager != null) {
        	log.info("[LOADER] find "+name +" with PrivilegedFindResourceByName");
            PrivilegedAction<ResourceEntry> dp = new PrivilegedFindResourceByName(name, classPath,false);
            entry = AccessController.doPrivileged(dp);
        } else {
            entry = super.findResourceInternal(name, classPath,false);
        }

        if (entry == null){
        	 throw new ClassNotFoundException(name);	
        }
        return entry; 
	}
	
	private Class<?> doDefindClass(String name,ResourceEntry entry) throws ClassNotFoundException{
	    Class<?> clazz = entry.loadedClass;
        if (clazz != null)
            return clazz;

        synchronized (this) {
            clazz = entry.loadedClass;
            if (clazz != null)
                return clazz;

            if (entry.binaryContent == null)
                throw new ClassNotFoundException(name);

            // Looking up the package
            String packageName = null;
            int pos = name.lastIndexOf('.');
            if (pos != -1)
                packageName = name.substring(0, pos);

            Package pkg = null;

            if (packageName != null) {
                pkg = super.getPackage(packageName);
                // Define the package (if null)
                if (pkg == null) {
                    try {
                        if (entry.manifest == null) {
                            super.definePackage(packageName, null, null, null, null,
                                    null, null, null);
                        } else {
                        	super.definePackage(packageName, entry.manifest,entry.codeBase);
                        }
                    } catch (IllegalArgumentException e) {
                        // Ignore: normal error due to dual definition of package
                    }
                    pkg = super.getPackage(packageName);
                }
            }

            if (securityManager != null) {

                // Checking sealing
                if (pkg != null) {
                    boolean sealCheck = true;
                    if (pkg.isSealed()) {
                        sealCheck = pkg.isSealed(entry.codeBase);
                    } else {
                        sealCheck = (entry.manifest == null)
                            || !isPackageSealed(packageName, entry.manifest);
                    }
                    if (!sealCheck)
                        throw new SecurityException
                            ("Sealing violation loading " + name + " : Package "
                             + packageName + " is sealed.");
                }

            }

            try {
                clazz = defineClass(name, entry.binaryContent, 0,
                        entry.binaryContent.length,
                        new CodeSource(entry.codeBase, entry.certificates));
            } catch (UnsupportedClassVersionError ucve) {
                throw new UnsupportedClassVersionError(
                        ucve.getLocalizedMessage() + " " +
                        sm.getString("webappClassLoader.wrongVersion",
                                name));
            }
            entry.loadedClass = clazz;
            entry.binaryContent = null;
            entry.source = null;
            entry.codeBase = null;
            entry.manifest = null;
            entry.certificates = null;
        }
        return clazz;
    }
	
	public SimpleClassLoader_666 copyWithoutTransformers() {
		SimpleClassLoader_666 result = new SimpleClassLoader_666(getParent());
		super.copyStateWithoutTransformers(result);
		try {
			result.start();
		} catch (LifecycleException e) {
			throw new IllegalStateException(e);
		}
		return result;
	}

}
