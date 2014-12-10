/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 * 
 * Copyright (C) 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;

import io.janusproject.kernel.Kernel;
import io.sarl.lang.core.Agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.arakhne.afc.vmutil.FileSystem;
import org.arakhne.afc.vmutil.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;

import static org.mockito.Mockito.*;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class ClasspathTest extends Assert {

	@Test
	public void onlyJanusGuavaInClasspath() throws Exception {
		String rawClasspath = System.getProperty("java.class.path"); //$NON-NLS-1$
		assertNotNull(rawClasspath);
		assertNotEquals("", rawClasspath);
		String[] paths = rawClasspath.split(File.pathSeparator);
		
		for(String path : paths) {
			File file = new File(path);
			if (file.isDirectory()) {
				file = new File(file, "META-INF");
				file = new File(file, "maven");
				file = new File(file, "com.google.guava");
				file = new File(file, "guava");
				file = new File(file, "pom.xml");
				assertFalse("The original Guava library was found in the classpath. Only the Janus fork must be used.", file.exists());
			} else {
				try (JarFile jf = new JarFile(file)) {
					JarEntry entry = jf.getJarEntry("META-INF/maven/com.google.guava/guava/pom.xml");
					assertNull("The original Guava library was found in the classpath. Only the Janus fork must be used.", entry);
				}
			}
		}
		
	}

}
