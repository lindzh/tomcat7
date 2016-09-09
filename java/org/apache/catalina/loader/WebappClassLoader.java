package org.apache.catalina.loader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.LifecycleException;
/**
 * 
 * 
 * @author lindezhi
 * 2016年7月2日 上午11:26:56
 */
public class WebappClassLoader extends AbstractWebappClassLoader{
	
    private static final org.apache.juli.logging.Log log = org.apache.juli.logging.LogFactory.getLog( WebappClassLoader.class );
    
    private AbstractWebappClassLoader loaderLink;
    
    ///data/gateway/tomcat7/catalina
    public static final String baseDir;
    
    private AbstractWebappClassLoader last;
    
    private Map<String,Class> classMap = new ConcurrentHashMap<String,Class>();
    
    static{
    	String catalinaHome = System.getProperty("catalina.home");
    	baseDir = catalinaHome + File.separator + "catalina";
    }
    
	public WebappClassLoader(){
		super();
	}
	
	public WebappClassLoader(ClassLoader parent){
		super(parent);
	}
	
	@Override
	public AbstractWebappClassLoader next() {
		return null;
	}
	
	@Override
	public void setRootLoader(WebappClassLoader loader) {
		
	}
	
	@Override
	public Class rootLoadClass(String name, boolean throwNotFound)
			throws ClassNotFoundException {
		Class<?> result = null;
		result = classMap.get(name);
		if (result != null) {
			return result;
		}
		try {
			result = super.findClass(name);
		} catch (Exception e) {
		}
		if (result == null && !throwNotFound) {
			try {
				result = super.loadClass(name);
			} catch (Exception e) {

			}
		}
		if (result == null) {
			try {
				result = this.getClass().getClassLoader().loadClass(name);
			} catch (Exception e) {

			}
		}
		if (result == null && name.startsWith("com.linda.koala.biz")) {
			try {
				if (last != null) {
					result = last.findClass(name);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (result != null) {
//			log.info("[loader] WebappClassLoader root load class:" + name);
			classMap.put(name, result);
		}
		if (throwNotFound && result == null) {
			throw new ClassNotFoundException(name);
		}
		return result;
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> result = null;
		synchronized (classMap) {
			result = this.rootLoadClass(name, false);
			if(result==null){
				try{
					if(last!=null){
						return last.findClass(name);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(result==null){
				throw new ClassNotFoundException(name);
			}
//			log.info("[loader] WebappClassLoader load class:"+name);
			classMap.put(name, result);
		}
		return result;
	}
	
	@Override
	public void start() throws LifecycleException {
		super.start();
		try{
			URL resource = WebappClassLoader.class.getResource("");
//			log.info("[loader] base path:"+resource);
			String loaderClassName = "org.apache.catalina.loader._ea.CatalinaClassLoader_ea60";
			Class<?> loaderClass = this.getClass().getClassLoader().loadClass(loaderClassName);
	        Class<?>[] argTypes = { ClassLoader.class };
	        Object[] args = { this.getParent() };
			Constructor<?> constructor = loaderClass.getConstructor(argTypes);
			loaderLink = (AbstractWebappClassLoader)constructor.newInstance(args);
			loaderLink.setRootLoader(this);
			super.copyStateWithoutTransformers(loaderLink);
			loaderLink.start();
			
			AbstractWebappClassLoader next = loaderLink;
			while(next!=null){
				last = next;
				next = next.next();
			}
		}catch(Exception e){
			
		}
	}

	@Override
	public void stop() throws LifecycleException {
		super.stop();
	}

	public WebappClassLoader copyWithoutTransformers() {
		WebappClassLoader result = new WebappClassLoader(getParent());
		super.copyStateWithoutTransformers(result);
		try {
			result.start();
		} catch (LifecycleException e) {
			throw new IllegalStateException(e);
		}
		return result;
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		return this.rootLoadClass(name, true);
	}

}
