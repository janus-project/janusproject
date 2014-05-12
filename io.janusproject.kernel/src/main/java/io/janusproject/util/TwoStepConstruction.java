/* 
 * $Id$
 * 
 * Copyright (C) 2010-2012 Janus Core Developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.janusproject.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation that permits to mark the classes that
 * must be constructed in two explicit steps.
 * <p>
 * The steps are:<ol>
 * <li>invoke the constructor (explicit call);</li>
 * <li>injection mechanism (implicit, done during the constructor call);</li>
 * <li>finalization of the construction, eg. <code>postConstruction()</code>.</li>
 * </ol>
 * 
 * @author $Author: galland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface TwoStepConstruction {
	
	/** Replies the names of the functions that are used for post construction.
	 * 
	 * @return the names of the functions.
	 */
	public String[] names() default {"postConstruction"};
	
}