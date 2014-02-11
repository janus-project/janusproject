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
package io.janusproject.network.zeromq;

import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;

import org.arakhne.afc.vmutil.locale.Locale;

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
	private final static String ALGORITHM = "AES/CBC/PKCS5Padding"; //$NON-NLS-1$

	//private Cipher cipher;

	@Inject
	void setKey(@Named(ZeroMQConfig.AES_KEY) String key)
			throws Exception {
		byte[] raw = key.getBytes(Charsets.UTF_8);
		int keySize = raw.length;
		if ((keySize % 16) == 0 || (keySize % 24) == 0 || (keySize % 32) == 0) {
			this.skeySpec = new SecretKeySpec(raw, "AES"); //$NON-NLS-1$
			//this.cipher = Cipher.getInstance(ALGORITHM);
		} else {
			throw new IllegalArgumentException(Locale.getString("INVALID_KEY_SIZE")); //$NON-NLS-1$
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
