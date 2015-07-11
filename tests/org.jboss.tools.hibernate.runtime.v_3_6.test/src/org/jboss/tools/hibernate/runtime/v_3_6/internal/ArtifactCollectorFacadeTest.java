package org.jboss.tools.hibernate.runtime.v_3_6.internal;

import java.lang.reflect.Method;

import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.jboss.tools.hibernate.runtime.common.AbstractArtifactCollectorFacade;
import org.jboss.tools.hibernate.runtime.common.IFacadeFactory;
import org.jboss.tools.hibernate.runtime.spi.IArtifactCollector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ArtifactCollectorFacadeTest {
	
	private static final IFacadeFactory FACADE_FACTORY = new FacadeFactoryImpl();

	private String methodName = null;
	private Object[] arguments = null;
	
	private IArtifactCollector artifactCollector = null; 
	
	@Before
	public void setUp() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(ArtifactCollector.class);
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(
					Object obj, 
					Method method, 
					Object[] args, 
					MethodProxy proxy) throws Throwable {
				if (methodName == null) {
					methodName = method.getName();
				}
				if (arguments == null) {
					arguments = args;
				}
				return proxy.invokeSuper(obj, args);
			}					
		});
		artifactCollector = new AbstractArtifactCollectorFacade(FACADE_FACTORY, enhancer.create()) {};
		reset();
	}

	@Test
	public void testGetFileTypes() {
		Assert.assertNotNull(artifactCollector.getFileTypes());
		Assert.assertEquals("getFileTypes", methodName);
		Assert.assertArrayEquals(new Object[] {}, arguments);
	}

	private void reset() {
		methodName = null;
		arguments = null;
	}
	
}
