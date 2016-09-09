package org.apache.catalina.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.tomcat.util.codec.DecoderException;
import org.apache.tomcat.util.codec.binary.Base64;

public class CatalinaClassLoader_FFFF extends WebappClassLoaderBase{
	
    private static final int EOF = -1;
    
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    
    private String key_FFFF = "c0893ecbdfafdc6f04d6b9f7835bca2661c8d02968aeb9c9";
	
    private static final org.apache.juli.logging.Log log = org.apache.juli.logging.LogFactory.getLog( BasicClassLoader.class );
	
	public CatalinaClassLoader_FFFF(){
		super();
	}
	
	public CatalinaClassLoader_FFFF(ClassLoader parent){
		super(parent);
	}
	
	private boolean isFind_FFFF(String name){
		return false;
	}
	
	private String[] getSuffix_FFFF(){
		return new String[]{".class_fffe",".classx"};
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> result = null;
		try{
			result = super.findClass(name);
		}catch(Exception e){
		}
		if(result==null){
			if(this.isFind_FFFF(name)){
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
	
	private String toString(String[] arr){
		StringBuilder sb = new StringBuilder();
		for(String str:arr){
			sb.append(str);
			sb.append(",");
		}
		return sb.toString();
	}
	
	private int toDigit(final char ch, final int index) throws DecoderException {
		final int digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new DecoderException("Illegal hexadecimal character " + ch
					+ " at index " + index);
		}
		return digit;
	}
	
	private Class<?> findClassInWebApp(String name) throws ClassNotFoundException{
		boolean find = false;
		ResourceEntry entry = null;
		String[] suffix = this.getSuffix_FFFF();
		for(String signSuffix:suffix){
			entry = this.findClassResource(name, signSuffix);
			if(entry!=null&&entry.source!=null){
				find = true;
				break;
			}
		}
		if(!find){
			String classLoaderName = "org.apache.catalina.loader.00.CatalinaClassLoader_FFFE";
			Class<?> subLoader = this.findClass(classLoaderName);
			if(subLoader!=null){
				try{
					Constructor<WebappClassLoaderBase> constructor = (Constructor<WebappClassLoaderBase>) subLoader.getConstructor(ClassLoader.class);
					WebappClassLoaderBase instance = constructor.newInstance(this.getParent());
					super.copyStateWithoutTransformers(instance);
					instance.start();
					return instance.findClass(name);
				}catch(Exception e){
					
				}
			}
			log.info("[LOADER] find class "+name+" "+this.toString(suffix)+" null");
			throw new ClassNotFoundException(name);
		}else{
			log.info("[LOADER] find class:"+name+" with "+this.toString(suffix)+" "+entry.source.toString());
			try {
				if(entry.binaryContent==null){
					InputStream ins = entry.source.openStream();
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			        int n = 0;
			        while (EOF != (n = ins.read(buf))) {
			            bos.write(buf, 0, n);
			        }
					ins.close();
					entry.binaryContent = bos.toByteArray();
					bos.close();
				}
				
				//TODO   解码==============================
				try {
					char[] data = this.key_FFFF.toCharArray();
					final int len = data.length;
					if ((len & 0x01) != 0) {
						throw new DecoderException("Odd number of characters.");
					}
					final byte[] bin = new byte[len >> 1];
					for (int i = 0, j = 0; j < len; i++) {
						int f = toDigit(data[j], j) << 4;
						j++;
						f = f | toDigit(data[j], j);
						j++;
						bin[i] = (byte) (f & 0xFF);
					}
					DESedeKeySpec keyspec = new DESedeKeySpec(bin);
					SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
					SecretKey key = keyfactory.generateSecret(keyspec);
					Base64 base64 = new Base64();
					byte[] content = base64.decode(entry.binaryContent);
					Cipher cipher = Cipher.getInstance("DESede");
					cipher.init(Cipher.DECRYPT_MODE, key);
					entry.binaryContent = cipher.doFinal(content);
				}catch(Exception e){
					throw new ClassNotFoundException(e.getMessage(),e);
				}
				//=====================解码结束======================
				
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
			} catch (IOException e) {
				throw new ClassNotFoundException("read "+entry.source.toString(),e);
			}
		}
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
	
	
	public CatalinaClassLoader_FFFF copyWithoutTransformers() {
		try{
			CatalinaClassLoader_FFFF result = new CatalinaClassLoader_FFFF(this.getParent());
			super.copyStateWithoutTransformers(result);
			result.start();
			return result;
		}catch(Exception e){
			throw new IllegalStateException(e);
		}
	}

}
