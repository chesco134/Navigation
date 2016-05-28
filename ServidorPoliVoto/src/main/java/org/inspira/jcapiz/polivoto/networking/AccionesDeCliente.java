package org.inspira.jcapiz.polivoto.networking;

import android.util.Log;

import org.inspira.jcapiz.polivoto.pojo.DatosDeAccionCliente;
import org.inspira.jcapiz.polivoto.seguridad.Hasher;
import org.inspira.jcapiz.polivoto.seguridad.PRNGFixes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AccionesDeCliente {

	// Al conectar con el servidor, cuenta el envío del primer byte.
	// Al enviar el byte -1, el servidor entiende que lo que sigue es
	// un intercambio de llave simétrica a través de un equema de llave
	// pública.

	// Posterior a que el sistema acepta un usuario,
	// le entrega un id de inicio de sesión que el cliente deberá
	// usar para hacer peticiones al servidor.
	// Con ello el servidor verifica que se trate de un inicio de
	// sesión válido y utilizará la llave simétrica acordada en dicho
	// establecimiento de sesión con el cliente.

	private Socket socket;
	private IOHandler ioHandler;
	private byte[] chunk;
    private SecretKey secretKey;
    private Cipher cipher;
    private SecretKey partKey;
    private DatosDeAccionCliente data;

    public AccionesDeCliente(String host) throws IOException {
		PRNGFixes.apply();
		socket = new Socket(host, 23543);
        data = new DatosDeAccionCliente();
		data.setrHost(host);
		socket.close();
	}

	public AccionesDeCliente(DatosDeAccionCliente data){
        PRNGFixes.apply();
        this.data = data;
    }

    public void probarConexion(String host) throws IOException{
        socket = new Socket(host, 23543);
        data.setrHost(host);
        socket.close();
    }

	public void signIn() throws IOException,
			NoSuchAlgorithmException, InvalidKeySpecException, JSONException,
			InvalidKeyException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		socket = new Socket(data.getrHost(), 23543);
		ioHandler = new IOHandler(new DataInputStream(socket.getInputStream()),
				new DataOutputStream(socket.getOutputStream()));
		ioHandler.writeInt(-1);
		System.out.println("We're waiting for the public key...");
		secretKey = null;
		cipher = null;
		chunk = ioHandler.handleIncommingMessage();
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(chunk);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey usrPubKey = keyFactory.generatePublic(pubKeySpec);
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, usrPubKey);
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128); // por ejemplo
		secretKey = keyGen.generateKey();
        data.setEncodedKey(secretKey.getEncoded());
		byte[] cipB = cipher.doFinal(data.getEncodedKey());
		ioHandler.sendMessage(cipB);
		System.out.println("We've just sent " + cipB.length + " bytes.");
		JSONObject json = new JSONObject();
		json.put("uName", data.getUsrName());
		json.put("psswd", data.getPsswd());
		cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		chunk = cipher.doFinal(json.toString().getBytes());
		ioHandler.sendMessage(chunk);
		data.setLID(ioHandler.readInt());
		ioHandler.close();
		System.out.println("Me llegó " + data.getLID());
		socket.close();
		if (data.getLID() == -1)
			throw new IOException("Error en las credenciales");
	}

	public void proporcionarInformacionDeInicio(){
    }

	public byte[] decrypt(byte[] bytes) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        secretKey = new SecretKeySpec(data.getEncodedKey(), "AES");
        Log.d("Decrypt", Arrays.toString(secretKey.getEncoded()));
        cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(bytes);
	}

	public String getHost() {
		return data.getrHost();
	}

    public void setHost(String host){
        data.setrHost(host);
    }

	public void setPsswd(String psswd) {
		data.setPsswd(psswd);
	}

	public void setUsrName(String usrName) {
		data.setUsrName(usrName);
	}

    public DatosDeAccionCliente getData() {
        return data;
    }

	public String[] getPerfilesArray() throws JSONException {
		return data.getPerfiles();
	}
}