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
import javax.inject.Named;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.inject.Inject;

/**
 * Encrypts the {@link EventDispatch} content using the AES algorithm to generate the {@link EventEnvelope}.
 * <p>
 * To define the key you need to specify the binding {@link ZeroMQConfig}.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class AESEventEncrypter implements EventEncrypter {

	private SecretKeySpec skeySpec;
	private final static String ALGORITHM = "AES/CBC/PKCS5Padding"; //$NON-NLS-1$

	// private Cipher cipher;

	/**
	 * Change the encryption key.
	 * 
	 * @param key - injected encryption key.
	 * @throws Exception
	 */
	@Inject
	void setKey(@Named(ZeroMQConfig.AES_KEY) String key) throws Exception {
		// FIXME: Unify the string charset to convert from to byte array
		byte[] raw = key.getBytes(ZeroMQConfig.BYTE_ARRAY_STRING_CHARSET);
		int keySize = raw.length;
		if ((keySize % 16) == 0 || (keySize % 24) == 0 || (keySize % 32) == 0) {
			this.skeySpec = new SecretKeySpec(raw, "AES"); //$NON-NLS-1$
			// this.cipher = Cipher.getInstance(ALGORITHM);
		} else {
			throw new IllegalArgumentException(Locale.getString("INVALID_KEY_SIZE")); //$NON-NLS-1$
		}

	}

	@Override
	public EventEnvelope encrypt(EventPack pack) throws Exception {
		assert (pack != null) : "Parameter 'pack' must not be null"; //$NON-NLS-1$
		return new EventEnvelope(encrypt(pack.getContextId()), encrypt(pack.getSpaceId()), encrypt(pack.getScope()), encrypt(pack.getHeaders()), encrypt(pack.getEvent()));
	}

	@Override
	public EventPack decrypt(EventEnvelope envelope) throws Exception {
		assert (envelope != null) : "Parameter 'envelope' must not be null"; //$NON-NLS-1$
		EventPack pack = new EventPack();
		pack.setContextId(decrypt(envelope.getContextId()));
		pack.setSpaceId(decrypt(envelope.getSpaceId()));
		pack.setScope(decrypt(envelope.getScope()));
		pack.setEvent(decrypt(envelope.getBody()));
		pack.setHeaders(decrypt(envelope.getCustomHeaders()));

		return pack;
	}

	private byte[] decrypt(byte[] encrypted) throws GeneralSecurityException {
		assert (encrypted != null && encrypted.length > 0) : "Parameter 'encrypted' must not be null nor zero-length"; //$NON-NLS-1$
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, this.skeySpec, new IvParameterSpec(new byte[16]));
		byte[] b = cipher.doFinal(encrypted);
		assert (b != null && b.length > 0) : "Result of decryption is null or empty"; //$NON-NLS-1$
		return b;
	}

	private byte[] encrypt(byte[] value) throws GeneralSecurityException {
		assert (value != null && value.length > 0) : "Parameter 'value' must not be null nor zero-length"; //$NON-NLS-1$
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, this.skeySpec, new IvParameterSpec(new byte[16]));
		byte[] b = cipher.doFinal(value);
		assert (b != null && b.length > 0) : "Result of encryption is null or empty"; //$NON-NLS-1$
		return b;
	}

	/** {@inheritDoc}
	 */
	@Override
	public byte[] encryptUUID(UUID uuid) {
		try {
			return encrypt(uuid.toString().getBytes(ZeroMQConfig.BYTE_ARRAY_STRING_CHARSET));
		}
		catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

}
