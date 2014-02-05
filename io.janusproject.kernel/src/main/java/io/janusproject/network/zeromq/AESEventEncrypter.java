/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on &lt;http://www.janus-project.org&gt;
 * Copyright (C) 2013 Janus Core Developers
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
 * along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package io.janusproject.network.zeromq;

import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Charsets;

/**
 * Encrypts the {@link EventDispatch} content using the AES algorithm to
 * generate the {@link EventEnvelope}
 * 
 * To define the key you need to specify the binding {@link ZeroMQConfig#}
 * 
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class AESEventEncrypter implements EventEncrypter {

	private SecretKeySpec skeySpec;
	private final static String ALGORITHM = "AES/CBC/PKCS5Padding";

	//private Cipher cipher;

	@Inject
	void setKey(@Named(ZeroMQConfig.AES_KEY) String key)
			throws Exception {
		byte[] raw = key.getBytes(Charsets.UTF_8);
		int keySize = raw.length;
		if ((keySize % 16) == 0 || (keySize % 24) == 0 || (keySize % 32) == 0) {
			this.skeySpec = new SecretKeySpec(raw, "AES");
			//this.cipher = Cipher.getInstance(ALGORITHM);
		} else {
			throw new IllegalArgumentException(
					"Wrong keysize: must be equal to 128, 192 or 256. If you want to use keys higher of 128 bit (16 chars), you must install the Unlimited Strength Jurisdiction Policy (See http://docs.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html)");
		}

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public EventEnvelope encrypt(EventPack pack) throws Exception {
		return EventEnvelope.build(encrypt(pack.getContextId()),
				encrypt(pack.getSpaceId()), encrypt(pack.getScope()),
				encrypt(pack.getHeaders()), encrypt(pack.getEvent()));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public EventPack decrypt(EventEnvelope envelope) throws Exception {
		EventPack pack = new EventPack();
		pack.setContextId(decrypt(envelope.getContextId()));
		pack.setSpaceId(decrypt(envelope.getSpaceId()));
		pack.setScope(decrypt(envelope.getScope()));
		pack.setEvent(decrypt(envelope.getBody()));
		pack.setHeaders(decrypt(envelope.getCustomHeaders()));

		return pack;
	}

	private String decrypt(byte[] encrypted) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, this.skeySpec, new IvParameterSpec(
				new byte[16]));
		byte[] original = cipher.doFinal(encrypted);
		return new String(original, Charsets.UTF_8);

	}

	private byte[] encrypt(String value) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, this.skeySpec, new IvParameterSpec(
				new byte[16]));
		return cipher.doFinal(value.getBytes(Charsets.UTF_8));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public byte[] encrytContextID(UUID id) throws Exception {
		return encrypt(id.toString());
	}
}
