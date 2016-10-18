package org.jboss.tools.hibernate.runtime.v_4_3.internal;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.DefaultNamingStrategy;
import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.Settings;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.OneToMany;
import org.jboss.tools.hibernate.runtime.common.IFacade;
import org.jboss.tools.hibernate.runtime.common.IFacadeFactory;
import org.jboss.tools.hibernate.runtime.spi.IConfiguration;
import org.jboss.tools.hibernate.runtime.spi.IDialect;
import org.jboss.tools.hibernate.runtime.spi.IMappings;
import org.jboss.tools.hibernate.runtime.spi.INamingStrategy;
import org.jboss.tools.hibernate.runtime.spi.IPersistentClass;
import org.jboss.tools.hibernate.runtime.spi.ISessionFactory;
import org.jboss.tools.hibernate.runtime.spi.ISettings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigurationFacadeTest2 {
	
	private static final String FOO_HBM_XML_STRING =
			"<!DOCTYPE hibernate-mapping PUBLIC" +
			"		'-//Hibernate/Hibernate Mapping DTD 3.0//EN'" +
			"		'http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd'>" +
			"<hibernate-mapping package='org.jboss.tools.hibernate.runtime.v_4_3.internal'>" +
			"  <class name='ConfigurationFacadeTest2$Foo'>" + 
			"    <id name='fooId'/>" +
			"  </class>" +
			"</hibernate-mapping>";
	
	private static final String BAR_HBM_XML_STRING =
			"<!DOCTYPE hibernate-mapping PUBLIC" +
			"		'-//Hibernate/Hibernate Mapping DTD 3.0//EN'" +
			"		'http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd'>" +
			"<hibernate-mapping package='org.jboss.tools.hibernate.runtime.v_4_3.internal'>" +
			"  <class name='ConfigurationFacadeTest2$Bar'>" + 
			"    <id name='barId'/>" +
			"    <set name='fooSet' inverse='true'>" +
			"      <key column='fooId'/>" +
			"      <one-to-many class='ConfigurationFacadeTest2$Foo'/>" +
			"    </set>" +
			"  </class>" +
			"</hibernate-mapping>";
	
	static class Foo {
		public String fooId;
	}
	
	static class Bar {
		public String barId;
		public Set<Foo> fooSet;
	}
			
	private static final IFacadeFactory FACADE_FACTORY = new FacadeFactoryImpl();

	private IConfiguration configurationFacade = null;
	private Configuration configuration = null;

	@Before
	public void setUp() {
		configuration = new Configuration();
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
	}
	
	@Test
	public void testGetProperty() {
		Assert.assertNull(configurationFacade.getProperty("foo"));
		configuration.setProperty("foo", "bar");
		Assert.assertEquals("bar", configurationFacade.getProperty("foo"));
	}

	@Test 
	public void testSetProperties() {
		Properties testProperties = new Properties();
		Assert.assertNotSame(testProperties, configuration.getProperties());
		Assert.assertSame(
				configurationFacade, 
				configurationFacade.setProperties(testProperties));
		Assert.assertSame(testProperties, configuration.getProperties());
	}
	
	@Test
	public void testAddFile() throws Exception {
		File testFile = File.createTempFile("test", "hbm.xml");
		PrintWriter printWriter = new PrintWriter(testFile);
		printWriter.write(FOO_HBM_XML_STRING);
		printWriter.close();
		String fooClassName = 
				"org.jboss.tools.hibernate.runtime.v_4_3.internal.ConfigurationFacadeTest2$Foo";
		// make sure the mappings are built before checking whether the class exists
		configuration.buildMappings();
		Assert.assertNull(configuration.getClassMapping(fooClassName));
		Assert.assertSame(
				configurationFacade,
				configurationFacade.addFile(testFile));
		// now that the file has been added, rebuild the mappings 
		configuration.buildMappings();
		// now the class should exist 
		Assert.assertNotNull(configuration.getClassMapping(fooClassName));
		Assert.assertTrue(testFile.delete());
	}
	
	@Test
	public void testSetEntityResolver() {
		EntityResolver testResolver = new DefaultHandler();
		Assert.assertNotSame(testResolver, configuration.getEntityResolver());
		configurationFacade.setEntityResolver(testResolver);
		Assert.assertSame(testResolver, configuration.getEntityResolver());
	}
	
	@Test
	public void testGetEntityResolver() {
		EntityResolver testResolver = new DefaultHandler();
		Assert.assertNotSame(testResolver, configurationFacade.getEntityResolver());
		configuration.setEntityResolver(testResolver);
		Assert.assertSame(testResolver, configurationFacade.getEntityResolver());
	}
	
	@Test
	public void testSetNamingStrategy() {
		DefaultNamingStrategy dns = new DefaultNamingStrategy();
		INamingStrategy namingStrategy = FACADE_FACTORY.createNamingStrategy(dns);
		Assert.assertNotSame(dns, configuration.getNamingStrategy());
		configurationFacade.setNamingStrategy(namingStrategy);
		Assert.assertSame(dns, configuration.getNamingStrategy());
	}
	
	@Test
	public void testAddProperties() {
		Assert.assertNull(configuration.getProperty("foo"));
		Properties testProperties = new Properties();
		testProperties.put("foo", "bar");
		configurationFacade.addProperties(testProperties);
		Assert.assertEquals("bar", configuration.getProperty("foo"));
	}
	
	@Test
	public void testConfigure() {
		String fooClassName = 
				"org.jboss.tools.hibernate.runtime.v_4_3.internal.test.Foo";
		Assert.assertNull(configuration.getClassMapping(fooClassName));
		configurationFacade.configure();
		Assert.assertNotNull(configuration.getClassMapping(fooClassName));
	}
	
	@Test 
	public void testCreateMappings() {
		configuration.setProperty("createMappingsProperty", "a lot of blabla");
		IMappings mappingsFacade = configurationFacade.createMappings();
		Assert.assertNotNull(mappingsFacade);
		Object object = ((IFacade)mappingsFacade).getTarget();
		Assert.assertTrue(object instanceof Mappings);
		Mappings mappings = (Mappings)object;
		Assert.assertEquals(
				"a lot of blabla", 
				mappings.getConfigurationProperties().get("createMappingsProperty"));
	}

	@Test
	public void testBuildMappings() throws Exception {
		File fooFile = File.createTempFile("foo", "hbm.xml");
		PrintWriter fooWriter = new PrintWriter(fooFile);
		fooWriter.write(FOO_HBM_XML_STRING);
		fooWriter.close();
		configuration.addFile(fooFile);
		File barFile = File.createTempFile("bar", "hbm.xml");
		PrintWriter barWriter = new PrintWriter(barFile);
		barWriter.write(BAR_HBM_XML_STRING);
		barWriter.close();
		configuration.addFile(barFile);
		String collectionName = 
			"org.jboss.tools.hibernate.runtime.v_4_3.internal.ConfigurationFacadeTest2$Bar.fooSet";
		Assert.assertNull(configuration.getCollectionMapping(collectionName));
		configurationFacade.buildMappings();
		Collection collection = configuration.getCollectionMapping(collectionName);
		OneToMany element = (OneToMany)collection.getElement();
		Assert.assertEquals(
				"org.jboss.tools.hibernate.runtime.v_4_3.internal.ConfigurationFacadeTest2$Foo",
				element.getAssociatedClass().getClassName());
	}
	
	@Test
	public void testBuildSessionFactory() throws Throwable {
		ISessionFactory sessionFactoryFacade = 
				configurationFacade.buildSessionFactory();
		Assert.assertNotNull(sessionFactoryFacade);
		Object sessionFactory = ((IFacade)sessionFactoryFacade).getTarget();
		Assert.assertNotNull(sessionFactory);
		Assert.assertTrue(sessionFactory instanceof SessionFactory);
	}
	
	@Test
	public void testBuildSettings() {
		ISettings settingsFacade = configurationFacade.buildSettings();
		Assert.assertNotNull(settingsFacade);
		Object settings = ((IFacade)settingsFacade).getTarget();
		Assert.assertNotNull(settings);
		Assert.assertTrue(settings instanceof Settings);
	}

	@Test
	public void testGetClassMappings() {
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
		Iterator<IPersistentClass> iterator = configurationFacade.getClassMappings();
		Assert.assertFalse(iterator.hasNext());
		configuration.configure();
		configuration.buildMappings();
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
		iterator = configurationFacade.getClassMappings();
		IPersistentClass persistentClassFacade = iterator.next();
		Assert.assertEquals(
				"org.jboss.tools.hibernate.runtime.v_4_3.internal.test.Foo",
				persistentClassFacade.getClassName());
	}
	
	@Test
	public void testSetPreferBasicCompositeIds() {
		JDBCMetaDataConfiguration configuration = new JDBCMetaDataConfiguration();
		configurationFacade = FACADE_FACTORY.createConfiguration(configuration);
		// the default is false
		Assert.assertTrue(configuration.preferBasicCompositeIds());
		configurationFacade.setPreferBasicCompositeIds(false);
		Assert.assertFalse(configuration.preferBasicCompositeIds());
	}
	
	@Test
	public void testGetDialect() {
		configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		IDialect dialectFacade = configurationFacade.getDialect();
		Assert.assertNotNull(dialectFacade);
		Dialect dialect = (Dialect)((IFacade)dialectFacade).getTarget();
		Assert.assertEquals("org.hibernate.dialect.H2Dialect", dialect.getClass().getName());
	}
	
}
