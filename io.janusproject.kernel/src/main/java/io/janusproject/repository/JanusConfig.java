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
package io.janusproject.repository;


/** Constants for the Janus configuration.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JanusConfig {
	
	/** Name of the property that contains the identifier of the Janus context.
	 * @see #DEFAULT_JANUS_CONTEXT_ID
	 */
	public static final String JANUS_CONTEXT_ID = "janus.context.id"; //$NON-NLS-1$

	/** Name of the property that contains the identifier for the default
	 * space of the Janus context.
	 * @see #DEFAULT_JANUS_SPACE_ID
	 */
	public static final String JANUS_DEF_SPACE_ID = "janus.context.defspace.id"; //$NON-NLS-1$
	
	/** The default value for the Janus context identifier.
	 * @see #JANUS_CONTEXT_ID
	 */
	public static final String DEFAULT_JANUS_CONTEXT_ID = "2c38fb7f-f363-4f6e-877b-110b1f07cc77"; //$NON-NLS-1$
	
	/** The default value for the Janus space identifier.
	 * @see #JANUS_DEF_SPACE_ID
	 */
	public static final String DEFAULT_JANUS_SPACE_ID = "7ba8885d-545b-445a-a0e9-b655bc15ebe0"; //$NON-NLS-1$
}
