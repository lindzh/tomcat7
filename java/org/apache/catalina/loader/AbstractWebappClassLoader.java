package org.apache.catalina.loader;

public abstract class AbstractWebappClassLoader extends WebappClassLoaderBase{
	
	public abstract AbstractWebappClassLoader next();
	
	public abstract Class rootLoadClass(String name,boolean throwNotFound) throws ClassNotFoundException;
	
	public abstract void setRootLoader(WebappClassLoader loader);
	
	public AbstractWebappClassLoader(){
		super();
	}
	
	public AbstractWebappClassLoader(ClassLoader parent){
		super(parent);
	}
}
